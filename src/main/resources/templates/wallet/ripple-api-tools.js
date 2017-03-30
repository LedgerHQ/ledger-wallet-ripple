
const {RippleAPI} = require('ripple-lib');

window.addEventListener('message', onMessage);

function onMessage(event) {
    var method = event.data.method
    var call_id = event.data.call_id
    var parameters = event.data.parameters
    switch(method) {
        case "set_option":
            response = createAPI(parameters);
            break;
    }
    var toScala = {call_id = event.data.call_id, response=response}
    event.source.postMessage(toScala, event.origin)
}

function createAPI(options){
    const api = new RippleAPI(options);
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
    api.connect().catch(console.error);
    var response = {connected=true}
    return response
}
