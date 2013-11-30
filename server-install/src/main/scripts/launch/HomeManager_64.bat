cd lib
del rxtxSerial.dll
copy ..\os\rxtxSerial_64.dll rxtxSerial.dll
start javaw -jar ${project.artifactId}.jar
