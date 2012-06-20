#!/bin/bash
cd /www/sites/properties.superyachts.com/test-properties
git clone git@helios:properties.git
cd properties
/opt/Play20/play clean compile stage
rm -fr app
rm -fr conf
rm -fr lib
rm -fr project
rm -fr *.sh
cd ..
rm -fr public
rm -fr target
mv -f properties/* ./
rm -fr properties
/opt/Play20/play stop

if [ -f RUNNING_PID ]
	then
		runningpid=$(cat RUNNING_PID)
		kill -9 $runningpid
		rm RUNNING_PID
fi

nohup /www/sites/properties.superyachts.com/test-properties/target/start -Dhttp.port=9005 -Dconfig.resource=test.conf &