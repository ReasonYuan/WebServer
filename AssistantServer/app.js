var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

//middleware
var versionMiddleware = require('./lib/middleware/version');

var setupview = require('./lib/db/views/setupviews');

//third modules
var uuid = require('node-uuid')
var session = require('express-session')
//third modules end


var routes = require('./routes/index');
var users = require('./routes/users');
var group = require('./routes/group');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hjs');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('combined'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);
app.use(versionMiddleware.versionCheck);
app.use('/users', users);
app.use('/group', group);

sessionInfo = []
app.use(session({
  genid: function(req) {
    var sessionid = uuid.v4()
    console.log("gen session id: " + sessionid)
    return sessionid // use UUIDs for session IDs
  },
  resave: false,
  saveUninitialized:true,
  secret: 'keyboard cat assistant server',
  cookie:{maxAge:48*60*60*1000}
}))

app.use(function(req, res, next){
  if(!sessionInfo[req.sessionID]){
    console.log("redirect... " + sessionInfo[req.sessionID])
    res.redirect("/")
    return
  } else {
    //console.log("not redirect... " + sessionInfo[req.sessionID])
    next()
  }
})

//db view setup
setupview.setUpView();

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});


module.exports = app;
