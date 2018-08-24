# javaScript
The javaScript import post processor (`com.powsybl.iidm.import_.JavaScriptPostProcessor` class) runs a JavaScript (using the Java Scripting API, see `javax.script.ScriptEngine`), providing the imported network as input to the script (setting the `network` attribute in the context, see `javax.script.ScriptContext`). This way the JavaScript has access to the imported network data, and it is able to work with and possibly change it.  
  
  
In order to run this post processor after the import of a network, add `javaScript` to the list of post processors to be run, in the `postProcessors` tag of the `import` section, in your [configuration file](../../../configuration/configuration.md)  

```xml
<import>
    <postProcessors>javaScript</postProcessors>
</import>
```

The JavaScript to be run by the post processor can be configured in the [configuration file](../../../configuration/configuration.md), in the `script` tag of the `javaScriptPostProcessor` section.

```xml
<javaScriptPostProcessor>
    <script>$HOME/script.js</script>
</javaScriptPostProcessor>   
```

if the configuration file does not contains the `javaScriptPostProcessor` section, the post processor looks for a JavaScript named `import-post-processor.js` in the [configuration folder](../../../configuration/configuration.md) 