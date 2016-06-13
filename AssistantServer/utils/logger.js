/**
 * Created by garychen on 11/16/15.
 */
var winston = require('winston');
winston.emitErrs = true;

var logger = new winston.Logger({
	transports: [
    new (winston.transports.Console)({
      level: 'debug',
      handleExceptions: true,
      colorize: true,
      json: false
    })
  ],
  exitOnError: false
});

module.exports = logger;
module.exports.stream = {
	write: function (message, encoding) {
		logger.info(message);
	}
}
