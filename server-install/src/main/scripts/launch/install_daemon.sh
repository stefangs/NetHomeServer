#!/bin/sh
#
#Assumes root, make sure to call as 'sudo install_daemon.sh'
#

DISPLAY_NAME=NetHomeServer
INFILE=nhs_daemon_template
OUTFILE=nhs-daemon
INFILE_NHS=nhs_template
OUTFILE_NHS=nhs
SRCPATH=$(dirname $(readlink -f $0))
SCRIPT=HomeManager_raspian_debug.sh
JAVA_RUNTIME=/usr/bin/java
START_PATH=$SRCPATH/lib
ID=$OUTFILE
JAVA_ARGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Djava.library.path=$START_PATH/ -jar $START_PATH/home.jar"
# start-stop-daemon --start --quiet --pidfile /etc/init.d/nhs-daemon --exec /usr/bin/java -- -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Djava.library.path=/home/pi/nethomeservernightly/lib/ -jar /home/pi/nethomeservernightly/lib/home.jar

echo "Copying drivers..."
pwd
cd lib
rm librxtxSerial.so
cp ../os/librxtxSerial_raspian.so librxtxSerial.so
cd ..

echo "Replacing display name with ${OUTFILE}..."
sed -e "s|_DAEMON_DISPLAY_NAME_|$DISPLAY_NAME|" \
	-e "s|_JAVA_RUNTIME_|${JAVA_RUNTIME}|g"  \
	-e "s|_JAVA_ARGS_|${JAVA_ARGS}|g"  \
	-e "s|_START_PATH_|${START_PATH}|g"  \
	-e "s|_DAEMON_ID_|${ID}|g" ${INFILE} \
	${INFILE} > ${OUTFILE}

echo "Creating utility file $OUTFILE_NHS"
sed -e "s|_NHS_DAEMON_|$OUTFILE|" \
	${INFILE_NHS} > /usr/sbin/${OUTFILE_NHS}

echo "Copying configurations..."
cp ${OUTFILE} /etc/init.d
chmod +x /etc/init.d/${OUTFILE} 
chmod +x /usr/sbin/${OUTFILE_NHS}

echo "Installing as a service"
update-rc.d ${OUTFILE} 	 defaults

echo "Start the service by issing:"
${OUTFILE_NHS}