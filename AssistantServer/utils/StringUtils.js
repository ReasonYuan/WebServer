/**
 * Created by garychen on 11/29/15.
 */
function StringUtils() {
}

/**
 * 校验11位手机号，以1开头
 * @param mobileNumber
 * @returns {boolean}
 */
StringUtils.prototype.checkMobile = function (mobileNumber) {
	var re = /^1\d{10}/;
	if (re.test(mobileNumber)) {
		return true;
	} else {
		return false;
	}
}

StringUtils.prototype.formatPlaceholder = function() {
	// The string containing the format items (e.g. "{0}")
	// will and always has to be the first argument.
	var theString = arguments[0];

	// start with the second argument (i = 1)
	for (var i = 1; i < arguments.length; i++) {
		// "gm" = RegEx options for Global search (more than one instance)
		// and for Multiline search
		var regEx = new RegExp("\\{" + (i - 1) + "\\}", "gm");
		theString = theString.replace(regEx, arguments[i]);
	}

	return theString;
}

module.exports = new StringUtils();