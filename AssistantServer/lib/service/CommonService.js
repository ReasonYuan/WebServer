/**
 * Created by garychen on 12/25/15.
 */
var config = require('../../config');
var requestmethod = require("../../lib/httprequest/requestmethod");
var logger = require("../../utils/logger");
var async = require("async");
var dbmanager = require("../db/dbmanager");
var constant = require("../constant/constant");

var clusterUrl = config.urls.couchbase_cluster,
		bucketName = config.buckets.couchbase_server,
		syncGatewayUrl = config.urls.sync_gateway;

function CommonService() {
	this.clusterUrl = clusterUrl;
	this.syncGatewayUrl = syncGatewayUrl;
	this.bucketName = bucketName;
}

/**
 * 根据id获取document
 * @param docId
 * @param cb
 */
CommonService.prototype.getDocByIdSdk = function (docId, cb) {
	if (!docId) {
		var error = new Error("请指定要查询的document");
		cb(error);
	}
	var self = this;

	async.waterfall([
		function (callback) {
			dbmanager.openBucket(self.clusterUrl, self.bucketName, callback);
		},
		function (bucket, callback) {
			bucket.get(docId, callback);
		}
	], function (err, result) {
		cb(err, result);
	});
}

/**
 * 根据document的type, 及一个字段获取document
 * @param docType document的type
 * @param fieldFilter 某一个字段名, optional
 * @param fieldVal 对应字段的值
 * @param cb
 */
CommonService.prototype.getDocByTypeField = function (docType, fieldFilter, fieldVal, cb) {
	if (!docType) {
		var error = new Error("请指定要查询的document类型.");
		cb(error);
	}
	var self = this;

	async.waterfall([
		function (callback) {
			dbmanager.openBucket(self.clusterUrl, self.bucketName, callback);
		},
		function (bucket, callback) {
			var qureyStr = 'select meta(doc).id, doc.* from ' + self.bucketName + ' doc ' +
				'where doc.type="'+ docType +'" ' +
				'and doc._sync is not missing ';
			if (fieldFilter) {
				if (typeof fieldVal === "number") {
					qureyStr += ' and doc.' + fieldFilter + ' =' + fieldVal;
				} else {
					qureyStr += ' and doc.' + fieldFilter + ' ="' + fieldVal + '"';
				}
			}
			console.log(qureyStr);
			dbmanager.N1qlQuery(bucket,qureyStr, callback);
		}
	], function (err, result) {
		cb(err, result);
	});
}


/**
 * 根据记录类型查询记录, 分页, 指定字段排序
 * @param docType
 * @param skip
 * @param limit
 * @param orderField
 * @param orderMethod
 * @param cb
 */
CommonService.prototype.getDocByType = function (docType, skip, limit, orderField, orderMethod, keywordLikeArr, keywordEqualArr, cb) {
	if (docType == undefined || docType == '' || docType == null) {
		var error = new Error("请指定要查询的记录类型.");
		cb(error);
		return
	}
	var self = this;
	async.waterfall([
		//获取bucket连接
		function (callback) {
			dbmanager.openBucket(self.clusterUrl, self.bucketName, callback);
		},
		function (bucket, callback) {
			var queryStr = 'select meta(doc).id, doc.* from ' + self.bucketName + ' doc ' +
				'where doc.type = "' + docType + '" ' +
				'and doc._sync is not missing ' +
				'and (doc._delete is missing or doc._delete != true ) ' +
				'and (doc.`deleted` is missing or doc.`deleted` != true ) ';

			if (keywordLikeArr) {
				for (var i = 0; i < keywordLikeArr.length; i++) {
					queryStr += 'and doc.`' + keywordLikeArr[i]["field"] + '` like "%' + keywordLikeArr[i]["value"] + '%" ';
				}
			}

			if (keywordEqualArr) {
				for (var i = 0; i < keywordEqualArr.length; i++) {
					var toLower = keywordEqualArr[i]["lower"];
					if(typeof keywordEqualArr[i]["value"] == "number") {
						queryStr += 'and doc.`' + keywordEqualArr[i]["field"] + '` = ' + keywordEqualArr[i]["value"] + ' ';
					} else if (typeof keywordEqualArr[i]["value"] == "boolean") {
						queryStr += 'and doc.`' + keywordEqualArr[i]["field"] + '` = ' + keywordEqualArr[i]["value"] + ' ';
					} else {
						if (toLower) {
							queryStr += 'and LOWER(doc.`' + keywordEqualArr[i]["field"] + '`) = "' + keywordEqualArr[i]["value"] + '" ';
						} else {
							queryStr += 'and doc.`' + keywordEqualArr[i]["field"] + '` = "' + keywordEqualArr[i]["value"] + '" ';
						}
					}
				}
			}

			if (orderField) {
				if (orderMethod == constant.viewQuery.orderDesc) {
					queryStr += 'order by doc.' + orderField + ' DESC ';
				} else {
					queryStr += 'order by doc.' + orderField + ' ASC ';
				}
			}
			if (limit != undefined) {
				queryStr += 'LIMIT ' + (+limit) + ' ';
			}
			if(skip != undefined) {
				queryStr += 'OFFSET ' + (+skip) + ' ';
			}
			console.log(queryStr);

			var pre = new Date().getTime();
			//console.log("time start: " + new Date().getTime());
			dbmanager.N1qlQuery(bucket, queryStr, callback);
			//TODO 换成直接get
			//bucket.get("52f78a68a5dc125855be25394640a212", {}, callback);
			//console.log("spent: " + (new Date().getTime() - pre));

		}
	], function (err, result) {
		cb(err, result);
	});
}


/**
 * 根据id获取document
 * @param docId
 * @param cb
 */
CommonService.prototype.getDocById = function (docId, cb) {
	if (!docId) {
		logger.error("请指定要查询的document.");
		var error = new Error("请指定要查询的document.");
		cb(error);
	}
	if (!(cb instanceof Function)) {
		logger.error("callback必须是Function.");
		var error = new Error("callback必须是Function.");
		cb(error);
	}

	var self = this;
	var docUrl = self.syncGatewayUrl + "/" + self.bucketName + "/" + docId;
	requestmethod.requestGet(docUrl, function (err, result) {
		//如果返回的是字符串, 转换成JSON
		result = typeof result === "string" ? JSON.parse(result) : result;
		if (result.error || err) {
			var error = new Error("查询document信息失败! " + ((err ? err.message || err.toString() : '') || result.error));
			cb(error);
		} else {
			cb(null, result);
		}
	});
}

/**
 * post方式新增记录
 * @param docObj
 * @param cb
 */
CommonService.prototype.addDoc = function (docObj, cb) {
	if (!docObj) {
		logger.error("请指定要添加的记录! ");
		var error = new Error("请指定要添加的记录! ");
		cb(error);
	}
	var self = this;
	var addUrl = self.syncGatewayUrl + "/" + self.bucketName + "/";
	requestmethod.requestPost(addUrl, docObj, cb);
}


/**
 * 根据docId, revision, content更新document
 * @param docId
 * @param rev
 * @param content
 * @param cb
 */
CommonService.prototype.updateDocByIdRevContent = function (docId, rev, content, cb) {
	var self = this;
	if (!docId || !rev || !content) {
		var error = new Error("更新document时,请提供必要的信息: docId, revision, content");
		cb(error);
	}
	var docUpdateUrl = self.syncGatewayUrl + "/" + self.bucketName + "/" + docId + "?rev=" + rev;
	if (content["_id"]) {
		delete content["_id"]
	}
	if (content["_rev"]) {
		delete content["_rev"]
	}
	if (content["id"]) {
		delete content["id"];
	}
	if (content["_sync"]) {
		delete content["_sync"];
	}
	requestmethod.requestPut(docUpdateUrl, content, function (err, result) {
		//如果返回的是字符串, 转换成JSON
		result = typeof result === "string" ? JSON.parse(result) : result;
		if (result.error || err) {
			logger.error("更新document失败! " + ((err ? err.message || err.toString() : '') || result.error));
			var error = new Error("更新document失败! " + ((err ? err.message || err.toString() : '') || result.error));
			cb(error);
		} else {
			cb(null, result);
		}
	});
}


module.exports = new CommonService();
