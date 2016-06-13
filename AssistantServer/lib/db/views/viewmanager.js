/**
 * Created by garychen on 11/16/15.
 */
var couchbase = require('couchbase');
var logger = require('../../../utils/logger');

function ViewManager() {}

/**
 *
 * @param openBucket bucket连接
 * @param designDocName design document name
 * @param designViewName design view name
 * @param keyStr the key word
 * @param stale query mode, 1: before, 2: none, 3: after
 * @param limitNumber if need limit, number
 * @param callback
 * @constructor
 */
ViewManager.prototype.QueryView = function (openBucket, designDocName, designViewName, keyWord, stale, skipNumber, limitNumber, orderBy, startKey, endKey, reduce, callback) {
	if (!openBucket || !designDocName || !designViewName) {
		logger.error("openedBucket, designDocName, designViewName must provide");
	}
	var viewQuery = couchbase.ViewQuery.from(designDocName, designViewName);

	if (keyWord || keyWord === 0) {
		viewQuery = viewQuery.key(keyWord);
	}

	var queryMode;
	switch (stale) {
		case 1:
			queryMode = couchbase.ViewQuery.Update.BEFORE;
			break;
		case 2:
			queryMode = couchbase.ViewQuery.Update.NONE;
			break;
		case 3:
			queryMode = couchbase.ViewQuery.Update.AFTER;
			break;
		default:
			queryMode = couchbase.ViewQuery.Update.AFTER;
			break;
	}
	viewQuery = viewQuery.stale(queryMode);

	if (startKey && endKey) {
		viewQuery = viewQuery.range(startKey, endKey);
	}

	if(skipNumber && (typeof skipNumber === "number")) {
		viewQuery = viewQuery.skip(skipNumber);
	}

	if(limitNumber && (typeof limitNumber === "number")) {
		viewQuery = viewQuery.limit(limitNumber);
	}

	if(reduce != undefined && (typeof reduce === "boolean")) {
		viewQuery = viewQuery.reduce(reduce)
	}

	if(orderBy && (typeof orderBy === "number")) {
		if (orderBy === 2) {
			viewQuery = viewQuery.order(couchbase.ViewQuery.Order.DESCENDING);
		} else {
			viewQuery = viewQuery.order(couchbase.ViewQuery.Order.ASCENDING);
		}
	}

	openBucket.query(viewQuery, function (err, results) {
		callback(err, results);
	});
};

module.exports = new ViewManager();
