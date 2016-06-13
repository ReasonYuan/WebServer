/**
 * Created by garychen on 3/11/16.
 */
var versionService = require('../service/VersionService');
var constant = require('../constant/constant');
var logger = require('../../utils/logger');

exports.versionCheck = function (req, res, next) {
	var reqUrl = req.url;
	logger.info("check version middleware");
	if(reqUrl === "/users/admin_login" || reqUrl === '/favicon.ico' || reqUrl === '/users/register' || reqUrl === '/'){
		logger.info("no check required!");
		next();
		return
	}

	console.log("reqUrl = " + reqUrl);
	if (constant.versionCheckUrl.exclude.indexOf(reqUrl) === -1) {
		var versionObj = req.body;
		var roleType = versionObj["roleType"];
		var os = versionObj["os"];
		var bundleVersion = versionObj["boundleVersion"];
		versionService.checkVersion(roleType, os, bundleVersion, function(checkResult) {
			console.log(checkResult);
			if (!checkResult["success"]) {
				//返回errorCode为600, app需要升级
				if (checkResult["errorCode"] === constant.versionCheckCode.appNeedReInstall) {
					res.status(constant.response_code.ok.code).json(checkResult);
				} else {
					//如果是所有情况都需要校验的接口,则返回错误.
					if (constant.versionCheckUrl.allCheck.indexOf(reqUrl) === -1) {
						next();
					} else {
						res.status(constant.response_code.ok.code).json(checkResult);
					}
				}
			} else {
				next();
			}
		});
	} else {
		logger.info("该接口不进行统一的版本校验");
		next();
	}
}