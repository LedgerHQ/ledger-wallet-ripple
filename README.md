#Ledger Wallet Ripple

#Build
You need [SBT](http://www.scala-sbt.org/0.13/docs/Setup.html) installed. (For Windows user, we recommand you install and use the [Git bash](https://git-scm.com/download/win)
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
Download and extract [x64 version](https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-win-ia32.zip) or [x32 version](https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-win-ia32.zip) at the root of the repository
```
nwjs-sdk-v0.25.4-win-ia32/nwjs.exe target/chrome-app/
```

##Windows x64
Download and extract [x64 version](https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-win-x64.zip) or [x32 version](https://dl.nwjs.io/v0.25.4/nwjs-sdk-v0.25.4-win-ia32.zip) at the root of the repository
```
nwjs-sdk-v0.25.4-win-x64/nwjs.exe target/chrome-app/
```


