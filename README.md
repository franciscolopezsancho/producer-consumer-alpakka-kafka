 
# the idea behind
The gist of this app is to automate the creation of some objects to push them 
to a topic and read them later on. This can be used to write into one Kafka topic that another app processes and puts in a different topic. That way this app can prove all (or maybe part) of the initial objects have been processed correctly. 



 

# Running the app 
to run the application start `sbt` and then 
	runMain localhost:9092 initTopic finalTopic earliest groupIdX

1. the bootstrap server
2. the topic to write to
3. the topic to read from
4. the offset to start reading from
5. the group id of the reader 



 # Running the tests

 An environment with kafka must be availabe, this will provide that. 

 https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html#step-1-download-and-start-cp-using-docker

 run `docker-compose up` under the download and create the topic "mytopic". I think is possible through the browser at localhost:9021. Then 'sbt test' should pass.




