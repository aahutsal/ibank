FROM mbopm/ubuntu-oracle-java

COPY target/ibank*.jar /data/

CMD ["java","-jar", "/data/ibank-1.0-SNAPSHOT.jar"]
