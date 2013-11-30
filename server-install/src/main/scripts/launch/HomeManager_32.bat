cd lib
del swt.jar
del rxtxSerial.dll
copy ..\os\rxtxSerial_32.dll rxtxSerial.dll
start javaw -jar ${project.artifactId}.jar
