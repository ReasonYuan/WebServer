/**
 * Created by garychen on 12/15/15.
 */
var requestMethod = require('../httprequest/requestmethod');
var config = require('../../config');
var constant = require('../constant/constant');

var syncGatewayUrl = config.urls.sync_gateway,
		bucketName = config.buckets.couchbase_server;

/**
 * 根据提供的channel,发送普通消息
 * @param from
 * @param msgContent
 * @param channel
 * @param callback
 */
module.exports.sendNormalMsgWithChannel = function (from, msgContent, channel, callback) {
	if (!from) {
		var error = new Error("message provider is required");
		callback(error);
	}
	if (!channel) {
		var error = new Error("channel must provide");
		callback(error);
	}
	if (!(callback instanceof Function)) {
		var error = new Error("callback must provide and should be func");
		callback(error);
	}
	if (!msgContent) {
		msgContent = '';
	}

	var newMessage = {
		channel: channel,
		messageType: constant.message_type.str,
		message: msgContent,
		from: from,
		type: constant.document_type.Message,
		date: new Date().getTime().toString()
	};
	var postMsgUrl = syncGatewayUrl + bucketName + "/";
	requestMethod.requestPost(postMsgUrl, newMessage, callback);
};
