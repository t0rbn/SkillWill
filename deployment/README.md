# Prerequisites
* get Accounts/Credentials
  * technical LDAP user
  * google oauth client_id and client_secret
  * SSL Certificate
* get a VM to run all the stuff on
  * static IP needed
  * have it whitelisted for LDAP access
* install
  * nginx
  * Java Runtime (>= 1.8)
  * MongoDB

  
# Configuration

## MongoDB
* should run on standard port (27017)
  * Port needs to be configured in the application (.properties file)
* secure with password

## OAuth Proxy
* config in ```oauth_proxy/oauth_proxy.conff```
* set redirect url (aka domain the app runs on)
* set upstream
  * connection to backend
  * ```http://localhost:1337``` by default
* set ```client_id``` and ```client_secret``` (get from google developers console)
* set ```cookie_secret``` (this will be used to generate cookie hashes, can be anything)

## nginx
* example config in ```nginx/skillwill.conf```
* proxy pass requests to backend
* direct location ```/auth2/``` to oauth proxy
* serve files in ```oauth_proxy/resources``` to ```/auth-resources``` and exclude from auth
  * assets used to for sign-in and error template
* include SSL certificate
* force redirect from http to https

## Application (application-production.properties)
* set technical LDAP user in properties
* set LDAP url (should be right by default)
* set MongoDB url (localhost:27017 (standard port) by default)

# Start all the things
* nginx: ```systemctl start nginx```
* MongoDB ```systemctl start mongodb.service```
* Proxy: ```oauth2_proxy --config=/path/to/config``` or systemd unit
* Application ```java -jar -Dspring.profiles.active=production skillwill.jar``` or systemd unit


