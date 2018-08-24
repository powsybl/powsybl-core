# groovyScript
The groovyScript import post processor (`com.powsybl.iidm.import_.GroovyScriptPostProcessor` class) runs a Groovy script, providing the imported network (along with the computation platform) as input to the script (variables named `network` and `computationManager`). This way the Groovy script has full access to the imported network data, and it is able to work with and possibly change it.  
  
  
In order to run this post processor after the import of a network, add `groovyScript` to the list of post processors to be run, in the `postProcessors` tag of the `import` section, in your [configuration file](../configuration/configuration.md)  

```xml
<import>
    <postProcessors>groovyScript</postProcessors>
</import>
```

The Groovy script to be run by the post processor can be configured in the [configuration file](../configuration/configuration.md), in the `script` tag of the `groovy-post-processor` section

```xml
<groovy-post-processor>
    <script>$HOME/script.groovy</script>
</groovy-post-processor>   
```

if the configuration file does not contains the `groovy-post-processor` section, the post processor looks for a Groovy script named `import-post-processor.groovy` in the [configuration folder](../configuration/configuration.md) 