iBank project for CrossOver
===========================

* Overview

Project is organized and stored on Gihub as main Java project and custom docker-activemq submodule. Custom docker-activemq support securing openwire transport connection through SSL. So we strongly recommend using it when you run activemq in docker.

* Application could run as IBANK server (generate fake activity between following accounts) IBANK_IBANK=1 env variable should be set to run in that mode
* DAS server (peform transaction interception, analysis, storing to Mongo database) IBANK_DAS=1 should be set to run int that mode

Single application instance could run in 2 modes at time

* To build submodule use:

   cd docker-activemq
   docker build -t activemq .

* To run ActiveMQ as docker container use:
   docker run --rm -ti -e 'ACTIVEMQ_STATIC_QUEUES=queue1;queue2;queue3' -e 'ACTIVEMQ_MIN_MEMORY=1024' -e  'ACTIVEMQ_MAX_MEMORY=4096' -e 'ACTIVEMQ_ENABLED_SCHEDULER=true' -v /data/activemq:/data/activemq -v /var/log/activemq:/var/log/activemq -p 8161:8161 -p 61616:61616 -p 61613:61613 softsky/activemq

   please, note, submodule should be initially built with `docker build -t activmeq .`

* To run MongoDB as docker container use:
   docker run -v ~/tmp/data:/data/db -p 27017:27017 mongo

* To build main project use

    mvn clean package

* To run this project from within Maven use:

   a) IBANK server
      IBANK_IBANK=1 IBANK_ACTIVEMQ_USER=admin IBANK_ACTIVEMQ_PASSWORD=admin mvn exec:java

   b) DAS server
      IBANK_DAS=1 IBANK_ACTIVEMQ_USER=admin IBANK_ACTIVEMQ_PASSWORD=admin mvn exec:java

   c) BOTH
      IBANK_IBANK=1 IBANK_DAS=1 IBANK_ACTIVEMQ_USER=admin IBANK_ACTIVEMQ_PASSWORD=admin mvn exec:java

Since DAS use MongoDb heavily it will alway look up Mongo server on localhost. However, ActiveMQ message queue could be share among multiple IBANK/DAS
running instances.

* Securing ActiveMQ transport connection

Just pass:
     IBANK_ACTIVEMQURL=ssl://localhost:61616

as environment variable

* To generate reports run:

   curl -XPOST http://localhost:8181/reports?completed -d "{'from.customer': 'Lena' }"
   curl -XPOST http://localhost:8181/reports?completed -d "{'to.customer': 'Lena' }"

Same to see failed transactions:

   curl -XPOST http://localhost:8181/reports?failed -d "{'from.customer': 'Lena' }"
   curl -XPOST http://localhost:8181/reports?failed -d "{'to.customer': 'Lena' }"


Please, note DAS server should be running



* Packaging:

For more help see the Apache Camel documentation

    http://github.com/aahutsal/ibank/

