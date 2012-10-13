#!/bin/sh                                                                                                                                                                                                                        

RSYNC=/usr/bin/rsync 
SSH=/usr/bin/ssh 
RUSER=ubuntu 
RHOST=wethreecreatives.com
RPATH1="/www/sites/we3c/tracker/"
LPATH="/media/ext3/www/htdocs/work/we3c/tracker/"

$RSYNC -rvz --include-from=rsync_include.txt --exclude=* -e $SSH  $LPATH $RUSER@$RHOST:$RPATH1

