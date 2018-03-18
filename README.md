# Hamon

A simple http common log file parser.




## What it is

Parses a log file in [common log format](https://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format "reference") and displays general information on the system console.

It is composed of two components:
* the monitor/parser itself,
* a tool for generating a compliant log file with  with random yet reasonable data




## What it does

The monitor displays at fixed time intervals (approximately 10 seconds) a summary of the traffic obtained by computing the target log file, while it's being actively written to.

(I've also tested it with a simple Tomcat 8.5 instance, while moving around its predefined deployed web apps).

When the total traffic within a 2 minutes time window exceeds a threshold value (`-t` parameter, in bytes) you get a red warning on the top of the console. THe warning stays fixed on the top of the screen 

When the total traffic drops back below the threshold, another message is added after the last message, in a downward growing stack fashion.




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

When the build completes, a single jar will be put into the `./dist` directory, whereas the dependencies will be put into `./dist/lib`

Use `mvn package` (don't mind the stack traces, they're part of successful tests), or `mvn package -DskipTests` to skip tests.


## Parameters



### Hamon component
 `-f` : name of the log file that you want to keep under observation
 `-t` : traffic threshold (in bytes) over which to display alarms
 `-shutdown` : stops all the facilities and closes the program


### Log generation component
`someFileName` : name of the file to which the randomly generated log lines will be written to
`-shutdown` : stops all the facilities and closes the program




## Running 

Before running, check that no processes are listening to the ports 19756 and 19757. These ports are used as listening sockets for issuing shutdown command for the log generator process and the log parser.
 
Once you've built the project, you have multiple choices.

**Note:** Always build the project beforehead.

### Using the provided batch files

**Note:** running the tool this way will result in seeing a demo. The random log file producer and the monitor will be spawned with some default values for your convenience. Let it run for 5-6 minutes and you should see the stats flowing as well as some alarms being raised and reset.

From the command prompt, launch the `hamonStart.bat` file.

It will spawn both the random log generator process and the monitor as well, with some default options.
To stop both processes, launch the `hamonStop.bat` file.



### Using the binaries in the `dist` directory

We'll be using Windows' `start` command, since both the log generator and the log parser will spawn their own console window. It also leaves your command prompt free for issuing the shutdown commands.

Once you changed into the `dist` directory

to start the log generation process use the command:

	start "logGen" java -cp hamon.jar org.gmnz.clog.Main <yourLogFileName>
	
to start the monitoring process use the command:

	start "hamon" java -cp hamon.jar org.gmnz.hamon.Main -t <trafficThreshold> -f <yourLogFileName>
	
**Warning**. The target log file _MUST_ exist, otherwise the monitor will exit with an exception.
 
Running processes are stopped respectively with the following commands:

	java -cp hamon.jar org.gmnz.clog.Main -shutdown
	java -cp hamon.jar org.gmnz.hamon.Main -shutdown


### Using maven

You can start the tools from the directory where the pom.xml file resides.

The only drawback of this method is that you end up cluttering the project root directory with the files produced during the processing.
 
Starting the processes:

	start "logGen" mvn exec:java -Dexec.mainClass=org.gmnz.clog.Main -Dexec.args="<yourLogFileName>" 	
	start "hamon" mvn exec:java -Dexec.mainClass=org.gmnz.hamon.Main -Dexec.args="-t <trafficThreshold> -f <yourLogFileName>"

Stopping the processes:

	mvn exec:java -Dexec.mainClass=org.gmnz.clog.Main -Dexec.args="-shutdown"
	mvn exec:java -Dexec.mainClass=org.gmnz.hamon.Main -Dexec.args="-shutdown"


## General architecture

### Hamon

The flow of data is rather simple. It comes from the logfile itself, it's parsed and persisted to some storage. It is then picked up by an analysis component which checks for thresholds and other data, and then is sent to the console management component for displaying.

In this first version I've used an embedded HSQLDB as the local data repository.

#### Sampler
Its role is to periodically check the monitored file for new lines being added. Within every sampling period, if there are new lines, they're gathered into a batch and sent to the parser for actual parsing.

#### Parser
Parses the log lines sent by the sampler and breaks them into the required components. The parsed data is then sent to the persistent storage.

Particular attention has been put in making the parser distinguish the request made on the root section while stripping information about requesting resources found on the root section. (i.e. requests made towards "/" and "/saitama.js" or "/favicon.ico" should be recognized as requests made to the root secion).

#### Analyzer
The analyzer is the bridge between the data repository and the output facility. It uses two separate processes to gather generic statistics and traffic information from the database, does the necessary computations to determine whether an alarm should be raised or reset and then communicates with a console controller, whose role is to display the results.

#### ConsoleOutput
Displays the various information sent by the Analyzer. The output is periodically updated with the following information:
* generic statistics about the total logged traffic, near the lower margin of the screen,
* alarms, whenever they are raised and reset, as a downward-growing list beginning at the top of the screen (see `known bugs` later in this document).

Here is some sample output taken from what you can see if you start the tools using the batch files

	.----- status update at 18/mar/2018:22:02:58 +0100 (1155 s elapsed since start)
	Most requested sections: <Aldebaran> (1212 hits) | <Saga> (1109 hits) | <ROOT> (1057 hits)
	total site hits <11638> | topTraffic <127.94.197.64> (8791813)
	topSc200 <127.94.197.64> (251) | topSc302 <177.223.136.121> (206)
	topSc5xx <217.129.241.133> (8) | topSc4xx <123.152.129.6> (192)
	Approximate request methods breakdown: HEAD (20,9%) | DELETE (14,8%) | POST (20,3%) | GET (21,2%) | OPTIONS (9,5%) | PUT (13,3%)
	'-----

From top to bottom, the traffic statistics are organized as follows:
* update timestamp and total time elapsed since the application was started
* list of the most requested site sections, each with the number of hits
* total hits detected in this session (that's practically the number of lines in the log file...), followed by the ip which generated the highest traffic
* The ip address for which the server responded with http status code "ok" (200), followed by the one which got the highest number of redirects
* the ip address for which the server responded with a server side error (500 < status code < 599), and the one who got the highest number of client request error  (400 < status code < 499)
* an approximate percentage breakdown of all the http methods detected in the log

On the upper side of the console you will see the alarms being raised and (hopefully) reset during the monitor operations. The alarm shows the time at which it was raised, the detected traffic value, its relation with the set traffic threshold and a percentage of overflow.

When an alarm is reset, another line is produced, indicating the timestamp, the traffic value, and its fraction of the traffic threshold as a percentage.


### The random log generator tool

Its task is to procude a log file in the expected format, but with time-variable frequencies. I purposefully did not let it accept parameters other than the target log file name, since this tool is intended to use as a demo.

The random log line generation logic is in the class `org.gmnz.clog.ClfLineGenerator`, you can check it to see how I played with (pseudo) random numbers to generate a spectrum of requests as most variable as possible.

The thread which drives the generation of the log lines operates in a two-state fashion; it has a running period of 3 minutes, with alternating states of low and high traffic generation, set in a duty cycle of 35%. Means: it's programmed to emit low traffic (generates 2 reqs/second) for 13/20 of the 3 minutes time window and emits high traffic (15 reqs/second) for the remaining time. This assures an alternating behavior of alarms being raised and reset with respect to a threshold of 20000000 bytes.

  








**TODO TERMINARE** 



## Improvements

A raised alarm should get updated for you to check how much the total traffic goes above the threshold you set. Every update should be marked with its timestamp, while the displayed alarm line retains the timestamp of when the alarm was first issued.




	
	



 
