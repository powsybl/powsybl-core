# Import Post Processors

powsybl-core, using import post processors, provides a mechanism that allows to perform operations on networks after they have been imported.  
Some of the available import post processors allow, e.g., to run a script that can possibly work on the imported network.  
The import post processors mechanism can also be used to create new post processors.  
  
In this tutorial you are going to see how to work with import post processors, learning:
 - How to use import post processors
 - [How to create a new import post processor](../../../tutorials/iidm/howto-extend-postprocessor.md)

## How to use import post processors  
An import post processor, if properly configured, is run on a network, after the import of the network itself. The goal of a post processor is to work on the imported network, possibly changing data.   
Each post processor is identified by its name.  
The list of post processors currently implemented and available in powsybl-core is:
- [groovyScript](groovyScriptPostProcessor.md)
- [javaScript](javaScriptPostProcessor.md)
- [loadflowResultsCompletion](loadflowResultsCompletion.md)

The powsybl-core platform reads from the configuration all the post processors to run, and, just after a network is imported (e.g. when you run an itools command that import a network), calls the post processors.    
the import post processors to run are defined in powsybl's [configuration file](../../../configuration/configuration.md).  

```xml
<import>
    <postProcessors>groovyScript,javaScript</postProcessors>
</import>
```

In the `import` section, the `postProcessors` tag contains the list of post processors to run. The post processors are run according to the order specified in the `postProcessors` tag.      
  
Some post processor may have or require a further configuration (e.g. [groovyScript](groovyScriptPostProcessor.md)).