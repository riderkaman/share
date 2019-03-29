set SERVER_NAME=e-ps_2.gsc

set DEBUG_OPTS=-Xdebug
set DEBUG_OPTS=%DEBUG_OPTS% -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000

set JMX_OPTS=-Dcom.sun.management.jmxremote=true
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.ssl=false
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.local.only=false
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.port=1403
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.rmi.port=1403
set JMX_OPTS=%JMX_OPTS% -Djava.rmi.server.hostname=203.245.89.117

set JAVA_OPTS=-server
set JAVA_OPTS=%JAVA_OPTS% -Xms8192m -Xmx8192m
set JAVA_OPTS=%JAVA_OPTS% -Djava.net.preferIPv4Stack=true
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=utf-8
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.trustStore="C:\Program Files\Java\jdk1.7.0_80\jre\lib\security\cacerts"
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.trustStorePassword="changeit"

set LAUNCHER=e-ps-3.2.8.RELEASE.jar

goto checkJava

:checkJava
set JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%JAVACMD%" == "" set JAVACMD=%JAVA_HOME%\bin\java.exe

:noJavaHome
if "%JAVACMD%" == "" set JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.
