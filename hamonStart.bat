@echo off
start "logGen" mvn exec:java -Dexec.mainClass=org.gmnz.clog.Main -Dexec.args="random.log"
start "hamon"  mvn exec:java -Dexec.mainClass=org.gmnz.hamon.Main -Dexec.args="-t 1200 -f random.log"
