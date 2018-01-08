var request = require('request');
var stringifySafe = require('json-stringify-safe');
var _assign = require('lodash.assign');
var dgram = require('dgram');

exports.version = require('../package.json').version;

var LogzioLogger = function (options) {
    if (!options || !options.token) {
        throw new Error('You are required to supply a token for logging.');
    }

    this.token = options.token;
    this.host = options.host || 'listener.logz.io';
    this.userAgent = 'Logzio-Logger NodeJS';
    this.type = options.type || 'nodejs';
    this.sendIntervalMs = options.sendIntervalMs || 10 * 1000;
    this.bufferSize = options.bufferSize || 100;
    this.debug = options.debug || false;
    this.numberOfRetries = options.numberOfRetries || 3;
    this.timer = null;
    this.closed = false;
    this.supressErrors = options.supressErrors || false;
    this.addTimestampWithNanoSecs = options.addTimestampWithNanoSecs || false;

    var protocolToPortMap = {
        'udp': 5050,
        'http': 8070,
        'https': 8071
    };
    this.protocol = options.protocol || 'http';
    if (!protocolToPortMap.hasOwnProperty(this.protocol)) {
        throw new Error('Invalid protocol defined. Valid options are : ' + JSON.stringify(Object.keys(protocolToPortMap)));
    }
    this.port = protocolToPortMap[this.protocol];

    if (this.protocol === 'udp') {
        this.udpClient = dgram.createSocket('udp4');
    }

    /*
      Callback method executed on each bulk of messages sent to logzio.
      If the bulk failed, it will be called: callback(exception), otherwise upon
      success it will called as callback()
    */
    this.callback = options.callback || this._defaultCallback;

    /*
     * the read/write/connection timeout in milliseconds of the outgoing HTTP request
     */
    this.timeout = options.timeout;

    // build the url for logging
    this.url = this.protocol + '://' + this.host + ':' + this.port + '?token=' + this.token;

    this.messages = [];
    this.bulkId = 1;
    this.extraFields = options.extraFields || {};
};

exports.createLogger = function (options) {
    var l = new LogzioLogger(options);
    l._timerSend();
    return l;
};

var jsonToString = exports.jsonToString = function(json) {
    try {
        return JSON.stringify(json);
    }
    catch(ex) {
        return stringifySafe(json, null, null, function() { });
    }
};

LogzioLogger.prototype._defaultCallback = function(err) {
    if (err && !this.supressErrors) {
        console.error('logzio-logger error: ' + err, err);
    }
};

LogzioLogger.prototype.sendAndClose = function(callback){
    this.callback = callback || this._defaultCallback;
    this._debug("Sending last messages and closing...");
    this._popMsgsAndSend();
    clearTimeout(this.timer);

    if (this.protocol === 'udp') {
        this.udpClient.close();
    }
};

LogzioLogger.prototype._timerSend = function() {
    if (this.messages.length > 0) {
        this._debug('Woke up and saw ' + this.messages.length + ' messages to send. Sending now...');
        this._popMsgsAndSend();
    }

    var self = this;
    this.timer = setTimeout(function() {
        self._timerSend();
    }, this.sendIntervalMs);
};

LogzioLogger.prototype._sendMessagesUDP = function() {
    var messagesLength = this.messages.length;
    var self = this;

    var udpSentCallback = function(err, bytes) {
        if (err) {
            self._debug('Error while sending udp packets. err = ' + err);
            callback(new Error('Failed to send udp log message. err = ' + err));
        }
    };

    for (var i=0; i<messagesLength; i++) {

        var msg = this.messages[i];
        msg.token = this.token;
        var buff = new Buffer(stringifySafe(msg));

        self._debug('Starting to send messages via udp.');
        this.udpClient.send(buff, 0, buff.length, this.port, this.host, udpSentCallback);
    }
};

LogzioLogger.prototype.close = function () {
    // clearing the timer allows the node event loop to quit when needed
    clearTimeout(this.timer);
    if (this.protocol === 'udp') {
        this.udpClient.close();
    }

    // send pending messages, if any
    if (this.messages.length > 0) {
        this._debug("Closing, purging messages.");
        this._popMsgsAndSend();
    }

    // no more logging allowed
    this.closed = true;
};

/**
 * Attach a timestamp to the log record. If @timestamp already exists, use it. Else, use current time.
 * The same goes for @timestamp_nano
 * @param msg - The message (Object) to append the timestamp to.
 * @private
 */
LogzioLogger.prototype._addTimestamp = function(msg) {
    var now = (new Date()).toISOString();
    msg['@timestamp'] = msg['@timestamp'] || now;

    if (this.addTimestampWithNanoSecs) {
        var time = process.hrtime();
        msg['@timestamp_nano'] = msg['@timestamp_nano'] || [now, time[0].toString(), time[1].toString()].join('-');
    }
};

LogzioLogger.prototype.log = function(msg) {
    if (this.closed === true) {
        throw new Error('Logging into a logger that has been closed!');
    }
    if (typeof msg === 'string') {
        msg = { message: msg };
        if (this.type) msg.type = this.type;
    }
    msg = _assign(msg, this.extraFields);
    msg.type = this.type;

    this._addTimestamp(msg);

    this.messages.push(msg);
    if (this.messages.length >= this.bufferSize) {
        this._debug('Buffer is full - sending bulk');
        this._popMsgsAndSend();
    }
};

LogzioLogger.prototype._popMsgsAndSend = function() {
    
    if (this.protocol === 'udp') {
        this._debug('Sending messages via udp');
        this._sendMessagesUDP();
    }
    else {
        var bulk = this._createBulk(this.messages);
        this._debug('Sending bulk #' + bulk.id);
        this._send(bulk);
    }

    this.messages = [];
};

LogzioLogger.prototype._createBulk = function(msgs) {
    var bulk = {};
    // creates a new copy of the array. Objects references are copied (no deep copy)
    bulk.msgs = msgs.slice();
    bulk.attemptNumber = 1;
    bulk.sleepUntilNextRetry = 2*1000;
    bulk.id = this.bulkId++;

    return bulk;
};

LogzioLogger.prototype._messagesToBody = function(msgs) {
    var body = '';
    for (var i = 0; i < msgs.length; i++) {
        body = body + jsonToString(msgs[i]) + '\n';
    }
    return body;
};

LogzioLogger.prototype._debug = function(msg) {
    if (this.debug) console.log('logzio-nodejs: ' + msg);
};

LogzioLogger.prototype._send = function(bulk) {
    var self = this;
    function tryAgainIn(sleepTimeMs) {
        self._debug('Bulk #' + bulk.id + ' - Trying again in ' + sleepTimeMs + '[ms], attempt no. ' + bulk.attemptNumber);
        setTimeout(function() {
            self._send(bulk);
        }, sleepTimeMs);
    }

    var body = this._messagesToBody(bulk.msgs);
    var options = {
        uri: this.url,
        body: body,
        headers: {
            'host': this.host,
            'accept': '*/*',
            'user-agent': this.userAgent,
            'content-type': 'text/plain',
            'content-length': Buffer.byteLength(body)
        }
    };
    if (typeof this.timeout !== 'undefined') {
        options.timeout = this.timeout;
    }

    var callback = this.callback;
    try {
        request.post(options, function (err, res, body) {
            if (err) {
                // In rare cases server is busy
                if (err.code === 'ETIMEDOUT' || err.code === 'ECONNRESET' || err.code === 'ESOCKETTIMEDOUT' || err.code === 'ECONNABORTED') {
                    if (bulk.attemptNumber >= self.numberOfRetries) {
                        callback(new Error('Failed after ' + bulk.attemptNumber + ' retries on error = ' + err, err));
                    } else {
                        self._debug('Bulk #' + bulk.id + ' - failed on error: ' + err);
                        var sleepTimeMs = bulk.sleepUntilNextRetry;
                        bulk.sleepUntilNextRetry = bulk.sleepUntilNextRetry * 2;
                        bulk.attemptNumber++;
                        tryAgainIn(sleepTimeMs)
                    }
                }
                else {
                    callback(err);
                }
            }
            else {
                var responseCode = res.statusCode.toString();
                if (responseCode !== '200') {
                    callback(new Error('There was a problem with the request.\nResponse: ' + responseCode + ': ' + body.toString()));
                }
                else {
                    self._debug('Bulk #' + bulk.id + ' - sent successfully');
                    callback();
                }
            }

        });
    }
    catch (ex) {
        callback(ex);
    }
};
