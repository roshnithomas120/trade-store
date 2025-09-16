Getting Started
Reference Documentation
For further reference, please consider the following sections:

Official Gradle documentation
Spring Boot Gradle Plugin Reference Guide
Create an OCI image
Additional Links
These additional references should also help you:

Gradle Build Scans – insights for your project's build
Testing locally
To run application locally

docker compose up -d postgres kafka zookeeper
./gradlew bootRun
Run the tests using ./gradlew test
Access H2 console at http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
User Name: sa
Password: (leave blank)
Access mongo docker exec -it mongosh
use trade_store
db.trade.find().pretty()
