@echo off
setlocal
set ngoyVersion=1.0.0-rc8-SNAPSHOT
set ngoyJar=%~dp0ngoy-%ngoyVersion%-all.jar
java -jar %ngoyJar% %*
