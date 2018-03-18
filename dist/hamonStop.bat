@echo off
call mvn exec:java -Dexec.mainClass=org.gmnz.clog.Main -Dexec.args="-shutdown"

call mvn exec:java -Dexec.mainClass=org.gmnz.hamon.Main -Dexec.args="-shutdown"
