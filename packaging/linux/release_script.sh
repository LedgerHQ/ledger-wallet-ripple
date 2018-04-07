#64
if [ "$1" == "" ]; then
    echo "Positional parameter 1 must specify the version of the release"
fi
sudo rm temp/app.nw
sudo rm temp/"LedgerWalletRipple"
sudo rm -r temp/ledger_wallet_ripple_64
sudo rm -r temp/ledger_wallet_ripple_64.zip
sudo rm -r temp/*.tar.gz
sudo rm -r temp/ledger_wallet_ripple_64
sudo rm -r temp/ledger_wallet_ripple

cd ../../target/chrome-app/
zip -r ../../packaging/linux/temp/app.nw *
cd ../../packaging/linux/temp
cp -r ../templates/ledger_wallet_ripple .
cat ../../../nwjs-sdk-v0.25.4-linux-x64/nw app.nw > "LedgerWalletRipple" && chmod +x "LedgerWalletRipple"
mkdir ledger_wallet_ripple_64
cp -r "LedgerWalletRipple" ledger_wallet_ripple_64/
cp -r ../../../nwjs-sdk-v0.25.4-linux-x64/* ledger_wallet_ripple_64
rm ledger_wallet_ripple_64/nw
zip -r ledger_wallet_ripple_64.zip ledger_wallet_ripple_64/*
cp -r ledger_wallet_ripple_64 ledger_wallet_ripple/usr/share/ledger_wallet_ripple
cp ../templates/ripple.ico ledger_wallet_ripple/usr/share/ledger_wallet_ripple/
cd ledger_wallet_ripple
sudo tar -czvf ../ledger_wallet_ripple_x64.tar.gz *
cd ..
sudo alien --to-deb --version=$1 --nopatch ledger_wallet_ripple_x64.tar.gz
mv -f *.deb ../releases/ledger_wallet_ripple_x64-$1.deb
cd ../releases
sudo alien --to-rpm --version=$1 --target=x86_64 --nopatch ledger_wallet_ripple_x64_$1.deb
mv *.rpm ledger_wallet_ripple_x64_$1.rpm


#32
# sudo rm ./temp/app.nw
# sudo rm "Ledger Wallet Ripple"
# sudo rm -r ./temp/ledger_wallet_ripple_32
# sudo rm -r ./temp/ledger_wallet_ripple_32.zip
# sudo rm -r ./temp/ledger_wallet_ripple.tar.gz
# sudo rm -r ./temp/ledger_wallet_ripple
# sudo rm -r ./temp/templates/ledger_wallet_ripple/usr/share/ledger_wallet_ripple
# 
# cd /home/alix
# cat nwjs-sdk-v0.25.4-linux-ia32/nw app.nw > "Ledger Wallet Ripple" && chmod +x "Ledger Wallet Ripple"
# mkdir ledger_wallet_ripple_32
# cp -r "Ledger Wallet Ripple" ledger_wallet_ripple_32/
# cp -r nwContext-ia32/* ledger_wallet_ripple_32/
# zip -r ledger_wallet_ripple_32.zip ledger_wallet_ripple_32/*	
# cp -r ledger_wallet_ripple_32 ./temp/templates/ledger_wallet_ripple/usr/share/ledger_wallet_ripple
# cd ./temp/templates/ledger_wallet_ripple/
# sudo tar -czvf ../ledger_wallet_ripple_ia32.tar.gz *
# cd ./temp/templates/
# sudo alien --to-deb ledger_wallet_ripple_ia32.tar.gz
# sudo alien --to-rpm --target=ia32 ledger-wallet-ripple-ia32_1-2_all.deb


