@echo off
SET "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
SET "PATH=%JAVA_HOME%\bin;%PATH%"
gradlew assembleDebug
