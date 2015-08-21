# poc-google-mock-mongodb

An example of mocking google API (currently only regions and zones).
The application is using techniques like scala, play2, reactivemongo, mongodb.

#### 1. Pre-requisites
Scala: 2.11.7</br>
Playframework: 2.4.x (with activator)
Mongodb: 3.0.5

#### 2. Compile and Run
Before run the application, mongodb is required to be run first (refer to [MongoDB Installation Guide](http://docs.mongodb.org/manual/installation/) ):</br>
[command] mongod --dbpath "<some path>"
Compile and run poc-google-mock-h2 through play-activator:</br>
[command] cd </br>
[command] activator</br>
Inside activator - Basic steps to run the application:</br>
[command] clean</br>
[command] compile</br>
[command] run
###### Note: check the port used by mongodb, and you can change the connection configuration from conf/application.conf

#### 3. Edit by IDE (eclipse, intelliJ Idea)
Inside project/plugins.sbt, sbt-eclipse and mpeltonen-idea have already been added to the plugins.</br>
Generate Eclipse Project:</br>
[command] eclipse</br>
Generate IntelliJ Project:</br>
[command] gen-idea

#### 4. Database configuration
conf/application.conf

#### 6. Generate test data
http://host:port/compute/v1/initialize

#### 5. Sample requests
http://host:port/compute/v1/projects/pocproj1/regions</br>
http://host:port/compute/v1/projects/pocproj1/regions/asia-east1</br>
http://host:port/compute/v1/projects/pocproj1/zones</br>
http://host:port/compute/v1/projects/pocproj1/zones/asia-east1-a
