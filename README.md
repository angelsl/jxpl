# jxpl - The javax.scripting Plugin Loader

jxpl is a plugin providing a plugin loader that loads whatever script frameworks work with javax.script, and are Invocable.

## End-user Usage

Place scripts in the scripts/ directory in the root directory (where plugins/ is).  Do not place scripts in plugins/.

## Developer Usage

An example script is in the git repository as well as here.  It should contain everything you need to get started.  Note that Java comes with only Mozilla Rhino 1.6R2, and nothing else.  Should you want to script using other languages (provided they work with ScriptEngineManager in javax.script), you will need to include the framework in your classpath yourself.

## Some available languages

You will need to download the jar for the language you want to run a script in.

* ### JavaScript
 * Mozilla Rhino 1.6R2, included with Java 6
 * [Mozilla Rhino 1.7R2](http://www.mozilla.org/rhino/download.html) 
 
* ### [Python 2.5 (Jython)](http://www.jython.org/downloads.html)
 * You'll need to download the installer, run it and create a standalone jar.
 
* ### [Ruby 1.8 (JRuby)](http://www.jruby.org/download)
 * From the download page you can get the file "jruby-complete-1.6.0.RC2.jar".
 
* ### [Groovy](http://groovy.codehaus.org/Download)

* ### [Scheme (Kawa)](http://www.gnu.org/software/kawa/)

## Craftbukkit startup script

### For usage with Jython and other engines in the form of JARs

* Linux
 * `java -server -Xms2048M -Xmx3072M -cp myengine1.jar:myengine2.jar:craftbukkit.jar org.bukkit.craftbukkit.Main`
* Windows
  * `java -server -Xms2048M -Xmx3072M -cp myengine1.jar;myengine2.jar;craftbukkit.jar org.bukkit.craftbukkit.Main`