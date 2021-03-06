# Distributed url-shortener service
A distributed url-shortener service that focuses on availability and partition tolerance.



## Dependencies
Some of the dependencies are not in Maven Central, so you will need to download them and install in your local Maven repository. These dependencies are [Consistent Hashing](https://github.com/Balzu/Consistent-Hashing), [Gossiping](https://github.com/tonellotto/Distributed-Enabling-Platforms/tree/master/gossiping) and [Versioning](https://github.com/tonellotto/Distributed-Enabling-Platforms/tree/master/versioning). The procedure to follow is the same for each dependency, so let's show only for Consistent Hashing. You have to do only

`git clone https://github.com/Balzu/Consistent-Hashing.git`  
`mvn install`  

## Build

Now you can download and build the project:

`git clone https://github.com/Balzu/url-shortener.git`  
`cd url-shortener`  
`mvn clean package`  

## Usage

Then you have to run the url-shortener service. This setups a cluster of nodes that all implement the url-shortener service. It is possible to tweak the system by providing a custom configuration file, otherwise the default one will be used. To start the service:

`cd core`  
`java -jar target/core-1.0-SNAPSHOT-jar-with-dependencies.jar -n <node_id> [-c <configuration_file>] `  

where

* `-n` specifies the id of the node you want to turn on, where the id corresponds to the index of the node in the "nodes" array of the configuration file;
* `-c` allows to provide a custom configuration file. It must be named `core.conf` and must be placed in the `core/src/main/resources` folder

Finally you can run the client to actually use the service. Three APIs are provided:

* `PUT` to shorten a url
* `GET` to retrieve the original url, giving she shortened url as input
* `REMOVE` to remove the pair <original_url, shortened_url >

It is possible to run the client either in interactive or batch mode. To run the client:

`cd client`  
`java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar {-p <original_url> | -g <shortened_url> | -r <shortened_url> | -i} [-c <configuration_file>] [-o <output_file>]`  

where

* `-p` is used to PUT
* `-g` is used to GET
* `-r` is used to REMOVE 
* `-i` starts an interactive session
* `-c` allows to provide a custom configuration file. It must be named `client.conf` and must be placed in the `client/src/main/resources` folder
* `-o` is used to write the output in an output file

## Limitations

There are two main limitations above the others:

* **Replica management**: I designated the project with scalability in mind, that's why I avoided a replication strategy based on multicast. Anyway the solution I devised offers no way to solve replication problems in a general way, so I had to patch all the bugs by hand.

* **eventual consistency**: in some cases, especially in case of failure of some node, the system takes a lot of time to reach consistency. This is what happens in the tests: usually they work as expected, but when they fail it is because the system has not reached consistency yet

## TODO
Although the system works quite well, last changes broke some tests. There is also some minor bugs that still has to be fixed.


