#64
sudo rm ./temp/app.nw
sudo rm "Ledger Wallet Ripple"
sudo rm -r ./temp/ledger_wallet_ripple_64
sudo rm -r ./temp/ledger_wallet_ripple_64.zip
sudo rm -r ./temp/ledger_wallet_ripple.tar.gz
sudo rm -r ./temp/ledger_wallet_ripple
sudo rm -r ./temp/templates/ledger_wallet_ripple/usr/share/ledger_wallet_ripple

cd ./target/chrome-app/
zip -r ../../../app.nw *
cd ../../
cat nwjs-sdk-v0.25.4-linux-x64/nw app.nw > "Ledger Wallet Ripple" && chmod +x "Ledger Wallet Ripple"
mkdir ledger_wallet_ripple_64
cp -r "Ledger Wallet Ripple" ledger_wallet_ripple_64/
cp -r nwContext-x64/* ledger_wallet_ripple_64
zip -r ledger_wallet_ripple_64.zip ledger_wallet_ripple_64/*	
cp -r ledger_wallet_ripple_64 ./temp/templates/ledger_wallet_ripple/usr/share/ledger_wallet_ripple
cd ./temp/templates/ledger_wallet_ripple/
sudo tar -czvf ../ledger_wallet_ripple_x64.tar.gz *
cd ./temp/templates/
sudo alien --to-deb ledger_wallet_ripple_x64.tar.gz
sudo alien --to-rpm --target=x86_64 ledger-wallet-ripple-x64_1-2_all.deb


#32
sudo rm ./temp/app.nw
sudo rm "Ledger Wallet Ripple"
sudo rm -r ./temp/ledger_wallet_ripple_32
sudo rm -r ./temp/ledger_wallet_ripple_32.zip
sudo rm -r ./temp/ledger_wallet_ripple.tar.gz
sudo rm -r ./temp/ledger_wallet_ripple
sudo rm -r ./temp/templates/ledger_wallet_ripple/usr/share/ledger_wallet_ripple

cd /home/alix
cat nwjs-sdk-v0.25.4-linux-ia32/nw app.nw > "Ledger Wallet Ripple" && chmod +x "Ledger Wallet Ripple"
mkdir ledger_wallet_ripple_32
cp -r "Ledger Wallet Ripple" ledger_wallet_ripple_32/
cp -r nwContext-ia32/* ledger_wallet_ripple_32/
zip -r ledger_wallet_ripple_32.zip ledger_wallet_ripple_32/*	
cp -r ledger_wallet_ripple_32 ./temp/templates/ledger_wallet_ripple/usr/share/ledger_wallet_ripple
cd ./temp/templates/ledger_wallet_ripple/
sudo tar -czvf ../ledger_wallet_ripple_ia32.tar.gz *
cd ./temp/templates/
sudo alien --to-deb ledger_wallet_ripple_ia32.tar.gz
sudo alien --to-rpm --target=ia32 ledger-wallet-ripple-ia32_1-2_all.deb


