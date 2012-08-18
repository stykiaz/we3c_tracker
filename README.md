we3c_tracker
========

#### Realtime Mouse Tracking ####

Free web application allow webadministrators to generate useful statiscts based on how users navigate in their websites in real time.

[Google Group](https://groups.google.com/forum/?#!forum/we3c_tracker)
[Contributors](http://github.com/stykiaz/we3c_tracker/contributors)

### Examples ###

In directory examples are a few generated heatmaps as well as a session replay download file.

### Usage ###

Developed in Java using the PlayFramework2.
Currenly only test under Linux.
For database the application uses MongoDB. Everything is easily configurable from the application.conf.
Deployment scripts that I am using for different env. are provides as well.
Once the application is install, an administrator needs to be added in the admininistrators collection in MongoDB ( this is only for initialising ).
After that its a matter of adding dominas to the list and using the code provided.


Note: the project is in very early stage, meaning that installing for the first time might prove to be a pain, so feel free to contact me with any questions.


### Planned  ###

**All suggestions and bug fixes are welcome**

There are a few annoying bug in the system, and a few things ( related to the heatmaps) that need to be seriously optimized

