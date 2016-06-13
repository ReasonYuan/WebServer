/**
 * Created by qiangpeng on 16/1/2.
 */
var express = require('express');

var dbmanager = require('../lib/db/dbmanager');
var constant = require('../lib/constant/constant');
var async = require('async');
var syncUserService = require('../lib/service/SyncUserService');
var config = require('../config');
var requestMethod = require("../lib/httprequest/requestmethod");
var ArrayUtils = require("../utils/ArrayUtils"); //use array extends contains

var router = express.Router();

var ATOMIC_PREFIX = "atomic_lock_";

/* GET home page. */
router.post('/remove_members_from_group', function(req, res, next) {
    var removeInfo = req.body

    if(!removeInfo) {
        res.status(constant.response_code.ok).json({error: "请提供要删除的信息"})
        return
    }

    if(!removeInfo.groupId){
        res.status(constant.response_code.ok).json({error: "请提供要群id"})
        return
    }

    if(!removeInfo.remove_members || !((removeInfo.remove_members) instanceof Array)){
        res.status(constant.response_code.ok).json({error: "请提供要删除的组员信息"})
        return
    }

    var remove_members = removeInfo.remove_members;
    var groupId = removeInfo.groupId

    async.each(remove_members, function(member, callback){
        syncUserService.removeUserChannels(member, [groupId], function(error, results){
            callback(error)
        })
    }, function(error, results){
        if(error){
            console.log("failed to remove members channel for " + removeInfo);
            res.status(constant.response_code.ok.code).json({error: error.toString()});
        } else {
            res.status(constant.response_code.ok.code).json({ok:"remove members success!"});
        }
    })
});



/* 删除群成员. */
router.post('/remove_members', function(req, res, next) {
    var removeInfo = req.body

    if(!removeInfo) {
        res.status(constant.response_code.ok).json({error: "请提供要删除的用户信息"})
        return
    }

    if(!removeInfo.groupId){
        res.status(constant.response_code.ok).json({error: "请提供要群id"})
        return
    }

    if(!removeInfo.remove_members || !((removeInfo.remove_members) instanceof Array) || removeInfo.remove_members.length == 0) {
        res.status(constant.response_code.ok).json({error: "请提供要删除的组员信息"})
        return
    }

    var remove_members = removeInfo.remove_members;
    var groupId = removeInfo.groupId

    var clusterUrl = config.urls.couchbase_cluster;
    var bucketName = config.buckets.couchbase_server;
    var syncGatewayUrl = config.urls.sync_gateway;
    var openedBucket;

    async.waterfall([
        //获取bucket连接
        function (callback) {
            dbmanager.openBucket(clusterUrl, bucketName, callback);
        },
        //保存变量bucket
        function(bucket, callback) {
            openedBucket = bucket;
            callback()
        }
    ], function(_err, _results){
        if(_err){
            res.status(constant.response_code.ok.code).json({error: error.toString()});
        } else {
            async.waterfall([
                function(callback){
                    //atomic operation, lock the resource
                    openedBucket.get(ATOMIC_PREFIX + groupId, {}, function(err_atomic, res_atomic){
                        if(err_atomic){
                            //增加一个15秒过期的锁文件
                            openedBucket.insert(ATOMIC_PREFIX + groupId, {inProcess:true}, {"expiry":15}, function(err, meta){
                                if(err){
                                    callback(new Error("群操作失败，严重错误，请联系管理员!"));
                                } else {
                                    //lock success
                                    callback()
                                }
                            })
                        } else {
                            callback(new Error("群操作失败，请稍后重试!"));
                        }
                    });
                }, function(callback){
                    var getUrl = syncGatewayUrl + bucketName + "/" + groupId;
                    //查询当前群信息, 并修改
                    requestMethod.requestGet(getUrl, function(error, data){
                        if(error || data.error){
                            //release lock
                            openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                            callback(new Error("群操作失败，未查到群信息!"))
                        } else {
                            var jsonData = JSON.parse(data)
                            var opSuccess = true
                            var rerror = null;
                            for(var i=0; i<remove_members.length; i++){
                                var beforeLen = jsonData.members.length
                                ArrayUtils.removeByValue(jsonData.members, remove_members[i])
                                if(beforeLen == jsonData.members.length){
                                    rerror = new Error("要删除的人不在群里!")
                                    opSuccess = false
                                    break;
                                }
                                if(jsonData.members.length <= 1){
                                    rerror = new Error("删除后群成<=1, 当前版本不支持该操作!")
                                    opSuccess = false
                                    break
                                }
                            }
                            if(opSuccess){
                                callback(null, jsonData)
                            } else {
                                //release lock
                                openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                                callback(rerror)
                            }
                        }
                    })
                }, function(results, callback){
                    var successRemoves = {}
                    async.each(remove_members, function(member, callback){
                        syncUserService.removeUserChannels(member, [groupId], function(error, results){
                            if(!error){
                                successRemoves[member] = true;
                            }
                            callback(error)
                        })
                    }, function(_e, _rs){
                        if(_e){
                            //由于不支持事物回滚, 手动记录移除channel，一个出错后全部回滚
                            for(var i=0; i<remove_members.length; i++){
                                if(successRemoves[remove_members[i]]){
                                    //回滚
                                    syncUserService.addUserAdminChannels(remove_members[i], [groupId], function(error, results){})
                                }
                            }
                            console.log("failed to remove members channel for " + JSON.stringify(removeInfo));
                            callback(new Error("删除组员权限出错, 请稍后重试!"))
                        } else {
                            callback(null, results)
                        }
                    })
                }, function(results, callback){
                    var putUrl = syncGatewayUrl + bucketName + "/" + groupId + "?rev=" + results._rev;
                    //保存修改后的群信息
                    delete results._rev
                    delete results._id
                    requestMethod.requestPut(putUrl, results, function(error, data){
                        //release lock
                        openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                        if(error || data.error){
                            callback(new Error("群操作失败，未成功保存群信息!"))
                        } else {
                            //成功修改群信息
                            callback()
                        }
                    })
                }
            ], function(error, results){
                if(error){
                    res.status(constant.response_code.ok.code).json({error: error.toString()});
                } else {
                    res.status(constant.response_code.ok.code).json({ok:"remove members success!"});
                }
            });
        }
    })
});

/* 增加群成员. */
router.post('/add_members', function(req, res, next) {
    var addInfo = req.body

    if(!addInfo) {
        res.status(constant.response_code.ok).json({error: "请提供要添加的用户信息"})
        return
    }

    if(!addInfo.groupId){
        res.status(constant.response_code.ok).json({error: "请提供要群id"})
        return
    }

    if(!addInfo.add_members || !((addInfo.add_members) instanceof Array)) {
        res.status(constant.response_code.ok).json({error: "请提供要增加的组员信息"})
        return
    }

    var add_members = addInfo.add_members;
    var groupId = addInfo.groupId

    var clusterUrl = config.urls.couchbase_cluster;
    var bucketName = config.buckets.couchbase_server;
    var syncGatewayUrl = config.urls.sync_gateway;
    var openedBucket;

    async.waterfall([
        //获取bucket连接
        function (callback) {
            dbmanager.openBucket(clusterUrl, bucketName, callback);
        },
        //保存变量bucket
        function(bucket, callback) {
            openedBucket = bucket;
            callback()
        }
    ], function(_err, _results){
        if(_err){
            res.status(constant.response_code.ok.code).json({error: error.toString()});
        } else {
            async.waterfall([
                function(callback){
                    //atomic operation, lock the resource
                    openedBucket.get(ATOMIC_PREFIX + groupId, {}, function(err_atomic, res_atomic){
                        if(err_atomic){
                            //增加一个15秒过期的锁文件
                            openedBucket.insert(ATOMIC_PREFIX + groupId, {inProcess:true}, {"expiry":15}, function(err, meta){
                                if(err){
                                    callback(new Error("群操作失败，严重错误，请联系管理员!"));
                                } else {
                                    //lock success
                                    callback()
                                }
                            })
                        } else {
                            callback(new Error("群操作失败，请稍后重试!"));
                        }
                    });
                }, function(callback){
                    var getUrl = syncGatewayUrl + bucketName + "/" + groupId;
                    //查询当前群信息, 并修改
                    requestMethod.requestGet(getUrl, function(error, data){
                        if(error || data.error){
                            //release lock
                            openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                            callback(new Error("群操作失败，未查到群信息!"))
                        } else {
                            var addSuccess = true
                            var jsonData = JSON.parse(data)
                            for(var i=0; i<add_members.length; i++){
                                if(!jsonData.members.contains(add_members[i])){
                                    jsonData.members.push(add_members[i]);
                                } else {
                                    //用户已经在组里了，不能再添加
                                    addSuccess = false
                                    break;
                                }
                            }
                            if(!addSuccess){
                                //release lock
                                openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                                callback(new Error('用户已经在群里了，不能再添加'))
                            } else {
                                callback(null, jsonData)
                            }
                        }
                    })
                }, function(results, callback){
                    var putUrl = syncGatewayUrl + bucketName + "/" + groupId + "?rev=" + results._rev;
                    //保存修改后的群信息
                    delete results._rev
                    delete results._id
                    requestMethod.requestPut(putUrl, results, function(error, data){
                        //release lock
                        openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                        if(error || data.error){
                            callback(new Error("群操作失败，未成功保存群信息!"))
                        } else {
                            //成功修改群信息
                            callback()
                        }
                    })
                }
            ], function(error, results){
                if(error){
                    res.status(constant.response_code.ok.code).json({error: error.toString()});
                } else {
                    res.status(constant.response_code.ok.code).json({ok:"add members success!"});
                }
            });
        }
    })
});

/* 修改群名称. */
router.post('/modify_group_name', function(req, res, next) {
    var modifyInfo = req.body

    if(!modifyInfo) {
        res.status(constant.response_code.ok).json({error: "请提供要要修改的群信息"})
        return
    }

    if(!modifyInfo.groupId){
        res.status(constant.response_code.ok).json({error: "请提供要群id"})
        return
    }

    if(!modifyInfo.new_name) {
        res.status(constant.response_code.ok).json({error: "请提供新的群名称"})
        return
    }

    var newName = modifyInfo.new_name;
    var groupId = modifyInfo.groupId

    var clusterUrl = config.urls.couchbase_cluster;
    var bucketName = config.buckets.couchbase_server;
    var syncGatewayUrl = config.urls.sync_gateway;
    var openedBucket;

    async.waterfall([
        //获取bucket连接
        function (callback) {
            dbmanager.openBucket(clusterUrl, bucketName, callback);
        },
        //保存变量bucket
        function(bucket, callback) {
            openedBucket = bucket;
            callback()
        }
    ], function(_err, _results){
        if(_err){
            res.status(constant.response_code.ok.code).json({error: error.toString()});
        } else {
            async.waterfall([
                function(callback){
                    //atomic operation, lock the resource
                    openedBucket.get(ATOMIC_PREFIX + groupId, {}, function(err_atomic, res_atomic){
                        if(err_atomic){
                            //增加一个15秒过期的锁文件
                            openedBucket.insert(ATOMIC_PREFIX + groupId, {inProcess:true}, {"expiry":15}, function(err, meta){
                                if(err){
                                    callback(new Error("群操作失败，严重错误，请联系管理员!"));
                                } else {
                                    //lock success
                                    callback()
                                }
                            })
                        } else {
                            callback(new Error("群操作失败，请稍后重试!"));
                        }
                    });
                }, function(callback){
                    var getUrl = syncGatewayUrl + bucketName + "/" + groupId;
                    //查询当前群信息, 并修改
                    requestMethod.requestGet(getUrl, function(error, data){
                        if(error || data.error){
                            //release lock
                            openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                            callback(new Error("群操作失败，未查到群信息!"))
                        } else {
                            var jsonData = JSON.parse(data)
                            jsonData.name = newName
                            callback(null, jsonData)
                        }
                    })
                }, function(results, callback){
                    var putUrl = syncGatewayUrl + bucketName + "/" + groupId + "?rev=" + results._rev;
                    //保存修改后的群信息
                    delete results._rev
                    delete results._id
                    requestMethod.requestPut(putUrl, results, function(error, data){
                        //release lock
                        openedBucket.remove(ATOMIC_PREFIX + groupId, function(error, results){})
                        if(error || data.error){
                            callback(new Error("群操作失败，未成功保存群信息!"))
                        } else {
                            //成功修改群信息
                            callback()
                        }
                    })
                }
            ], function(error, results){
                if(error){
                    res.status(constant.response_code.ok.code).json({error: error.toString()});
                } else {
                    res.status(constant.response_code.ok.code).json({ok:"modify group name success!"});
                }
            });
        }
    })
});

module.exports = router;