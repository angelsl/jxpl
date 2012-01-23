# jxpl - The __j__ava__x__.scripting __P__lugin __L__oader

jxpl is a plugin providing a plugin loader that loads whatever script engines work with javax.script, and are Invocable.

## End-user Usage

Place scripts in the scripts/ directory in the root directory (where plugins/ is).  Do not place scripts in plugins/.

## Developer Usage

Examples are in the [jxpl-scripts repository](https://github.com/angelsl/jxpl-scripts).  It should contain everything you need to get started. jxpl comes with the latest version of Rhino shaded in the jar. Should you want to script using other languages (provided they work with ScriptEngineManager in javax.script), you will need to include the framework in your classpath yourself.

## Some available languages

You will need to download the jar for the language you want to run a script in.

* __JavaScript__
  * Mozilla Rhino 1.7R3, bundled with jxpl
* __[Python 2.5 (Jython)](http://www.jython.org/downloads.html)__
  * You'll need to download the installer, run it and create a standalone jar.
* __[Ruby 1.9 (JRuby)](http://www.jruby.org/download)__
  * From the download page you can get the file "jruby-complete-1.6.5.jar".
* __[Groovy](http://groovy.codehaus.org/Download)__
* __[Lua](http://code.google.com/p/jnlua/downloads/list)__

### Installing script engines

Simply drop the JAR into `plugins/jxpl/lib/` and restart your server.

## Donations

If you wish to donate, you can buy me one of these:

  * A Namecheap domain
  * Xbox Live Gold Subscription
  * A VPS or Dedicated server
  
Heh.