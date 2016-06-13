/**
 * Created by garychen on 11/16/15.
 */
var constant = require('../lib/constant/constant');

var Generator = function () {
	this.secutityCodechars = ['0','1','2','3','4','5','6','7','8','9'];
	this.yiyiNumberChars = ['0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
}

/**
 * 生成医家号
 * @param n
 * @returns {string}
 */
Generator.prototype.generateYiyiNumber = function(n) {
	n = n ? n : 6;
	var res = "";
	for (var i = 0; i < n ; i++) {
		var index = Math.ceil(Math.random()*35);
		res += this.yiyiNumberChars[index];
	}
	return res;
};

/**
 * 生成验证码
 * @param n 验证码位数
 */
Generator.prototype.getSecurityCode = function (n) {
	n = n ? n : 6;
	var res = "";
	for (var i = 0; i < n ; i++) {
		var index = Math.ceil(Math.random() * (this.secutityCodechars.length - 1));
		res += this.secutityCodechars[index];
	}
	return res;
};

/**
 * 生成验证码Id
 * @param phoneNumber
 * @param userRole
 * @returns {string}
 */
Generator.prototype.generateSecurityCodeId = function (phoneNumber, userRole) {
	var roleName = this.getRoleName(userRole);
	return "security_code" + "_" + roleName + "_" + phoneNumber;
};

/**
 * 根据用户名和用户类型生成用户id(不是couchbase用，是couchbase里userprofile的id)
 * @param phoneNumber
 * @param userRole
 * @returns {string}
 */
Generator.prototype.generateUserId = function (phoneNumber, userRole) {
	var roleName = this.getRoleName(userRole);
	return roleName + "_" + phoneNumber;
};

/**
 * patient的channel
 * @param patientId
 * @returns {string}
 */
Generator.prototype.getPatientChannelById = function(patientId) {
	return "patients-" + patientId;
};

/**
 * 根据UserProfile的id，取得couchbase user的channel
 * @param ufId
 */
Generator.prototype.getCBUserChannelByUFid = function(ufId) {
	return "user_" + ufId;
}

Generator.prototype.getRecordType = function (typeNumber) {
	var recordType;
	switch (typeNumber) {
		case "出院记录":
			recordType = "出院";
			break;
		case "入院记录":
			recordType = "入院";
			break;
		case "化验记录":
			recordType = "化验";
			break;
		default:
			recordType = "图片";
			break;
	}
	return recordType;
}

Generator.prototype.getRoleName = function (roleType) {
	var roleName;
	switch (roleType) {
		case constant.user_role.doctor:
			roleName = "doctor";
			break;
		case constant.user_role.patient:
			roleName = "patient";
			break;
		case constant.user_role.assistant:
			roleName = "assistant";
			break;
		default:
			roleName = "doctor";
			break;
	}
	return roleName;
}

module.exports = new Generator();