This is a few examples of HomeItem plugins. Any jars containing plugins
that are placed in this directory will be loaded by NetHomeServer at startup
and the plugin Items will be used by the NetHomeServer.

The source code of the examples contains comments describing how to write
your own HomeItem plugins.

To build the example decoder, simply issue the following commands from the 
command line:

javac -classpath ../../lib/server-1.0-SNAPSHOT.jar;../../lib/utils-1.0-SNAPSHOT.jar Example1.java Example2.java
jar cf ExamplePlugin.jar Example1.class Example2.class

The first line will compile the java source into .class-files and the second line will place those
.class-file in a .jar-file called plugin.jar. When the NetHomeServer starts it will scan this
library, find these .jar-files and load them and use the Items it finds in it.

Note! if you have installed NetHomeServer in the Program Files folder on windows, you may not have
write permissions to this folder. The simplest solution then is to uninstall it and reinstall it
somewhere else.