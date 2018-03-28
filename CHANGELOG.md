# Changelog
1.1.1
==================
- Added node indicator on account page
- Fixed a rare bug with sorting of displayed transactions
- Reworded the help page
- Added a link to see full account history on bithomp explorer

1.1.0
==================
- Added Ledger Default Node support
- Support for Shapeshift address format with tag
- Support for future firmware using hid and u2f

1.0.3 (2017-18-12)
==================
- Removing direct mail contact for support
- Using base fees as default instead of mean fees
- Separating wallets when using different nodes
- Add option in the help section to reset the data
- Custom fees are now set to match base fees by default
- Corrected logs saving
- Corrected device discovery
- Changed class names to avoid cache error when compiling

1.0.2 (2017-07-06)
==================
- Fixed the transaction being displayed successful while missing a required destination tag
- Fixed transaction amount being false for partial payments
- The app doesn't use the Data API anymore to collect balance, transactions and check accounts (for it was unavailable most of the time)
- The QR Code scanning functionality is now functional


1.0.1 (2017-05-22)
==================
- Fixed tags not working when > 2147483647
- Fixed default node being corrupted when cancelling changes
- Added fields restriction for transactions
- Added network status check before sending


1.0.0 (2017-05-18)
==================
- First release of Ledger Wallet Ripple
