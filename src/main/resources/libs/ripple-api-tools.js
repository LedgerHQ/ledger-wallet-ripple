
window.addEventListener('message', onMessage);
var message = null
var methodMatch = {
    "setOption": createAPI,
    "preparePayment": preparePayment,
    "disconnect": disconnect
}

var api={}

function onMessage(event) {
    console.log("messagecaught")
    var method = event.data.method;
    var call_id = event.data.call_id;
    var parameters = event.data.parameters;
    console.log(event.data)
    var result = methodMatch[method]
        console.log("functionover")

    result.then(function (message) {
         var toScala = {call_id: event.data.call_id, response: message};
         event.source.postMessage(toScala, event.origin);
        }).catch(function (message) {
            var toScala = {call_id: event.data.call_id, response: message};
            event.source.postMessage(toScala, event.origin);
        })
    }

function disconnect(){
    return api.disconnect()
}

function preparePayment(parameters) {
    return api.preparePayment(parameters)
}

function createAPI(options){
    console.log("inside")

    api = new RippleAPI(options);
    console.log("new")

    api.on('error', (errorCode, errorMessage) => {
      console.log(errorCode + ': ' + errorMessage);
    });
    api.on('connected', function () {
      console.log('connected');
    });
    api.on('disconnected', function (code) {
      // code - [close code](https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent) sent by the server
      // will be 1000 if this was normal closure
      console.log('disconnected, code:', code);
    });
    return api.connect()
}
