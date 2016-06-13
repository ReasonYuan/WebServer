/**
 * Created by garychen on 11/29/15.
 */
var yunpainSMSClient = require('yunpian-sms-client');
var config = require('../config');
var stringUtils = require('../utils/StringUtils');
var yunpianApiKey = config.apiKeys.yunpian;

function SendSMS() {
	this.yunpianClient = null;
}

SendSMS.prototype.sendSMS = function (phoneNumber, securityCode, callback) {
	this.yunpianClient = new yunpainSMSClient({
		apiKey: yunpianApiKey,
		sendContent: '您的验证码是' + securityCode + '。如非本人操作，请忽略本短信',
		mobile: [phoneNumber]
	});

	this.yunpianClient.sendSMS(callback);
}

SendSMS.prototype.sendSMSByTemplate = function (phoneNumber, securityCode, templateName, callback) {
	templateName = !templateName ? 'company_code' : templateName;
	var smsTemplate = config.templates.SMSTemplates[templateName];
	smsTemplate.apiKey = yunpianApiKey;
	smsTemplate.mobile = phoneNumber;
	smsTemplate.templateValue = stringUtils.formatPlaceholder(smsTemplate.templateValue, securityCode);

	this.yunpianClient = new yunpainSMSClient(smsTemplate);

	this.yunpianClient.sendSMSByTemplate(callback);
}


module.exports = new SendSMS();