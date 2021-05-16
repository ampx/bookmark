# Under development

Development notes:

* 2021-05-09 - Started refactoring from a private project into stand-alone public project
* 2021-05-11 - Added logic for Java client mode
* 2021-05-12
  * Added Spring configurations
  * Added Grafana Json controller for server monitoring
* 2021-05-13
  * Updated pom to build a runnable server jar and client library jar

# Bookmark Service Overview
Service to persist and share ETL pipeline progress across different pipelines.  
The service can be used in a local mode or client-server mode.
In client-server mode, progress is saved to a centralized location that can then be 
consumed by other processes.
Centralized checkpointing service that lets you combine ETL pipelines across different languages.

# Benefits
Centralized checkpoint
Monitoring



# Server Monitor Notes
TBD

# build 
mvn clean compile package

**Content:**

* [Web API](docs/web-api.md)
* [Performance Profile](docs/performance.md)

