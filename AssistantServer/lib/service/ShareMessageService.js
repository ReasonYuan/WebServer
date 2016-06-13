/**
 * Created by garychen on 12/16/15.
 */
var dbmanager = require('../db/dbmanager');
var config = require('../../config');
var constant = require('../constant/constant');
var async = require('async');

var syncGatewayUrl = config.urls.sync_gateway,
		clusterUrl = config.urls.couchbase_cluster,
		bucketName = config.buckets.couchbase_server;

/**
 * 分页查询用户的动态
 * @param userId
 * @param skip
 * @param limit
 * @param timeOrder
 * @param cb
 */
module.exports.getShareMsgByUserId = function (userId, skip, limit, timeOrder, cb) {
	if (!userId) {
		var error = new Error("please provider user");
		cb(error);
	}
	if (!(cb instanceof Function)) {
		var error = new Error("callback must be func");
		cb(error);
	}
	if (skip == undefined) {
		skip = 0;
	}
	if (limit == undefined) {
		limit = constant.pagination.pageSize;
	}
	if (!timeOrder) {
		timeOrder = constant.viewQuery.orderDesc;
	}

	var openBucket;
	async.waterfall([
		function (callback) {
			dbmanager.openBucket(clusterUrl, bucketName, callback);
		},
		function (bucket, callback) {
			openBucket = bucket;
			var queryStr = 'SELECT meta(share_message).id as id, share_message.* ' +
				             'FROM test share_message ' +
										 'WHERE share_message.type == "' + constant.document_type.ShareMessage + '" ' +
										 'AND share_message._sync IS NOT MISSING ' +
										 'AND share_message.`user` == "' + userId + '" ';
			if (timeOrder == constant.viewQuery.orderDesc) {
				queryStr += 'ORDER BY createAt DESC ';
			}
			queryStr += 'LIMIT ' + limit + ' ';
			queryStr += 'OFFSET ' + skip + ' ';
			dbmanager.N1qlQuery(openBucket, queryStr, callback);
		}
	], function (err, result) {
		cb(err, result);
	});
};