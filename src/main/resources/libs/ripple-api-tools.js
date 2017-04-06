
var message = null
var methodMatch = {
    "setOption": createAPI,
    "preparePayment": preparePayment,
    "disconnect": disconnect
}
window.addEventListener('message', onMessage);


var api={}

function onMessage(event) {
    var method = event.data.methodName;
    var call_id = event.data.call_id;
    var parameters = (JSON.parse(event.data.parameters));
    var result = methodMatch[method](parameters)
    console.log(result)
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
    console.log(options)
    api = new RippleAPI(options);
    console.log(api)
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
    console.log(api)
    return api.connect()
}
