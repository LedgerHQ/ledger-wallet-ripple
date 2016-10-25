var apps = [
    {
        name: "Ledger Wallet Bitcoin",
        id: "kkdpmhnladdopljabkgpacgpliggeeaf"
    },
    {
        name: "Ledger Manager",
        id: "beimhnaefocolcplfimocfiaiefpkgbf"
    }
];

function ensureIsSingleton(callback) {
    var iterate = function (index) {
        if (index >= apps.length) {
            callback(true, null);
        } else {
            var app = apps[index];
            chrome.runtime.sendMessage(app.id, {request: "is_launched"},
                function (response) {
                    if (typeof response === "undefined" || !response.result)
                        iterate(index + 1);
                    else
                        callback(false, app);
                });
        }
    };
    iterate(0)
}

function startApp() {
    chrome.app.window.create('window.html', {
            id: "main_window",
            innerBounds: {
                width: 430,
                height: 580,
                minWidth: 430,
                minHeight: 580
            }
        }
    );
}

function displayCantLaunchNotification(app) {
    chrome.notifications.create("cannot_launch", {
        type: "basic",
        title: chrome.i18n.getMessage("application_name"),
        message: chrome.i18n.getMessage("application_singleton_alert_message").replace("{APPLICATION_NAME}", app.name),
        iconUrl: "images/app_icon/ic_app_48.png"
    }, function () {});
    chrome.app.window.create('mac_close_fix/fix.html', {
        id: "fix1000",
        innerBounds: {
            width: 0,
            height: 0,
            left: 0,
            top: 0,
            minWidth: 0,
            minHeight: 0
        },
        hidden: true,
        frame: "none"
    })
}

function tryStartApp() {
    ensureIsSingleton(function (isSingleton, app) {
        if (isSingleton) {
            startApp();
        } else {
            displayCantLaunchNotification(app)
        }
    })
}

chrome.app.runtime.onLaunched.addListener(function () {
    tryStartApp();
});

chrome.runtime.onMessageExternal.addListener(
    function(request, sender, sendResponse) {
        var command = request["request"];
        if (typeof command === "string") {
            command = {command: command};
        }

        switch (command.command) {
            case "launch":
                tryStartApp();
                sendResponse({command: "launch", result: true});
            case "ping":
                sendResponse({command: "ping", result: true});
                break;
            case "is_launched":
                sendResponse({command: "is_launched", result: (chrome.app.window.getAll().length != 0)});
                break;
        }
    });