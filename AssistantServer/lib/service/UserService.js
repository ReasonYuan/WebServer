/**
 * Created by garychen on 12/16/15.
 */
var dbmanager = require('../db/dbmanager');
var config = require('../../config');
var constant = require('../constant/constant');
var async = require('async');

var clusterUrl = config.urls.couchbase_cluster,
		bucketName = config.buckets.couchbase_server;

function UserService() {
	this.clusterUrl = clusterUrl;
	this.bucketName = bucketName;
}
/**
 *根据用户类型查询用户
 * @param userType: 用户类型
 * @param skip: 第几个开始
 * @param limit: 查询几个
 * @param orderField: 根据哪个字段排序
 * @param orderMethod: 排序方式ASC/DESC
 * @param cb
 */
UserService.prototype.findUserByType = function (userRole, skip, limit, orderField, orderMethod, cb) {
	if (userRole == undefined) {
		var error = new Error("请指定要查询的用户类型.");
		cb(error);
	}
	var self = this;
	async.waterfall([
		//获取bucket连接
		function (callback) {
			dbmanager.openBucket(self.clusterUrl, self.bucketName, callback);
		},
		function (bucket, callback) {
			var queryStr = 'select meta(usr).id as id, usr.* ' +
										 'from ' + self.bucketName + ' usr ' +
										 'where usr.type == "' + constant.document_type.User + '" ' +
										 'and usr._sync is not missing ' +
										 'and usr.role_type = ' + userRole + ' ';
			if (orderField) {
				if (orderField == constant.viewQuery.orderDesc) {
					//医生的话,先根据科室,再根据phone_numebr排序
					if (userRole == constant.user_role.doctor) {
						queryStr += 'order by usr.' + orderField + ' DESC, usr.phone_number DESC ';
					} else {
						queryStr += 'order by usr.' + orderField + ' DESC ';
					}
				} else {
					//医生的话,先根据科室,再根据phone_numebr排序
					if (userRole == constant.user_role.doctor) {
						queryStr += 'order by usr.' + orderField + ' ASC, usr.phone_number ASC ';
					} else {
						queryStr += 'order by usr.' + orderField + ' ASC ';
					}
				}
			}
			if (limit != undefined) {
				queryStr += 'LIMIT ' + (+limit) + ' ';
			}
			if(skip != undefined) {
				queryStr += 'OFFSET ' + (+skip) + ' ';
			}
			console.log(queryStr);
			dbmanager.N1qlQuery(bucket, queryStr, callback);
		}
	], function (err, result) {
		cb(err, result);
	});
};

/**
 * 根据患者id查询患者未付款的成员
 * @param patientId
 * @param cb
 */
UserService.prototype.findPatientMemberNotPayById = function (patientId, cb) {
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
			var queryStr = 'select meta(patient).id as patientId, patient.* ' +
										 'from ' + self.bucketName + ' as patient ' +
										 'where patient.type="Patient" ' +
										 'and patient._sync is not missing ' +
										 'and patient.`user` is not null ' +
										 'and patient.`user` is not missing ' +
										 'and patient.relationship is not null ' +
										 'and patient.relationship is not missing ' +
										 'and patient.isPay == false ' +
										 'and patient.`user` == "' + patientId + '"';
			dbmanager.N1qlQuery(bucket, queryStr, callback);
		}
	], function (err, result) {
		cb(err, result);
	});
};

/**
 * 根据id查询用户
 * @param userId
 * @param cb
 */
UserService.prototype.findUserById = function (userId, cb) {
	if (!userId) {
		var error = new Error("请指定要查询的用户.");
		cb(error);
	}
	var self = this;
	async.waterfall([
		//获取bucket连接
		function (callback) {
			dbmanager.openBucket(self.clusterUrl, self.bucketName, callback);
		},
		function (bucket, callback) {
			var queryStr = 'select usr.* from ' + self.bucketName + ' as usr ' +
			'use keys "' + userId + '" ' +
			'where ' +
			'usr._sync is not missing ' +
			'limit 1';
			console.log(queryStr);
			dbmanager.N1qlQuery(bucket, queryStr, callback);
		}
	], function (err, result) {
		cb(err, result);
	});
};


/**
 * 根据手机号, 用户类型查询用户
 * @param phoneNum 手机号
 * @param roleType 用户角色, constant.user_role
 * @param cb
 */
UserService.prototype.getUserByPhoneNumAndRole = function (phoneNum, roleType, cb) {
	var self = this;
	if (!phoneNum) {
		var error = new Error("手机号不能为空!");
		cb(error);
	}
	//不用!roleType判断,因为医生roleType===0
	if (roleType === undefined) {
		var error = new Error("用户类型不能为空!");
		cb(error);
	}
	async.waterfall([
		//bucket连接
		function (callback) {
			dbmanager.openBucket(self.clusterUrl, self.bucketName, callback);
		},
		function (bucket, callback) {
			var queryStr = 'select meta(usr).id, usr.* from ' + self.bucketName + ' usr ' +
				'where usr.type="' + constant.document_type.User + '" ' +
				'and usr.phone_number = "' + phoneNum + '" ' +
				'and usr.role_type = ' + roleType + ' ' +
				'and usr._sync is not missing ' +
				'limit 1';
			dbmanager.N1qlQuery(bucket, queryStr, callback);
		}
	], function (err, result) {
		cb(err, result);
	});
};

module.exports = new UserService();