#!/bin/bash
cd /www/sites/we3c/tracker
git clone /home/git/repositories/we3c-tracker.git/
cd we3c-tracker
/opt/play-2.0.1/play clean compile stage
rm -fr app
rm -fr conf
rm -fr lib
rm -fr project
rm -fr *.sh
cd ..
rm -fr public
rm -fr target
mv -f we3c-tracker/* ./
rm -fr we3c-tracker
/opt/play-2.0.1/play stop
nohup /www/sites/we3c/tracker/target/start -Dhttp.port=9001 -Dconfig.resource=prod.conf &