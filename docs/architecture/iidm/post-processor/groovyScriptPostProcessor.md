# groovyScript
The groovyScript import post processor (`com.powsybl.iidm.import_.GroovyScriptPostProcessor` class) runs a Groovy script, providing the imported network (along with the computation platform) as input to the script (variables named `network` and `computationManager`). This way the Groovy script has full access to the imported network data, and it is able to work with and possibly change it.  
  
  
In order to run this post processor after the import of a network, add `groovyScript` to the list of post processors to be run, in the `postProcessors` tag of the [`import` section](../../../configuration/modules/import.md), in your [configuration file](../../../configuration/configuration.md)  

### YAML
```yaml
import:
    postProcessors: groovyScript
```

### XML
```xml
<import>
    <postProcessors>groovyScript</postProcessors>
</import>
```

The Groovy script to be run by the post processor can be [configured](../../../configuration/modules/groovy-post-processor.md) in the [configuration file](../../../configuration/configuration.md).  
If not defined in the configuration, the post processor looks for a Groovy script named `import-post-processor.groovy` in the [configuration folder](../../../configuration/configuration.md) 

A sample Groovy script post processor for increasing the active power of all loads of an imported network can be found [here](../../../samples/groovyScriptPostProcessor/).  