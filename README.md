Example order processing matching engine. Interface for the engine is a rest based api using Jersey. 

Build:

maven clean install

How to run in Tomcat:

1) Download tomcat and untar.
2) Run <tomcat>/bin/startup.sh
3) cd <your github path>/orders
3) mvn clean install 
3) Copy cp target/orders.war to <tomcat>/webapps directory.

Example curl:

curl localhost:8080/orders/sell --data '{"qty":10,"prc":15}' -H "Content-Type: application/json"

