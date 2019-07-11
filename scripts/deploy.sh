#!/bin/bash

heroku login
heroku container:login

cd ../
heroku container:push web -a skillwill-alpha
heroku container:release web -a skillwill-alpha
