@echo off
echo 正在启动应用，带有自定义JVM参数...
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xshare:off"
pause 