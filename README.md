# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.example.tradestore..Trade_Demo' is invalid and this project uses 'com.example.tradestore.Trade_Demo' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.5/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.5/gradle-plugin/packaging-oci-image.html)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

### Testing locally
To run application locally

* docker compose up -d postgres kafka zookeeper
* ./gradlew bootRun
* Run the tests using ./gradlew test
* Access H2 console at http://localhost:8080/h2-console
  * JDBC URL: jdbc:h2:mem:testdb
  * User Name: sa
  * Password: (leave blank)
* Access mongo docker exec -it <mongo-container-name> mongosh
  * use trade_store
  * db.trade.find().pretty()
