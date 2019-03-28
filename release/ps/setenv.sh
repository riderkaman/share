#!/bin/bash

SERVER_NAME=e-ps.gsc

DEBUG_OPTS="
-Xdebug
-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000
"

JMX_OPTS="
-Dcom.sun.management.jmxremote=true
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.local.only=false
-Dcom.sun.management.jmxremote.port=1403
-Dcom.sun.management.jmxremote.rmi.port=1403
-Djava.rmi.server.hostname=10.1.3.91
"

GC_OPTS='
 -XX:+UseConcMarkSweepGC
 -XX:+PrintGCDetails
 -XX:+PrintGCTimeStamps
 -XX:-TraceClassUnloading
'

JAVA_OPTS="
-server
-Xms512m -Xmx512m
-Djava.net.preferIPv4Stack=true
-Dfile.encoding=utf-8
-Dspring.profiles.active=GSC
"

LAUNCHER=e-ps-3.2.8.RELEASE-r159.jar
