## HipChat plugin for Jenkins

Started with a fork of the Campfire plugin:

https://github.com/jgp/hudson_campfire_plugin

## Mustache template updates

The plugin has been updated to use [Mustache.java](https://github.com/spullara/mustache.java) to
generate the HipChat notifications - this allows for easier customisation, as well as adding custom
 build variables to be output.

 The default templates are:

 * Start message: ```{{build.project.displayName}} - {{build.displayName}}: {{trigger}} {{{link}}}```
 * Complete message: ```{{build.project.displayName}} - {{build.displayName}}: {{status}} after {{build.durationString}}```

 There is also a per-Job configurable "Message suffix" - this is also a Mustache template and is
 used when you want to keep the same template overall and just override something that gets appended
 to the default messages.

 ### Available parameters

 #### Start message

 * **build** - instance of [AbstractBuild](http://javadoc.jenkins-ci.org/hudson/model/AbstractBuild.html) -
   most of the information comes from this object (eg. ```{{build.project.displayName}}```)
 * **trigger** - a string describing why the build was started - tries to use the "changes" if available, otherwise
  the "cause", and finally falls back to the string "Started..."
 * **link** - link to the build in Jenkins - note the triple-bracket to prevent HTML from being escaped

 #### Completed message

 * **build** - see above
 * **status** - string describing the state of the build (eg. success, fail, etc)
 * **link** - link to the build in Jenkins

 ### Using custom build parameters

 Custom build parameters are available through ```{{build.buildVariables}}``` - for example if you have
 a parameter called ENVIRONMENT then you would use ```{{build.buildVariables.ENVIRONMENT}}```.