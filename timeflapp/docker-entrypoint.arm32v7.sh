#!/bin/bash
set -e
service dbus start
/usr/libexec/bluetooth/bluetoothd &
sleep 1
exec java -Djava.library.path=/usr/lib/arm-linux-gnueabihf/ -cp target/classes:lib/tinyb.jar:target/dependency/* at.ac.uibk.timeguess.timeflapp.Main
