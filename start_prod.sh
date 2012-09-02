#!/bin/bash

startService () 
{
	stopService
	cd /www/sites/we3c/tracker
	su ubuntu -c "nohup /www/sites/we3c/tracker/target/start -Dhttp.port=9001 -Dconfig.resource=prod.conf &"
}
stopService () 
{
	cd /www/sites/we3c/tracker
	if [ -f RUNNING_PID ]
		then
			runningpid=$(cat RUNNING_PID)
			kill -9 $runningpid
			rm RUNNING_PID
	fi
}
rebuildService () 
{
	cd /www/sites/we3c/tracker
	git clone /home/git/repositories/we3c-tracker.git/
	cd properties
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
}

case $1 in
	start)
		startService
	;;
	stop)
		stopService
	;;
	rebuild)
		rebuildService	
		
	;;
	*)
		log_success_msg "Usage: {start|stop|rebuild}"
		exit 1
	;;
esac