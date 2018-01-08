var logzioLogger = require('./index');

var logger = logzioLogger.createLogger({
    token: 'thuqwebLGAmdurgIrDjLlpAptmQqUkxQ',
    type: 'thingk-tenant-app',
    protocol: 'http',
    sendIntervalMs: 3000,
    bufferSize: 64,
    numberOfRetries: 5,
    addNanoSecs: true,
    callback: function(ex) {
        return;
    },
    debug: true,
    timeout: 1000
});

console.log('hello');
logger.log('ssl testing');

for (var i=0; i<100; i++) {
    logger.log('hello, this is test #:' + i);
}