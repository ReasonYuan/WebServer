/**
 * Created by garychen on 12/17/15.
 */
var dbmanager = require('../db/dbmanager');
var config = require('../../config');
var constant = require('../constant/constant');
var async = require('async');
var requestmethod = require('../httprequest/requestmethod');
var logger = require('../../utils/logger');
var arrayUtils = require('../../utils/ArrayUtils');

function SyncUserService() {
	this.syncGatewayUrl = config.urls.sync_gateway;
	this.bucketName = config.buckets.couchbase_server;
}

/**
 * 根据name查询couchbase本身用户
 * @param name
 * @param cb
 */
SyncUserService.prototype.getSyncUser = function (name, cb) {
	if (!name) {
		var error = new Error("请指定要查询的用户.");
		cb(error);
	}
	var usrUrl = this.syncGatewayUrl + this.bucketName + "/_user/" + name;
	requestmethod.requestGet(usrUrl, function (err, result) {
		//返回的是字符串, 转换成JSON
		result = typeof result === "string" ? JSON.parse(result) : result;
		if (result.error || err) {
			logger.error("查询用户信息失败!" + ((err ? err.message || err.toString() : '') || result.error));
			var error = new Error("查询用户信息失败!" + ((err ? err.message || err.toString() : '') || result.error));
			cb(error);
		} else {
			cb(null, result);
		}
	});
};

/**
 * 给用户添加channel
 * @param name
 * @param channel
 */
SyncUserService.prototype.addUserAdminChannels = function (name, addChannels, cb) {
	var self = this;
	if (!addChannels || !(addChannels instanceof Array) || (addChannels.length === 0)) {
		var error = new Error("请指定要添加的channel");
		cb(error);
	}
	//couchbase用户信息
	var userDoc;
	async.waterfall([
		//查询用户信息
		function (callback) {
			self.getSyncUser(name, function (err, result) {
				if (err) {
					callback(err);
				} else {
					userDoc = result;
					callback();
				}
			})
		},
		//添加用户channel
		function (callback) {
			var usrUrl = self.syncGatewayUrl + self.bucketName + "/_user/" + name;
			//当前用户admin_channels
			var adminChannels = userDoc["admin_channels"] === undefined ? [] : userDoc["admin_channels"];
			if (adminChannels) {
				for(var i = 0; i < addChannels.length; i++) {
					if(adminChannels.indexOf(addChannels[i]) === -1) {
						adminChannels.push(addChannels[i]);
					}
				}
			} else {
				adminChannels["admin_channels"] = addChannels;
			}
			userDoc["admin_channels"] = adminChannels;
			requestmethod.requestPut(usrUrl, userDoc, callback);
		}
	], function (err, result) {
		if (err) {
			logger.error("添加用户channel失败! ");
			var error = new Error("添加用户channel失败!  " + (err.message || err.toString()));
			cb(error);
		} else {
			cb(null, result);
		}
	});
};

/**
 * 移除用户channel
 * @param name
 * @param channels
 * @param cb
 */
SyncUserService.prototype.removeUserChannels = function (name, removeChannels, cb) {
	var self = this;
	if (!removeChannels || !(removeChannels instanceof Array) || (removeChannels.length === 0)) {
		logger.error("请指定要移除的channel.");
		var error = new Error("请指定要移除的channel.");
		cb(error);
	}
	//couchbase用户信息
	var userDoc;
	async.waterfall([
		//查询用户信息
		function (callback) {
			self.getSyncUser(name, function (err, result) {
				if (err) {
					callback(err);
				} else {
					userDoc = result;
					callback();
				}
			})
		},
		//添加用户channel
		function (callback) {
			var usrUrl = self.syncGatewayUrl + self.bucketName + "/_user/" + name;
			//当前用户admin_channels
			var adminChannels = userDoc["admin_channels"] === undefined ? [] : userDoc["admin_channels"];
			if (adminChannels) {
				for(var i = 0; i < removeChannels.length; i++) {
					adminChannels = arrayUtils.removeByValue(adminChannels, removeChannels[i]);
				}
			}
			userDoc["admin_channels"] = adminChannels;
			requestmethod.requestPut(usrUrl, userDoc, callback);
		}
	], function (err, result) {
		if (err) {
			logger.error("移除用户channel失败! ");
			var error = new Error("移除用户channel失败!  " + (err.message || err.toString()));
			cb(error);
		} else {
			cb(null, result);
		}
	});
};

module.exports = new SyncUserService();