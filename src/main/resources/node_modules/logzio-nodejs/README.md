![Build Status](https://travis-ci.org/logzio/logzio-nodejs.svg?branch=master)

# logzio-nodejs  
NodeJS logger for LogzIO.
The logger stashes the log messages you send into an array which is sent as a bulk once it reaches its size limit (100 messages) or time limit (10 sec) in an async fashion.
It contains a simple retry mechanism which upon connection reset (server side) or client timeout, wait a bit (default interval of 2 seconds), and try this bulk again (it does not block other messages from being accumulated and sent (async). The interval increases by a factor of 2 between each retry, until we reached the maximum allowed attempts (3).

 By default any error is logged to the console. This can be changed by supplying a callback function.


## Sample usage
```javascript
var logger = require('logzio-nodejs').createLogger({
    token: '__YOUR_API_TOKEN__',
    type: 'YourLogType'     // OPTIONAL (If none is set, it will be 'nodejs')
});


// sending text
logger.log('This is a log message');

// sending an object
var obj = {
    message: 'Some log message',
    param1: 'val1',
    param2: 'val2'
};
logger.log(obj);
```

## Options

* **token**
    Mandatory. Your API logging token. Look it up in the Device Config tab in Logz.io
* **type** - Log type. Help classify logs into different classifications
* **protocol** - 'http', 'https' or 'udp'. Default: http
* **sendIntervalMs** - Time in milliseconds to wait between retry attempts. Default: 2000 (2 sec)
* **bufferSize** - The maximum number of messages the logger will accumulate before sending them all as a bulk. Default: 100.
* **numberOfRetries** - The maximum number of retry attempts. Default: 3
* **debug** - Should the logger print debug messages to the console? Default: false
* **callback** - a callback function called when an unrecoverable error has occured in the logger. The function API is: function(err) - err being the Error object.
* **timeout** - the read/write/connection timeout in milliseconds.
* **addTimestampWithNanoSecs** - Add a timestamp with nano seconds granularity. This is needed when many logs are sent in the same millisecond, so you can properly order the logs in kibana. The added timestamp field will be `@timestamp_nano` Default: false

## Using UDP
A few notes are worth mentioning regarding the use of the UDP protocol :
* UDP has some limitations, and therefore it is not the recommended protocol :
  * There is no guarantee that the logs have been received.
  * UDP can't take advantage of the bulk api and therefore performance is sub-optimal.
* When using UDP, each message is being sent separately, and not using the bulk api. This means that the meaning of `bufferSize` is slightly
different in this case. The messages will still be sent separately, but the logger will wait for the buffer to reach the size specified before
sending out all the messages. If you want each message to be sent out immediately, then set `bufferSize = 1`.


## Update log
**0.4.6**  
- Updated moment (v2.19.3) and request (v2.81.0) packages 

**0.4.4**  
- `@timestamp` and `@timestamp_nano` will no longer be overriden given a custom value by the user. 

**0.4.3**  
- Add the `@timestamp` field to the logs on the client's machine (and not when it reaches the server)

**0.4.1**
- Updated `request` dependency to 2.75.0

**0.4.0**
- Fixed issue #12 - added support for UDP
- Minor refactorings

**0.3.10**
- Fixed issue #17 - sendAndClose() wasn't actually closing the timer

**0.3.9**
- Added option to add a timestamp with nano second granularity

**0.3.8**
- Updated listener url
- Added `sendAndClose()` method which immediately sends the queued messages and clears the global timer
- Added option to supress error messages

**0.3.6**
- Fixed URL for github repository in package.json

**0.3.5**
- Bug fix : upon retry (in case of network error), the message gets sent forever  

**0.3.4**
- Bug fix : `jsonToString()` was throwing an error in the catch()block  

**0.3.2**  
- Enhancement : Added option to attach extra fields to each log in a specific instance of the logger.

**0.3.1**
- Bug fix : When calling `log` with a string parameter, the object isn't constructed properly.  



# Scripts

- run `npm install` to install required dependencies
- run `npm test` to run unit tests
