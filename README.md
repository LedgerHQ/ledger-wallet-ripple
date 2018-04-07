# Ledger Wallet Ripple

# Build

## Windows Users

We recommend you install and use the [Git bash](https://git-scm.com/download/win)

- Open control panel
- Go to: System and Security -> System -> Advanced system settings (on th left) -> environment variables
- Under " System variables" click on "New"
- Name: JAVA_OPTS and Value: -Xms512m -Xmx1024m
- Confirm and apply

## All platform

You need [SBT](http://www.scala-sbt.org/1.x/docs/Setup.html) and [JSDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)  installed.
```bash
git clone https://github.com/LedgerHQ/ledger-wallet-ripple.git
cd ledger-wallet-ripple
sbt build
```

#Run
##Linux 32 bits
```
wget -qO- "https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-linux-ia32.tar.gz" | tar xvz
PATH=/nwjs-sdk-v0.25.4-linux-ia32/
nw target/chrome-app/.
```

##Linux 64 bits
```
wget -qO- "https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-linux-x64.tar.gz" | tar xvz
PATH=/nwjs-sdk-v0.25.4-linux-x64/
nw target/chrome-app/.
```

##OSX
```
curl "https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-osx-x64.zip" > nwjs.zip
unzip nwjs.zip
rm nwjs.zip
nwjs-sdk-v0.25.4-osx-x64/nwjs.app/Content/MacOS/nwjs target/chrome-app/
```

##Windows x32
Download and extract [x64 version](https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-win-ia32.zip) at the root of the repository
```
nwjs-sdk-v0.25.4-win-ia32/nwjs.exe target/chrome-app/
```

##Windows x64
Download and extract [x64 version](https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-win-x64.zip) at the root of the repository
```
nwjs-sdk-v0.25.4-win-x64/nwjs.exe target/chrome-app/
```

#Packaging
##Linux Deb/Rpm/Zip
```
sudo apt-get install alien -y
cd packaging/linux
sudo chmod +x release_script.sh 1.0.3
./release_script.sh
```

##OSX
Install [packages](http://s.sudre.free.fr/Software/Packages/about.html)
```$xslt

```