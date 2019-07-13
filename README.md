
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)

This fork of SinnerSchrader's [SkillWill](https://github.com/sinnerschrader/SkillWill) is intended to adapt it to the needs of [mindmatters](https://mindmatters.de). The necessary changes include:
* removing dependencies to external infrastrucure (except Google Loing APIs)
* applying custom stylesheets to match mindmatters' CD
* removing the admin role: every user is allowed to make global changes
* removing the ability to hide skills (as this added complexity without being useful...)


# What exactly is SkillWill?
![screenshot](screenshot.png)

SkillWill is a simple tool to track what people know (their skills) and what people want to do (their wills).
Every user can define their personal levels of interest and knowledge for each skill in the system; anyone can search for persons by those skills, e.g.
* You want somebody to teach you programming language x? → Search for x and see who can help.
* You're a project manager and need somebody who can do x? → Search x foobar and you get a list of candidates.
* You want to get better at x? → Show your interest for x and people who need some to do x will find you.
* ...



# Development Setup

## Infrastructure
If you don't want to used the dockerized development setup, you'll need to have some stuff installed to build and run skillwill locally:
* Java 12
* maven
* A local [MongoDB](https://www.mongodb.com/)

## Building
Variant 1: Docker Compose 
  * run `docker-compose up --build`
  * the application starts on port 8080

Variant 2: Build locally
* run `scripts/build-start-local.sh`


## Deploy
The app is deployed via heroku, assuming you have an account and have the required permissions to deploy the project, just run:
`scripts/deploy.sh`



# Important URLs
* `/`: Application main view
* `/swagger`: Interactive API documentation
* `/actuator/info`: Show application-specific stats (# of users, skills per user, etc.)



# Code Style Guidelines

## Backend
* [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
  * [IntelliJ Config](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
  * [Eclipse Config](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)
* Exceptions:
  * Maximum of 100 characters per line will _not_ be enforced.
  * Add one unit of vertical whitespace (aka one empty line) after _multi-line_ method signatures.

## Frontend
* most of all, have some common sense
* There are no hard and fast rules right now, but style guidlines were established over the last month:
  * We dont use *semi colons* at the end of a line unless needed
  * we favor *destructuring* of objects over repeating this and props
  * there are two spaces inside curly braces e.g. ~~{foo}~~ should be **{ foo }**
  * no spaces at the end of a line and no trailing commas
  * it's ok to use the implicit return of the arrow function
  * please use more expressive names for functions and variables than 'e', 'el', 'data'...
  * no deeply nested ternaries
  * every function should have a single purpose
  * To quote Robert Martin

> Functions should have a small number of arguments. No argument is best, followed by one, two, and three. More than three is very questionable and should be avoided with prejudice.



# License
* [MIT](https://opensource.org/licenses/MIT) (see LICENSE.md)
