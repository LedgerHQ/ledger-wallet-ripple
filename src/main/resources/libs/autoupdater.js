window.nwAutoupdate = require ( "nw-autoupdater" );
window.os = require("os");
window.env = require("./node_modules/nw-autoupdater/Lib/env.js")
window.debounce = require("debounce")
window.request = require("./node_modules/nw-autoupdater/Lib/request.js")
window.nwAutoupdaterFactory = function (options) {
    return new nwAutoupdate(
        chrome.runtime.getManifest(),
        options
    )
}