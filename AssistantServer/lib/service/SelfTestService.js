/**
 * Created by garychen on 12/21/15.
 */
var dbmanager = require('../db/dbmanager');
var config = require('../../config');
var constant = require('../constant/constant');
var async = require('async');
var requestmethod = require('../httprequest/requestmethod');
var logger = require('../../utils/logger');


function SelfTestService() {
	this.clusterUrl = config.urls.couchbase_cluster;
	this.syncGatewayUrl = config.urls.sync_gateway;
	this.bucketName = config.buckets.couchbase_server;
}

/**
 * 根据患者id查询自测
 * @param patientId
 * @param cb
 */
SelfTestService.prototype.getByPatientId = function (patientId, cb) {
	if (!patientId) {
		var error = new Error("请指定要查询的患者.");
		cb(error);
	}
	var self = this;

	async.waterfall([
		//获取bucket连接
		function (callback) {
			dbmanager.openBucket(self.clusterUrl, self.bucketName, callback);
		},
		function (bucket, callback) {
			var queryStr = 'select self_test.* from test self_test ' +
				'where self_test.type="SelfTest" ' +
				'and self_test._sync is not missing ' +
				'and self_test.patient == "' + patientId + '"';
			dbmanager.N1qlQuery(bucket, queryStr, callback);
		}
	], function (err, result) {
		if (err) {
			logger.error(err);
			cb(err);
		} else {
			cb(null, result);
		}
	});
};

module.exports = new SelfTestService();

