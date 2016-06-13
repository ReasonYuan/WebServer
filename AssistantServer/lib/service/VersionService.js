/**
 * Created by garychen on 3/10/16.
 */
var constant = require('../../lib/constant/constant');
var commonService = require('./CommonService');
var logger = require('../../utils/logger');
var async = require('async');

function VersionService() {}

/**
 * 校验版本信息
 * @param roleType app类型
 * @param os app操作系统
 * @param bundleVersion app传过来的版本号
 * @param cb 回调
 */
VersionService.prototype.checkVersion = function (roleType, os, bundleVersion, cb) {
	var resultObj;
	if (roleType === "" || roleType === null || roleType === undefined) {
		logger.error("roleType传值为空");
		resultObj = {
			success: "验证成功, roleType传值为空."
		}
		cb(resultObj);
		return;
	}
	if (os === "" || os === null || os === undefined) {
		logger.error("os传值为空");
		resultObj = {
			success: "验证成功, os传值为空."
		}
		cb(resultObj);
		return;
	}
	if (bundleVersion === "" || bundleVersion === null || bundleVersion === undefined) {
		logger.error("bundleVersion传值为空");
		resultObj = {
			success: "验证成功, bundleVersion传值为空."
		}
		cb(resultObj);
		return;
	}

	//couchbase中查询的版本记录
	var versionObjDB;
	var equalArr = [
		{
			field: "os",
			value: +os
		},
		{
			field: "roleType",
			value: +roleType
		}
	];
	async.waterfall([
		function (callback) {
			commonService.getDocByType(constant.document_type.Version, 0, 1, "date", constant.query.orderBy.desc, null, equalArr, function (err, result) {
				if (err) {
					logger.error("查询出错." + (err.message || err.toString()));
					resultObj = {
						success: "验证成功, 查询出错. " + (err.message || err.toString())
					};
					callback(resultObj);
				} else {
					if (result.length === 0) {
						resultObj = {
							success: "验证成功, 未查到版本信息. "
						};
						callback(resultObj);
					} else {
						versionObjDB = result[0];
						callback();
					}
				}
			});
		},
		//验证版本信息
		function (callback) {
			var minVersion = versionObjDB["minVersion"];
			var clientVersion = versionObjDB["clientVersion"];
			var jsVersion = versionObjDB["jsVersion"];
			var bundleName = versionObjDB["bundleName"];
			//当前版本小于最小可用版本
			if (minVersion && (bundleVersion < minVersion)) {
				console.log(bundleVersion + " < " + minVersion + ": " + (bundleVersion < minVersion));
				resultObj = {
					error: "应用版本过低/应用资源有更新",
					errorCode: constant.versionCheckCode.appNeedReInstall
				};
				callback(resultObj);
				return;
			}
			if (clientVersion && (bundleVersion < clientVersion)) {
				console.log(bundleVersion + " < " + clientVersion + ": " + (bundleVersion < clientVersion));
				resultObj = {
					error: "应用版本过低/应用资源有更新",
					errorCode: constant.versionCheckCode.appCanUpdate
				};
				callback(resultObj);
				return;
			}
			if (jsVersion && (bundleVersion < jsVersion)) {
				console.log(bundleVersion + " < " + jsVersion + ": " + (bundleVersion < jsVersion));
				resultObj = {
					error: "应用版本过低/应用资源有更新",
					errorCode: constant.versionCheckCode.appJsBundleUpdate,
					bundleName: bundleName
				};
				callback(resultObj);
				return;
			}
			resultObj = {
				success: "版本验证成功"
			};
			callback();
		}
	], function (err) {
		cb(resultObj);
	});
}

module.exports = new VersionService();