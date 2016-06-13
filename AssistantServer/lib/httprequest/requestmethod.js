/**
 * Created by garychen on 11/15/15.
 */
var request = require('request');
var logger = require('../../utils/logger');

function RequestMethod() {
}


RequestMethod.prototype.requestGet = function (url, callback) {
	request({
		url: url,
		method: "GET"
	}, function (err, response, body) {
		callback(err, body)
	});
};

RequestMethod.prototype.requestPost = function(url, postJson, callback) {
	request({
		url: url,
		method: "POST",
		json: postJson
	}, function (err, response, body) {
		callback(err, body);
	});
};

RequestMethod.prototype.requestPut = function(url, putJson, callback) {
	request({
		url: url,
		method: "PUT",
		json: putJson
	}, function (err, response, body) {
		callback(err, body);
	});
};

RequestMethod.prototype.requestDelete = function(url, postJson, callback) {
	request({
		url: url,
		method: "DELETE",
		json: postJson
	}, function (err, response, body) {
		callback(err, body);
	});
};

module.exports = new RequestMethod();