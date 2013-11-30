#!/bin/sh
cd lib
rm librxtxSerial.so
cp ../os/librxtxSerial_x86_64.so librxtxSerial.so
java -Djava.library.path=. -jar ${project.artifactId}.jar
