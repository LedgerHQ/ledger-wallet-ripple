chrome.app.runtime.onLaunched.addListener(function () {
    chrome.app.window.create('window.html', {
        id: "main_window",
        innerBounds: {
          minWidth: 430,
          minHeight: 560
        }
      }
    );
});