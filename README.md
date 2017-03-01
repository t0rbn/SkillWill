# Infrastructure
You'll need the following things:
* A connection to the LDAP
  * not sure? ```ping auth.sinnerschrader.com```
* A local [MongoDB](https://www.mongodb.com/) running on the port defined in application.properties (default: 27017)
  * You can use an embedded MongoDB, see section `build` for details

# Build
* Have a local maven installation? `mvn clean install`
* Wanna use the maven wrapper? `./mvnw clean install`
* You want to use an embedded MongoDB? `mvn clean install -PmongoEmbedded`

# Start
* `java -jar target/skillwill.jar`
* If you chose to embed the MongoDB, the file is called `skillwill-embeddedmongo.jar`
* To start for production, start with production profile `java -jar -Dspring.profiles.active=production target/skillwill.jar`

# API Docs
* Start the application
* Open its root path `http://localhost:1337/`
* Enjoy the interactive documentation using [Swagger](http://swagger.io/)

# License
* [MIT](https://opensource.org/licenses/MIT) (see LICENSE.md) 

# Links
* [Jira](https://jira.sinnerschrader.com/secure/RapidBoard.jspa?rapidView=425)
* [Wiki](https://wiki.sinnerschrader.com/display/flowteam/SkillWill+-+Technisches)
