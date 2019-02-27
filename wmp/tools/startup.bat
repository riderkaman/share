rem /dev/java/jdk-1.7.0_80/bin/keytool -exportcert -keystore jssecacerts -storepass changeit -file output.cert -alias fcm.googleapis.com-1
rem /dev/java/jdk-1.7.0_80/bin/keytool -importcert -keystore /dev/java/jdk-1.7.0_80/jre/lib/security/cacerts -storepass changeit -file output.cert -alias fcm.googleapis.com-1
rem /dev/java/jdk-1.7.0_80/bin/keytool -v -list -keystore /dev/java/jdk-1.7.0_80/jre/lib/security/cacerts -storepass changeit -alias fcm.googleapis.com-1
rem /dev/java/jdk-1.7.0_80/bin/keytool -delete  -alias fcm.googleapis.com-1 -keystore /dev/java/jdk-1.7.0_80/jre/lib/security/cacerts -storepass changeit
rem Profiles
rem GCM-CERT-CHANGER
rem APNS-CERT-CHANGER
rem PROFILE-IMAGE-CROPPER
%JAVA_HOME%/bin/java -Dspring.profiles.active=PROFILE-IMAGE-CROPPER -jar tools-1.0.0.jar