## Campfire plugin for Hudson

This is a fork of the plugin developed by Jens Lukowski. More information about
the original plugin is available from the [Hudson
wiki](http://wiki.hudson-ci.org/display/HUDSON/Campfire+Plugin) and [this blog
post from
Jens](http://schneide.wordpress.com/2009/10/26/a-campfire-plugin-for-hudson/).

Development of the original plugin seemed to stall, so I forked it to address a
number of issues and add some extra features...

* Refactored the code to fix a number of null pointer exceptions.
* Moved from per-job to global config.
* Fixed issues with configuration details being lost after a hudson restart.
* Tidied up jelly view for configuration form and added help files for each 
  field.
* Added a link to the build in notifications sent to campfire.

Thanks to Joshua Krall, we now have support for SSL, and the plugin works after
upgrading to Hudson ver. 1.363

Recently, Brad Greenlee added a smart notification feature, which disables
notifications for successful builds unless the previous build was unsuccessful,
and I added commit info to build notifications and an option to customize the
room to which notifications are sent per-project.

Note: The plugin code is a bit of a mess, partly just because I don't have a 
lot of Java experience, but also because I simply haven't got the time to tidy
it up. It does work though, and we use it daily without any trouble.

### Installation 

You'll need to have JDK 6 and maven2 installed to build the plugin. This should
be as simple as asking your package manager to install maven2, e.g.

    sudo port install maven2

Then clone the repository and build the package

    git clone git://github.com/jgp/hudson_campfire_plugin.git 
    cd hudson_campfire_plugin 
    mvn package

When the build has completed, you'll find a campfire.hpi file in the target
directory, which needs to be uploaded to your Hudson installation. If you
already have a campfire plugin installed, you need to delete it first, e.g.

    rm -rf /var/lib/hudson/plugins/campfire*

Then either use the advanced tab of the plugin manager to upload the hpi file or
just copy it to the plugins directory, e.g. 

    cp target/campfire.hpi /var/lib/hudson/plugins/

Finally, restart hudson (note: not reload configuration, restart the hudson
daemon).

### Troubleshooting

If you run into problems building the plugin with Maven, make sure Maven is
finding the right jdk...

run mvn --version and check the output, if it's not finding jdk 6, make sure you
have jdk 6 installed and make sure that the current jdk symlink points at
version 6.  On OSX, check that
/System/Library/Frameworks/JavaVM.framework/Versions/1.6 exists and that
/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK points to it.
If not, remove the CurrentJDK symlink and re-create it, pointing at the 1.6
directory. Other *nix users may run into similar issues, the solution should be
the same, just with different paths.

If you get HttpClient or WebClient exceptions, that probably means you've got
some configuration setting wrong (while there is some validation of
configuration settings, it's far from extensive).
