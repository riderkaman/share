#!/bin/bash

. "setenv.sh"

if [ -f $SERVER_NAME.pid ] ; then
	echo "Stop $SERVER_NAME..."
	kill -9 `cat $SERVER_NAME.pid`
	rm -f $SERVER_NAME.pid
else
	echo "$SERVER_NAME.pid file does not exists!"
fi
