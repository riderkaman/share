#!/bin/bash

. "setenv.sh"

if [ -f $SERVER_NAME.pid ] ; then
	kill -9 `cat $SERVER_NAME.pid`
fi

echo "Start $SERVER_NAME...."
#nohup java $JAVA_OPTS $DEBUG_OPTS $JMX_OPTS -jar $LAUNCHER $SERVER_NAME > nohup.out &
nohup java $JAVA_OPTS -jar $LAUNCHER $SERVER_NAME > nohup.out &
echo $! > $SERVER_NAME.pid
tail -f nohup.out

