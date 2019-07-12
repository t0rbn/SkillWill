#!/bin/bash

heroku login
heroku container:login

cd ../
heroku container:push web -a mindmatters-skills-beta
heroku container:release web -a mindmatters-skills-beta
