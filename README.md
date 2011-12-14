# jxpl - The __j__ava__x__.scripting __P__lugin __L__oader

jxpl is a plugin providing a plugin loader that loads whatever script engines work with javax.script, and are Invocable.

## End-user Usage

Place scripts in the scripts/ directory in the root directory (where plugins/ is).  Do not place scripts in plugins/.

## Developer Usage

Examples are in the [jxpl-scripts repository](https://github.com/angelsl/jxpl-scripts).  It should contain everything you need to get started.  Note that Java comes with only Mozilla Rhino 1.6R2, and nothing else.  Should you want to script using other languages (provided they work with ScriptEngineManager in javax.script), you will need to include the framework in your classpath yourself.

## Some available languages

You will need to download the jar for the language you want to run a script in.

* ### JavaScript
  * Mozilla Rhino 1.6R2, included with Java 6
  * [Mozilla Rhino 1.7R3](http://www.mozilla.org/rhino/download.html) 
 
* ### [Python 2.5 (Jython)](http://www.jython.org/downloads.html)
  * You'll need to download the installer, run it and create a standalone jar.
 
* ### [Ruby 1.9 (JRuby)](http://www.jruby.org/download)
  * From the download page you can get the file "jruby-complete-1.6.5.jar".

* ### [Groovy](http://groovy.codehaus.org/Download)

### Installing script engines

Simply drop the JAR into `plugins/jxpl/lib/` and restart your server.

## Donations

If you wish to donate, you can buy me one of these:

  * A Namecheap domain
  * Xbox Live Gold Subscription
  * A VPS or Dedicated server
  
Heh.