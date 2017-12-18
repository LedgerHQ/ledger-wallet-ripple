# ledger-wallet-ripple


### For windows users

- Open control panel
- Go to: System and Security -> System -> Advanced system settings (on th left) -> environment variables
- Under " System variables" click on "New"
- Name: JAVA_OPTS and Value: -Xms512m -Xmx1024m
- Confirm and apply

Install [GIT](https://git-scm.com/).
During installation, choose the option to install the git bash

## Building

Install [Java SDK 8 for your platform](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
Install [SBT 1.x.x](https://www.scala-sbt.org/download.html) then open a terminal and paste the commands below: (Windows users, right click and select "open git bash here")

```bash
git clone https://github.com/LedgerHQ/ledger-wallet-ripple.git
cd ledger-wallet-ripple
sbt build
```

## Launching

Install [nwjs](https://nwjs.io/)

```bash
cd target/chrome-app
path/to/nwjs .
```
