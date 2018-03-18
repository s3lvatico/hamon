# hamon
simple http clf log parser


## What it is

Parses a log file in [common log format](https://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format "reference") and displays general information on the system console.
Comes with a simple tool that you can use to generate a fac-simile log file with random yet reasonable data.

## How it was made


*   Java 1.7
*   Windows 10 (sufficiently updated)


------------------------------------------------------------

**Note:** Since nowadays Windows 10 has a very poor support for console management, as well as the JVM facilities as a whole, it is recommended to use a plain Windows command prompt and to configure its window to be about 43 lines by 100 columns, so that you won't be experiencing fuzzy output. 

## Installation

1. create your target directory and change into it
2. clone the repository at GitHub 
	
	git clone https://github.com/s3lvatico/hamon.git



## Building

When the build completes, binaries and the dependencies will be put into the `dist` directory.

Use `mvn package` (don't mind the stack traces, they're part of successful tests), or `mvn package -DskipTests` to skip tests.

## Running

Before running, check that no processes are listening to the ports 19756 and 19757. These ports are used as listening sockets for issuing shutdown command for the log generator process and the log parser.
 
Once you've built the project, you have multiple choices.

### Using the provided batch files

From the command prompt, launch the `hamonStart.bat` file.

It will spawn both the random log generator process and the monitor as well, with default options.
To stop both processes, launch the `hamonStop.bat` file.

### Using the binaries in the `dist` directory

We'll be using Windows' `start` command, since both the log generator and the log parser will spawn their own console window. It also leaves your command prompt free for issuing the shutdown commands.

Once you changed into the `dist` directory, use the command:

	start "logGen" java -cp hamon.jar org.gmnz.clog.Main <yourLogFileName>
	
to start the log generation process, and the command	

	java -cp hamon.jar org.gmnz.hamon.Main -t <trafficThreshold> -f <yourLogFileName>
	
to begin the monitoring.

**Warning**. The target log file _MUST_ exist, otherwise the monitor will exit with an exception. 

#### Parameters explanation

 
The monitor displays at fixed time intervals (approximately 10 seconds) a summary of the traffic obtained by computing the target log file, while it's being actively written to.

The `-f` parameter is simply the log file name that you want to keep under observation.

(I've tested with a simple Tomcat 8.5 instance, while moving around its predefined deployed web apps).

When the total traffic within a 2 minutes time window exceeds a threshold value (`-t` parameter, in bytes) you get a red warning on the top of the console. THe warning stays fixed on the top of the screen and gets updated for you to check how much the total traffic goes above the threshold you set. Every update is marked with its timestamp, but the line retains the timestamp of them the alarm was first issued.

When the total traffic drops back below the threshold, another message is added after the last message, in a downward growing stack fashion.











	
	



 
