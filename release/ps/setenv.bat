set SERVER_NAME=e-ps.gsc

set DEBUG_OPTS=-Xdebug
set DEBUG_OPTS=%DEBUG_OPTS% -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000

set JMX_OPTS=-Dcom.sun.management.jmxremote=true
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.ssl=false
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.local.only=false
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.port=1403
set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.rmi.port=1403
set JMX_OPTS=%JMX_OPTS% -Djava.rmi.server.hostname=10.1.3.225

set JAVA_OPTS=-server
set JAVA_OPTS=%JAVA_OPTS% -Xms256m -Xmx256m
set JAVA_OPTS=%JAVA_OPTS% -Djava.net.preferIPv4Stack=true
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=utf-8

set LAUNCHER=e-ts-3.2.8.RELEASE-r159.jar

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