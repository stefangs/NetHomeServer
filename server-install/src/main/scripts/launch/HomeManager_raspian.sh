#!/bin/sh
cd lib
rm librxtxSerial.so
cp ../os/librxtxSerial_raspian.so librxtxSerial.so
java -Djava.library.path=. -jar home.jar "$@"
