
window.addEventListener('message', onMessage);
var message = null

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
    console.log(event.data)
    switch(method){  //faire un dict
        case "set_option":
            var response = createAPI(parameters);
            break;
        //all prepare method return the same
        case "preparePayment":
            var promise = api.preparePayment(parameters);
            promise.then( function (prepared) {
                    message = {success: true, response: prepared};
                }
            ).catch(function (prepared) {
                    message = {success: false, response: prepared};
                }
            );
    }
    var toScala = {call_id: event.data.call_id, response: message};
    event.source.postMessage(toScala, event.origin);
}

function createAPI(options){
    api = new RippleAPI(options);
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
    api.connect().then(function () {return {connected:true};});
}

//kjjg