/**
 * Created by garychen on 11/16/15.
 */
var couchbase = require('couchbase');
var async = require('async');
var config = require('../../config');

function DBManager() {
}

DBManager.prototype.dbconnections = {};

DBManager.prototype.openBucket = function(clusterUrl, bucketName, callback) {
	var connKey = clusterUrl + "_" + bucketName;
	var self = this;
	if (!this.dbconnections[connKey]) {
		var cluster = new couchbase.Cluster(clusterUrl);
		var bucketConn = cluster.openBucket(bucketName, function (err, result) {
			if (err) {
				console.error("can not open bucket, cluster: " + clusterUrl + ", bucket: " + bucketName);
				callback(err);
			} else {
				bucketConn.operationTimeout = 60 * 1000; // 60 seconds operation timeout (LCB_CNTL_OP_TIMEOUT)
				self.dbconnections[connKey] = bucketConn;
				callback(null, bucketConn);
			}
		});
	} else {
		callback(null, self.dbconnections[connKey]);
	}
};

/**
 * N1ql查询
 * @param openBucket bucket连接
 * @param queryString 查询语句
 * @param cb 回调
 */
DBManager.prototype.N1qlQuery = function (openBucket, queryString, cb) {
	var N1qlQuery = couchbase.N1qlQuery;
	async.waterfall([
		function (callback) {
			var bucketName = config.buckets.couchbase_server;
			var query = N1qlQuery.fromString('CREATE PRIMARY INDEX ON `'+ bucketName +'`');
			openBucket.query(query, function (err, result) {
				callback(err);
			});
		},
		function (callback) {
			var query = N1qlQuery.fromString(queryString);
			openBucket.query(query, function (err, res) {
				callback(err, res);
			});
		}
	], function (err, result) {
		cb(err, result);
	});
};

module.exports = new DBManager();
