window.binary = require('ripple-binary-codec')
window.gui = require('nw.gui')
window.open = require('opn')

var option = {
  key : "Ctrl+Shift+A",
  active : function() {
    console.log("Global desktop keyboard shortcut: " + this.key + " active.");
    if (confirm("Are you sure you want to wipe application data? The wallet will close after cleaning the database")) {
        window.indexedDB.webkitGetDatabaseNames().onsuccess = function(sender,args)
        {
            var r = sender.target.result;
            for(var i in r)
                indexedDB.deleteDatabase(r[i]);
        };
        window.gui.App.quit()
    }
  },
  failed : function(msg) {
    // :(, fail to register the |key| or couldn't parse the |key|.
    console.log(msg);
  }
};

// Create a shortcut with |option|.
var shortcut = new window.gui.Shortcut(option);

// Register global desktop shortcut, which can work without focus.
window.gui.App.registerGlobalHotKey(shortcut);
