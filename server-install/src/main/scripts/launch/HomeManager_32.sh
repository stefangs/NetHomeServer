#!/bin/sh
cd lib
rm librxtxSerial.so
cp ../os/librxtxSerial_x86_32.so librxtxSerial.so
java -Djava.library.path=. -jar ${project.artifactId}.jar

