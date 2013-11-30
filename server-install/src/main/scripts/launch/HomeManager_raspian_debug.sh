#!/bin/sh
cd lib
rm librxtxSerial.so
cp ../os/librxtxSerial_raspian.so librxtxSerial.so
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Djava.library.path=. -jar home.jar "$@"
