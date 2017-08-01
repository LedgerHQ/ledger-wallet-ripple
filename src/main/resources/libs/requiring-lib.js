window.binary = require('ripple-binary-codec')
window.gui = require('nw.gui')
window.open = require('opn')


function resetData() {
    console.log("Global desktop keyboard shortcut: " + this.key + " active.");
    if (confirm("Are you sure you want to reset transactions history? The wallet will close after cleaning the database.")) {
        if (!_.isUndefined(window.currentDb)) {
            window.currentDbConnection.close()
            indexedDB.deleteDatabase(window.currentDb);
        }
        indexedDB.deleteDatabase("ChromePreferences");
        window.gui.App.quit();
    }
}

var option = {
  key : "Ctrl+Shift+A",
  active : resetData,
  failed : function(msg) {
    // :(, fail to register the |key| or couldn't parse the |key|.
    console.log(msg);
  }
};

// Create a shortcut with |option|.
var shortcut = new window.gui.Shortcut(option);

// Register global desktop shortcut, which can work without focus.
window.gui.App.registerGlobalHotKey(shortcut);
