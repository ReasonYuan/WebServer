/**
 * Created by garychen on 12/18/15.
 */
function ArrayUtils() {}

/**
 * 根据value移除数组元素
 * @param arr
 * @param value
 * @returns {*}
 */
ArrayUtils.prototype.removeByValue = function (arr, value) {
	var index = arr.indexOf(value);
	if (index >= 0) {
		arr.splice( index, 1 );
	}
	return arr;
}

Array.prototype.contains = function(obj) {
	var i = this.length;
	while (i--) {
		if (this[i] === obj) {
			return true;
		}
	}
	return false;
}

module.exports = new ArrayUtils();