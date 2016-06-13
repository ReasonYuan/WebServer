
var express = require('express');
var generator = require('../utils/generator');
var stringUtils = require('../utils/StringUtils');
var constant = require('../lib/constant/constant');
var logger = require('../utils/logger');
var async = require('async');
var dbmanager = require('../lib/db/dbmanager');
var viewmanager = require('../lib/db/views/viewmanager');
var viewconfig = require('../lib/db/views/viewsconfig');
var config = require('../config');
var requestMethod = require("../lib/httprequest/requestmethod");

var router = express.Router();

Array.prototype.insert = function (index, item) {
  this.splice(index, 0, item);
};

/* GET users listing. */
router.get('/', function(req, res, next) {
  res.send('respond with a resource');
});

/**
 * 用户登录,
 * TODO 目前拷贝自BaoshiServer，以后建中央登陆服务器，redirect到登录服务器
 */
router.post("/register", function (req, res) {
  var user = req.body;
  if (!user) {
    res.status(constant.response_code.ok.code).json({error: "请提供注册信息"});
    return;
  }
  if (!user.phone_number) {
    res.status(constant.response_code.ok.code).json({error: "请提供手机号"});
    return;
  }
  var fromAdmin = user.fromAdmin
  if(fromAdmin){
    user.role_type = parseInt(user.role_type)
    if(!user.name){
      res.status(constant.response_code.ok.code).json({error: "请提供姓名"});
      return;
    }
  }
  if (!user.password) {
    res.status(constant.response_code.ok.code).json({error: "请提供密码"});
    return;
  }
  if (user.role_type === undefined) {
    res.status(constant.response_code.ok.code).json({error: "please provide your register role"});
    return;
  }



  var name = ""
  if(user.name){
    name = user.name
  }


  var assistant = null
  if(user.assistant){
    assistant = user.assistant
  }

  var clusterUrl = config.urls.couchbase_cluster;
  var bucketName = config.buckets.couchbase_server;
  var syncGatewayUrl = config.urls.sync_gateway;
  var openedBucket;
  var session;
  //医家号
  var yiyiNumber;
  //助手id
  var assistantId = null;
  //根据用户类型及手机号生成用户id
  var userId = generator.generateUserId(user.phone_number, user.role_type);

  //It is always good practice to return callback(err, result) whenever a callback call is not the last statement of a function.
  async.waterfall([
    //获取bucket连接
    function (callback) {
      dbmanager.openBucket(clusterUrl, bucketName, callback);
    },
    //查询现有用户，根据生成的用户id
    function(bucket, callback) {
      openedBucket = bucket;
      openedBucket.get(userId, function (err, resRead) {
        if (err) {
          //未查到当前用户
          if (err.code === 13) {
            callback();
          } else {//其他错误
            callback(err);
          }
        } else {//无错误信息，查到用户
          var error = new Error("该用户已存在");
          callback(error);
        }
      });
    },
    //取系统中依依号，生成不存在的依依号
    function(callback) {
      var putUserUrl = syncGatewayUrl + bucketName + "/" + userId;
      requestMethod.requestPut(putUserUrl, {name: name, phone_number: user.phone_number, password: user.password, role_type: user.role_type, type:"User", assistant:assistant}, function (err, result) {
        if (err) {
          var error = new Error("create user failed. " + (err.message || err.toString()));
          callback(error);
        } else {
          if (result && result.error) {
            var error = new Error("create user failed. " + (result.reason || result.toString()));
            callback(error);
          } else {
            callback();
          }
        }
      })
    },
    //创建couchbase的用户，name用userId
    function (callback) {
      var postUserUrl = syncGatewayUrl + bucketName + "/_user/";
      requestMethod.requestPost(postUserUrl, {name: userId, password: user.password}, function (err, result) {
        if (err) {
          var error = new Error("create couchbase user failed. " + (err.message || err.toString()));
          callback(error);
        } else {
          if (result && result.error) {
            var error = new Error("create couchbase user failed. " + (result.reason || result.toString()));
            callback(error);
          } else {
            callback();
          }
        }
      });
    },
    //获取用户session
    function (calllback) {
      var userLookUpUrl = syncGatewayUrl + bucketName + "/_session";
      requestMethod.requestPost(userLookUpUrl, {name: userId}, function (err, result) {
        if (err) {
          var error = new Error("get user session failed, please login again. " + (err.message || err.toString()));
          calllback(error);
        } else {
          if (!result || !result["session_id"]) {
            var error = new Error("get user session failed, please login again.");
            calllback(error);
          } else {
            session = result;
            calllback();
          }
        }
      });
    }
  ], function (err, result){
    if (err) {
      logger.error(err);
      if(fromAdmin){
        res.render('manage_user', { flashMsg: '创建失败，' + (err.message || err.toString()), flashTitle:"Opps: " });
      } else {
        res.status(constant.response_code.ok.code).json({error: err.message || err.toString()});
      }
    } else {
      if(fromAdmin){
        res.render('manage_user', { flashMsg: '创建成功，您可以继续创建下一个用户...', flashTitle:"恭喜: " });
      } else {
        res.status(constant.response_code.ok.code).json(session);
      }
    }
    return;
  });

});

/**
 * 用户登录,
 * TODO 目前拷贝自BaoshiServer，以后建中央登陆服务器，redirect到登录服务器
 */
router.post("/login", function (req, res) {
  var user = req.body;
  if (!user) {
    res.status(constant.response_code.ok.code).json({error: "请提供登录信息"});
    return;
  }
  if (!user.phone_number) {
    res.status(constant.response_code.ok.code).json({error: "请提供手机号"});
    return;
  }
  if (!user.password) {
    res.status(constant.response_code.ok.code).json({error: "请提供密码"});
    return;
  }
  if(user.role_type === undefined) {
    res.status(constant.response_code.ok.code).json({error: "请提供登录角色"});
    return;
  }

  var openedBucket;
  var userInfo;
  var session;
  //根据用户类型及手机号获得用户信息的id，也是couchbase本身用户的name
  var userId = generator.generateUserId(user["phone_number"], user["role_type"]);
  var couchbaseCluster = config.urls.couchbase_cluster || "http://127.0.0.1/";
  var bucketName = config.buckets.couchbase_server || "test";
  var syncGatewayUrl = config.urls.sync_gateway || "http://127.0.0.1:4985";
  async.waterfall([
    //获取bucket连接
    function (callback) {
      dbmanager.openBucket(couchbaseCluster, bucketName, callback);
    },
    //查询view获得当前用户信息
    function (openBucket, callback) {
      openedBucket = openBucket;
      openedBucket.get(userId, function (err, resRead) {
        if (err) {
          //未查到当前用户
          callback(new Error("登录失败，未找到用户信息"));
          logger.error(err)
        } else {//无错误信息，查到用户
          resRead.value['id'] = userId
          callback(null, resRead);
        }
      });
    },
    //获得用户信息, 校验用户信息
    function(searchResult, callback) {
      if(searchResult.value.password == user.password){
        callback(null, searchResult.value)
      } else {
        var error = new Error("用户验证失败");
        callback(error)
      }
    },
    //获取用户session
    function (userFind, callback) {
      if (userFind["id"]) {
        userInfo = userFind
        var subUrl = config.urls.sync_gateway_urls.get_session;
        var jsonData = { name: userFind["id"]};
        requestMethod.requestPost(syncGatewayUrl + bucketName + subUrl, jsonData, callback);
      } else {
        var error = new Error("未找到用户ID");
        callback(error);
      }
    },
    //处理用户session
    function (sessionRes, callback) {
      if (typeof sessionRes === "string") {
        sessionRes = JSON.stringify(sessionRes);
      }

      if (!sessionRes || (sessionRes["error"])) {
        var error = new Error("获取登录会话失败");
        callback(error);
      } else {
        callback(null, sessionRes);
      }
    }
  ],function (err, results) {
    if (err) {
      logger.error(err);
      res.status(constant.response_code.ok.code).json({error: (err.message || err.toString())});
    } else {
      delete userInfo._sync
      results.userInfo = userInfo
      res.status(constant.response_code.ok.code).json(results);
    }
    return;
  });
});


//查询客户的所有病案, 用view查询
router.post('/list_user_patients', function(req, res, next) {
  var user = req.body;
  if (!user) {
    res.status(constant.response_code.ok.code).json({error: "need provide user info"});
    return;
  }
  if (!user.userid) {
    res.status(constant.response_code.ok.code).json({error: "please provide user id"});
    return;
  }
  var skip = 0
  var limit = null
  if(user.pageSize){
    limit = user.pageSize
    if(user.page != undefined && user.page >= 0){
      skip = user.page * user.pageSize
    }
  }

  var userId = user.userid
  var bucketName = config.buckets.couchbase_server || "test";
  var couchbaseCluster = config.urls.couchbase_cluster || "http://127.0.0.1/";
  var syncGatewayUrl = config.urls.sync_gateway || "http://127.0.0.1:4985";
  var openedBucket


  async.waterfall([
    //获取bucket连接
    function (callback) {
      dbmanager.openBucket(couchbaseCluster, bucketName, callback);
    },
    //查询view获得当前用户信息
    function (openBucket, callback) {
      openedBucket = openBucket;
      viewmanager.QueryView(openedBucket, viewconfig.patient.designdocumentName, viewconfig.patient.designdocumentViews.patient_by_userId.name, userId, constant.viewQuery.updateBefore, skip, limit, null, null, null, false, function(error, results){
        callback(error, results);
      })
    }],
      function(err, results){
        if (err) {
          logger.error(err);
          res.status(constant.response_code.ok.code).json({error: (err.message || err.toString())});
        } else {
          var returnValues = []
          for(var i=0; i<results.length; i++){
            delete results[i].value._sync
            if(results[i].value.relationship == "我"){
              returnValues.insert(0, results[i])
            } else {
              returnValues.push(results[i])
            }
          }
          res.status(constant.response_code.ok.code).json(returnValues);
        }
        return
      })
})

//查询病案下的所有记录, 用view查询
router.post('/list_patient_records', function(req, res, next) {
  var patient = req.body;
  if (!patient) {
    res.status(constant.response_code.ok.code).json({error: "need provide patient info"});
    return;
  }
  if (!patient.patientid) {
    res.status(constant.response_code.ok.code).json({error: "please provide patient id"});
    return;
  }
  var skip = 0
  var limit = null
  if(patient.pageSize){
    limit = patient.pageSize
    if(patient.page != undefined && patient.page >= 0){
      skip = patient.page * patient.pageSize
    }
  }

  var patientId = patient.patientid
  var bucketName = config.buckets.couchbase_server || "test";
  var couchbaseCluster = config.urls.couchbase_cluster || "http://127.0.0.1/";
  var openedBucket

  async.waterfall([
        //获取bucket连接
        function (callback) {
          dbmanager.openBucket(couchbaseCluster, bucketName, callback);
        },
        //查询view获得当前用户信息
        function (openBucket, callback) {
          openedBucket = openBucket;
          viewmanager.QueryView(openedBucket, viewconfig.record.designdocumentName, viewconfig.record.designdocumentViews.record_by_patientId.name, patientId, constant.viewQuery.updateBefore, skip, limit, constant.viewQuery.orderDesc, null, null, false, function(error, results){
            callback(error, results);
          })
        }],
      function(err, results){
        if (err) {
          logger.error(err);
          res.status(constant.response_code.ok.code).json({error: (err.message || err.toString())});
        } else {
          for(var i=0; i<results.length; i++){
            delete results[i].value._sync
          }
          res.status(constant.response_code.ok.code).json(results);
        }
        return
      })
})

//查询助理的医生朋友, 用view查询
router.post('/list_assistant_doctor_friends', function(req, res, next) {
  var assistant = req.body;
  if (!assistant) {
    res.status(constant.response_code.ok.code).json({error: "需要提供助理信息"});
    return;
  }
  if (!assistant.userid) {
    res.status(constant.response_code.ok.code).json({error: "请提供助理的id"});
    return;
  }
  var skip = 0
  var limit = null
  if(assistant.pageSize){
    limit = assistant.pageSize
    if(assistant.page != undefined && assistant.page >= 0){
      skip = assistant.page * assistant.pageSize
    }
  }

  var userid = assistant.userid
  var bucketName = config.buckets.couchbase_server || "test";
  var couchbaseCluster = config.urls.couchbase_cluster || "http://127.0.0.1/";
  var openedBucket

  async.waterfall([
        //获取bucket连接
        function (callback) {
          dbmanager.openBucket(couchbaseCluster, bucketName, callback);
        },
        //查询view获得当前用户信息
        function (openBucket, callback) {
          openedBucket = openBucket;
          viewmanager.QueryView(openedBucket, viewconfig.user.designdocumentName, viewconfig.user.designdocumentViews.doctor_friends_by_assistantId.name, userid, constant.viewQuery.updateBefore, skip, limit, constant.viewQuery.orderAsc, null, null, false, function(error, results){
            callback(error, results);
          })
        }],
      function(err, results){
        if (err) {
          logger.error(err);
          res.status(constant.response_code.ok.code).json({error: (err.message || err.toString())});
        } else {
          for(var i=0; i<results.length; i++){
            delete results[i].value._sync
          }
          res.status(constant.response_code.ok.code).json(results);
        }
        return
      })
})


//查询病案下的所有账单
router.post('/list_patient_bills', function(req, res, next) {
  var patient = req.body;
  if (!patient) {
    res.status(constant.response_code.ok.code).json({error: "need provide patient info"});
    return;
  }
  if (!patient.patientid) {
    res.status(constant.response_code.ok.code).json({error: "please provide patient id"});
    return;
  }
  var skip = 0
  var limit = null
  if(patient.pageSize){
    limit = patient.pageSize
    if(patient.page != undefined && patient.page >= 0){
      skip = patient.page * patient.pageSize
    }
  }

  var patientId = patient.patientid
  var bucketName = config.buckets.couchbase_server || "test";
  var couchbaseCluster = config.urls.couchbase_cluster || "http://127.0.0.1/";
  var openedBucket
  //http://stackoverflow.com/questions/13950689/couchbase-multiple-keys
  var endKey = [patientId, " "]
  var startKey = [patientId, "z"]

  async.waterfall([
        //获取bucket连接
        function (callback) {
          dbmanager.openBucket(couchbaseCluster, bucketName, callback);
        },
        //查询view获得当前用户信息
        function (openBucket, callback) {
          openedBucket = openBucket;
          viewmanager.QueryView(openedBucket, viewconfig.service.designdocumentName, viewconfig.service.designdocumentViews.service_by_patientId.name, null, constant.viewQuery.updateBefore, skip, limit, constant.viewQuery.orderDesc, startKey, endKey, false, function(error, results){
            callback(error, results);
          })
        }],
      function(err, results){
        if (err) {
          logger.error(err);
          res.status(constant.response_code.ok.code).json({error: (err.message || err.toString())});
        } else {
          for(var i=0; i<results.length; i++){
            delete results[i].value._sync
          }
          res.status(constant.response_code.ok.code).json(results);
        }
        return
      })
})

//查询医生的账单
router.post('/list_doctor_bills', function(req, res, next) {
  var doctor = req.body;
  if (!doctor) {
    res.status(constant.response_code.ok.code).json({error: "need provide doctor info"});
    return;
  }
  if (!doctor.doctorid) {
    res.status(constant.response_code.ok.code).json({error: "please provide doctor id"});
    return;
  }
  var skip = 0
  var limit = null
  if(doctor.pageSize){
    limit = doctor.pageSize
    if(doctor.page != undefined && doctor.page >= 0){
      skip = doctor.page * doctor.pageSize
    }
  }

  var doctorId = doctor.doctorid
  var bucketName = config.buckets.couchbase_server || "test";
  var couchbaseCluster = config.urls.couchbase_cluster || "http://127.0.0.1/";
  var openedBucket
  //http://stackoverflow.com/questions/13950689/couchbase-multiple-keys
  var endKey = [doctorId, " "]
  var startKey = [doctorId, "z"]
  var mapResults = null
  var reduceResults = null

  async.waterfall([
        //获取bucket连接
        function (callback) {
          dbmanager.openBucket(couchbaseCluster, bucketName, callback);
        },
        //查询view获得当前用户信息
        function (openBucket, callback) {
          openedBucket = openBucket;
          viewmanager.QueryView(openedBucket, viewconfig.service.designdocumentName, viewconfig.service.designdocumentViews.service_by_doctorId.name, null, constant.viewQuery.updateBefore, skip, limit, constant.viewQuery.orderDesc, startKey, endKey, false, function(error, results){
            callback(error, results);
          })
        },
        function (results, callback) {
          mapResults = results
          viewmanager.QueryView(openedBucket, viewconfig.service.designdocumentName, viewconfig.service.designdocumentViews.service_by_doctorId.name, null, constant.viewQuery.updateBefore, skip, limit, constant.viewQuery.orderDesc, startKey, endKey, true, function(error, results){
            callback(error, results);
          })
        }],
      function(err, results){
        if (err) {
          logger.error(err);
          res.status(constant.response_code.ok.code).json({error: (err.message || err.toString())});
        } else {
          reduceResults = results
          for(var i=0; i<mapResults.length; i++){
            delete mapResults[i].value._sync
          }
          res.status(constant.response_code.ok.code).json({results:mapResults, totalPrice:reduceResults[0] ? reduceResults[0].value : 0});
        }
        return
      })
})

//查询用户信息
router.post('/get_user_info', function(req, res, next) {
  var user = req.body;
  if (!user) {
    res.status(constant.response_code.ok.code).json({error: "need provide user info"});
    return;
  }
  if (!user.userid) {
    res.status(constant.response_code.ok.code).json({error: "please provide user id"});
    return;
  }
  var openedBucket;
  var userId = user.userid
  var couchbaseCluster = config.urls.couchbase_cluster || "http://127.0.0.1/";
  var bucketName = config.buckets.couchbase_server || "test";
  var syncGatewayUrl = config.urls.sync_gateway || "http://127.0.0.1:4985";
  async.waterfall([
    //获取bucket连接
    function (callback) {
      dbmanager.openBucket(couchbaseCluster, bucketName, callback);
    },
    //查询view获得当前用户信息
    function (openBucket, callback) {
      openedBucket = openBucket;
      openedBucket.get(userId, function (err, resRead) {
        if (err) {
          //未查到当前用户
          callback(new Error("user not found, due to " + err.message));
        } else {//无错误信息，查到用户
          resRead.value['id'] = userId
          callback(null, resRead);
        }
      });
    }], function (err, results) {
    if (err) {
      logger.error(err);
      res.status(constant.response_code.ok.code).json({error: (err.message || err.toString())});
    } else {
      delete results.value._sync
      res.status(constant.response_code.ok.code).json(results.value);
    }
    return;
  });
});

//后台管理员登陆
router.post('/admin_login', function(req, res, next){
  var user = req.body;
  if (!user) {
    res.status(constant.response_code.ok.code).json({error: "need provide login info"});
    return;
  }
  if (!user.userid) {
    res.status(constant.response_code.ok.code).json({error: "please provide your user id"});
    return;
  }
  if (!user.password) {
    res.status(constant.response_code.ok.code).json({error: "please provide your password"});
    return;
  }

  if(user.userid == "admin" && user.password == "Yiyihealth"){
    //pass
    sessionInfo[req.sessionID] = "admin"
  }

  res.render('manage_user', { title: '登陆成功' });
});


module.exports = router;


//======================下面均是临时代码======================
//以下是临时用于生成测试数据的代码
//生成模拟账单
router.post('/create_fake_bills', function(req, res, next) {

  for(var i=0; i<3; i++){
    for(var j=0; j<10; j++){
      var patient = "patient_test_" + i + "_" + j;

      requestMethod.requestPost("http://localhost:13100/users/list_user_patients", {userid:patient}, function(error, data){

        if(!error){
          //data = JSON.parse(data)
          //data = data.rows
          for(var pi =0; pi<data.length; pi++){
            createBill4OnePatient(data[pi])
          }
        }

      })

      if(j == 9 && i == 2){
        res.status(constant.response_code.ok.code).json({ok:true});
      }
    }
  }

});

function createBill4OnePatient(patientInfo){
  var billCnt = getRandom(10)
  for(var n =0; n<billCnt; n++){
    var ser = {} //new Service("channlllllll", "fromsome", "tosome")
    //ser.save()
    ser.channel = "some channel"
    ser.name = "服务内容, 服务内容, 服务内容, 服务内容, 服务内容, 服务内容, 服务内容, 服务内容, 服务内容, 服务内容: " + getRandom(1000000)
    ser.from = "don't know" //谁发的服务，助手的id
    ser.to = "don't know"   //给谁加的服务
    ser.price = 499*(getRandom(5) + 1) //价钱
    ser.judgment = "这个评价" //服务的评价
    ser.rating = 2 //评分
    ser.payStatus = 3 //付费的状态，3表示付费成功
    ser.group=null   //是否是群服务，和医生聊天
    ser.needPay=1 //是否需要支付
    ser.patient = patientInfo.id //这个服务属于哪一个病案
    ser.type = "Service"
    ser.messageType = 4
    var ONEDAY = 24*60*60*1000
    var ONEHOUR = 60*60*1000
    var ONEMINUTE = 60*1000
    var newSeconds = new Date().getTime()
    ser.date = newSeconds - (getRandom(1000)*ONEDAY) - getRandom(24)*ONEHOUR - (getRandom(60)*ONEMINUTE)

    //this.sendPostRequest("http://115.29.229.128:4985/test/", "POST", function(data){
    //  console.log(data);
    //})

    requestMethod.requestPost("http://localhost:14985/test/", ser, function(error, data){
      console.log("error: " + error + ", data: " + JSON.stringify(data))
    })
  }
}

function getRandom(max){
  var res = max
  while(res == max){
    res = parseInt(max*Math.random())
  }
  return res
}


//以下是临时用于生成测试数据的代码
//生成模拟账单
router.post('/delete_all_services', function(req, res, next) {

  requestMethod.requestGet("http://localhost:4985/test/_design/_all_services/_view/_all_services", function(error, data){

    if(!error){
      var pdata = JSON.parse(data).rows
      console.log("len = " + pdata.length)
      for(var pi =0; pi<pdata.length; pi++){
        requestMethod.requestGet("http://localhost:4985/test/" + pdata[pi].id, function(error, data){
          if(!error){
            data = JSON.parse(data)
            requestMethod.requestDelete("http://localhost:4985/test/" + data._id + "?rev=" + data._rev, {}, function(error, data){
              console.log("deleted!!!")
            })
          }
        })
      }
    }

  })

  res.status(constant.response_code.ok.code).json({ok:true});

});

//以下是临时用于生成测试数据的代码
//随机修改病历类型
router.post('/random_change_record_types', function(req, res, next) {

  requestMethod.requestGet("http://localhost:4985/test/_design/_all_records/_view/_all_records", function(error, data){

    if(!error){
      var pdata = JSON.parse(data).rows
      //console.log("len = " + pdata.length)
      for(var pi =0; pi<pdata.length; pi++){
        requestMethod.requestGet("http://localhost:4985/test/" + pdata[pi].id, function(error, data){
          if(!error){
            data = JSON.parse(data)
            var id = data._id
            var rev = data._rev
            var recordTypes = ["体检报告", "健康自测", "影像"]
            var type = recordTypes[getRandom(3)]
            data.info = {"出院诊断":"三闾大夫距离是的肌肤洛杉矶的翻领设计的，病。。。。。。。。。。。。"}
            data.record_type = type
            requestMethod.requestPut("http://localhost:4985/test/" + id + "?rev=" + rev, data, function(error, data){
              console.log("updated!!!")
            })
          }
        })
      }
    }

  })

  res.status(constant.response_code.ok.code).json({ok:true});

});



//查询客户的所有病案, 用view查询，第一次要新建view
router.post('/list_user_patients_by_user', function(req, res, next) {
  var user = req.body;
  if (!user) {
    res.status(constant.response_code.ok.code).json({error: "need provide user info"});
    return;
  }
  if (!user.userid) {
    res.status(constant.response_code.ok.code).json({error: "please provide user id"});
    return;
  }
  var userId = user.userid
  var bucketName = config.buckets.couchbase_server || "test";
  var syncGatewayUrl = config.urls.sync_gateway || "http://127.0.0.1:4985";
  var viewName = "search_patients_by_user_" + userId
  var viewUrl = syncGatewayUrl + bucketName + "/_design/" + viewName + "/_view/" + viewName;
  requestMethod.requestGet(viewUrl, function(error, data){
    data = JSON.parse(data)
    if(error || data && data.error){
      //error
      if(data.error){
        var viewCreateUrl = syncGatewayUrl + bucketName + "/_design/" + viewName
        var postData = {views:{}}
        var map = {map:"function (doc, meta) { if (doc.type == 'Patient' && doc.user == '" + userId + "')  emit(doc._id, doc); }"}
        postData.views[viewName] = map
        requestMethod.requestPut(viewCreateUrl,
            postData,
            function(error, data){
              if(error || data){
                res.status(constant.response_code.ok.code).json({error: "create data view failed!!!"});
              } else {
                tryQueryViewLater(5, viewUrl, res, 1000)
              }
            })
      } else {
        res.status(constant.response_code.ok.code).json({error: error});
      }
    } else {
      //send data back
      res.status(constant.response_code.ok.code).json(data);
    }
  });
});


//查询病案下的所有记录
router.post('/list_patient_records_by_patient', function(req, res, next) {
  var patient = req.body;
  if (!patient) {
    res.status(constant.response_code.ok.code).json({error: "need provide patient info"});
    return;
  }
  if (!patient.patientid) {
    res.status(constant.response_code.ok.code).json({error: "please provide patient id"});
    return;
  }
  var patientId = patient.patientid
  var bucketName = config.buckets.couchbase_server || "test";
  var syncGatewayUrl = config.urls.sync_gateway || "http://127.0.0.1:4985";
  var viewName = "search_records_by_patient_" + patientId
  var viewUrl = syncGatewayUrl + bucketName + "/_design/" + viewName + "/_view/" + viewName;
  requestMethod.requestGet(viewUrl, function(error, data){
    data = JSON.parse(data)
    if(error || data && data.error){
      //error
      if(data.error){
        var viewCreateUrl = syncGatewayUrl + bucketName + "/_design/" + viewName
        var postData = {views:{}}
        var map = {map:"function (doc, meta) { if (doc.type == 'Record' && doc.patient == '" + patientId + "')  emit(doc._id, doc); }"}
        postData.views[viewName] = map
        requestMethod.requestPut(viewCreateUrl,
            postData,
            function(error, data){
              if(error || data){
                res.status(constant.response_code.ok.code).json({error: "create data view failed!!!"});
              } else {
                tryQueryViewLater(5, viewUrl, res, 1000)
              }
            })
      } else {
        res.status(constant.response_code.ok.code).json({error: error});
      }
    } else {
      //send data back
      res.status(constant.response_code.ok.code).json(data);
    }
  });
});

function stringStartsWith (string, prefix) {
  return string.slice(0, prefix.length) == prefix;
}

//查询病案下的所有账单
router.post('/list_patient_bills_by_patient', function(req, res, next) {
  var patient = req.body;
  if (!patient) {
    res.status(constant.response_code.ok.code).json({error: "need provide patient info"});
    return;
  }
  if (!patient.patientid) {
    res.status(constant.response_code.ok.code).json({error: "please provide patient id"});
    return;
  }
  var patientId = patient.patientid
  var bucketName = config.buckets.couchbase_server || "test";
  var syncGatewayUrl = config.urls.sync_gateway || "http://127.0.0.1:4985";
  var viewName = "search_patient_bill_by_id_" + patientId
  var viewUrl = syncGatewayUrl + bucketName + "/_design/" + viewName + "/_view/" + viewName;
  requestMethod.requestGet(viewUrl, function(error, data){
    data = JSON.parse(data)
    if(error || data && data.error){
      //error
      if(data.error){
        var viewCreateUrl = syncGatewayUrl + bucketName + "/_design/" + viewName
        var postData = {views:{}}
        //pay status == 3 means paid
        var map = {map:"function (doc, meta) { if (doc.type == 'Service' && doc.patient == '" + patientId + "' && doc.payStatus == 3)  emit(doc.date, doc); }"}
        postData.views[viewName] = map
        requestMethod.requestPut(viewCreateUrl,
            postData,
            function(error, data){
              if(error || data){
                res.status(constant.response_code.ok.code).json({error: "create data view failed!!!"});
              } else {
                tryQueryViewLater(5, viewUrl, res, 1000)
              }
            })
      } else {
        res.status(constant.response_code.ok.code).json({error: error});
      }
    } else {
      //send data back
      res.status(constant.response_code.ok.code).json(data);
    }
  });
});

//TODO 加webhook来创建view
//第一次创建view需要一定时间，稍作延迟处理
function tryQueryViewLater(tryTimes, viewUrl, res, waitTime){
  setTimeout(function(){
    requestMethod.requestGet(viewUrl, function(error, data){
      data = JSON.parse(data)
      if(error){
        var remainTimes = tryTimes - 1
        if(remainTimes > 0){
          tryQueryViewLater(remainTimes, viewUrl, res, waitTime)
        } else {
          res.status(constant.response_code.ok.code).json({error:error});
        }
      } else {
        if(data.total_rows == 0 && tryTimes > 1){
          tryQueryViewLater(1, viewUrl, res, waitTime*4)
        } else {
          res.status(constant.response_code.ok.code).json(data);
        }
      }
    })
  }, waitTime)
}
