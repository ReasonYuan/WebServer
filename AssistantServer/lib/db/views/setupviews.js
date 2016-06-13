/**
 * Created by garychen on 11/16/15.
 */
var couchbase = require('couchbase');
var config = require('../../../config');
var designViews = require('./viewsconfig.js');
var logger = require('../../../utils/logger');
var async = require('async');

exports.setUpView = function () {
	var couchbaseUrl = config.urls.couchbase_cluster;
	var couchbaseBucketName = config.buckets.couchbase_server;

	var cluster = new couchbase.Cluster(couchbaseUrl);
	var bucket = cluster.openBucket(couchbaseBucketName, function(err) {
		if (err) {
			logger.error("can not open bucket, couchbase cluster is: " + couchbaseUrl + ", bucket name is: " + couchbaseBucketName);
			return;
		}
		var bucketManager = bucket.manager();

		async.forEachOf(designViews, function (value, designName, callback) {
			var views = designViews[designName]["designdocumentViews"];
			var viewNeedUpdates = {};
			var oldViewNames = [];
			for (var viewKey in views) {
				if (views[viewKey].needUpdate) {
					viewNeedUpdates[views[viewKey].name] = views[viewKey].mapfunc;
					oldViewNames.push(views[viewKey].oldViewname);
				}
			}

			if (Object.keys(viewNeedUpdates).length > 0) {
				bucketManager.getDesignDocument(designName, function (err, ddoc, meta) {
					if (ddoc === null || ddoc.views === undefined) { ddoc = {views:{}}; }
					for (var oldKey in oldViewNames) {
						delete ddoc.views[oldViewNames[oldKey]];
					}
					if (ddoc.views === undefined) { ddoc.views = {}; }

					for (var viewNeedUpdateKey in viewNeedUpdates) {
						ddoc.views[viewNeedUpdateKey] = viewNeedUpdates[viewNeedUpdateKey];
					}

					bucketManager.upsertDesignDocument(designName, ddoc, function (err) {
						callback(err);
					});
				});
			}
		}, function(err) {
			if (err) {
				logger.error("update design document failed, design document name is :" + designName);

			}
		});
	});
};
