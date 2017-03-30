
const {RippleAPI} = require('ripple-lib');

window.addEventListener('message', onMessage);

function onMessage(event) {
    var method = event.data.method;
    var call_id = event.data.call_id;
    var parameters = event.data.parameters;
    /*if(method !== "set_option") {
        var response = window[api][method](parameters)
    }else{
        var response = createAPI(parameters);
        break;
    }*/
    switch(method){
        case "set_option":
            var response = createAPI(parameters);
            break;
        //all prepare method return the same
        case "preparePayment":
            var promise = api.preparePayment(parameters);
            promise.then(prepared => {
            var response = {success = true, response = prepared}},
            prepared => {
            var response = {success = false, response = prepared}
            });
    }
    var toScala = {call_id = event.data.call_id, response=response};
    event.source.postMessage(toScala, event.origin);
}

function createAPI(options){
    api = new RippleAPI(options);
    api.on('error', (errorCode, errorMessage) => {
      console.log(errorCode + ': ' + errorMessage);
    });
    api.on('connected', () => {
      console.log('connected');
    });
    api.on('disconnected', (code) => {
      // code - [close code](https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent) sent by the server
      // will be 1000 if this was normal closure
      console.log('disconnected, code:', code);
    });
    api.connect().then(() => {return {connected=true}};
    )
}