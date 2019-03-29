call setenv.bat

TITLE %SERVER_NAME%
SETLOCAL enabledelayedexpansion
chcp 65001

rem "%JAVACMD%" -jar %JAVA_OPTS% %DEBUG_OPTS% %JMX_OPTS% %LAUNCHER%
"%JAVACMD%" -jar %JAVA_OPTS% %JMX_OPTS% %LAUNCHER% >> console.out
