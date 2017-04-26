window.require = function () {
    var stack = new Error().stack;
     console.log("PRINTING CALL STACK");
     console.log( stack );
}