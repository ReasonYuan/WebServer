/**
 * Created by garychen on 11/30/15.
 */
function DateUtils() {}

DateUtils.prototype.Format = function (date, fmt) {
	if (date instanceof Date) {
		var o = {
			"M+": date.getMonth() + 1, //月份
			"d+": date.getDate(), //日
			"h+": date.getHours(), //小时
			"m+": date.getMinutes(), //分
			"s+": date.getSeconds(), //秒
			"q+": Math.floor((date.getMonth() + 3) / 3), //季度
			"S": date.getMilliseconds() //毫秒
		};

		if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
		for (var k in o)
			if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
		return fmt;
	} else {
		throw new Error('need an instanceof Date');
		return date;
	}
}

DateUtils.prototype.getDays = function (fromDate, toDate) {
	if ((fromDate instanceof Date) && (toDate instanceof Date)) {
		var timeDiff = Math.abs(toDate.getTime() - fromDate.getTime());
		var diffDays = Math.round(timeDiff / (1000 * 3600 * 24));
		return diffDays;
	} else {
		throw new Error('need an instanceof Date');
		return 0;
	}
}

module.exports = new DateUtils();