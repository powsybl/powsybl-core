# Tutorial - How to write an import post processor

`ImportPostProcessor` is an interface which can be used to modify a network model, right after it's been read by an [importer](../../docs/architecture/iidm/importer/README.md).  
powsyble-core includes [some implementations](../../docs/architecture/iidm/post-processor/README.md) of this interface and allows to create new ones.   
  
In this tutorial you will see how to write a new post processor, for increasing the loads' active power of a network by a fixed percentage: 
- through a `Groovy` script, using the [groovyScript](../../docs/architecture/iidm/post-processor/groovyScript.md) post processor
- through a `JavaScript` script, using the  [javaScript](../../docs/architecture/iidm/post-processor/javaScript.md) post processor
- implementing a new import post processor, in a dedicated java module

Groovy script and java module post processor, will execute also a loadflow. 

# Groovy script (for the groovy script post processor)
The Groovy script can be found [here](../../samples/groovyScriptPostProcessor/increase-active-power-postprocessor.groovy).

You have to: 

1. Write a `Groovy` script that implements the processor's business logic. 

    ```groovy
    package com.powsybl.samples.groovyScriptPostProcessor
    
    import com.powsybl.iidm.network.Load
    import com.powsybl.iidm.network.Network
    import com.powsybl.loadflow.LoadFlowFactory
    import com.powsybl.loadflow.LoadFlowParameters
    import com.powsybl.commons.config.ComponentDefaultConfig
    
    println " Imported Network's Data: Network Id: " + network.getId()  + "  Generators: " + network.getGeneratorCount()+ "  Lines : " + network.getLineCount() +" Loads: " + network.getLoadCount() 
    
    println "\nDump LOADS "
    println " id | p | p+1%"
    
    // change the network
    def  percent = 1.01
    
    network.getLoads().each { load ->
        if ( load.getTerminal != null) {
            def currentValue = load.getTerminal().getP()
        	load.getTerminal().setP(currentValue * percent)
        	def newVal = load.getTerminal().getP()
        	println " "+load.getId() + "| " +currentValue + "| " + newVal
        }
    }
    
    // execute a LF
    println "\nExecute a LF"
    
    def defaultConfig = ComponentDefaultConfig.load()
    loadFlowFactory = defaultConfig.newFactoryImpl(LoadFlowFactory.class)
    loadFlowParameters = new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES)
    loadFlow = loadFlowFactory.create(network, computationManager, 0)
    result = loadFlow.run(network.getStateManager().getWorkingStateId(),loadFlowParameters).join()
    
    println " LF results - converge:" + result.ok + " ; metrics: " +result.getMetrics()
    }
    ```
    
    This script uses the `network` variable, that is binded by the [groovyScript](../../docs/architecture/iidm/post-processor/groovyScript.md) post processor.
    
    ComponenteDefaultConfig load configuration from [powsybl configuration file](../configuration/configuration.md). It provide access to loadFlow implemantation.

2.  Declare the `groovyScript` post processor  (for more details refer to         [import](../../configuration/importConfig.md)) in the configuration file:

    ```xml
        <import>
            <postProcessors>groovyScript</postProcessors>
        </import>
    ```
    
    and configure the groovy script's path to use in the [groovy-post-processor](../../configuration/groovyScriptPostProcessor/groovyScriptPostProcessor.md) module section, also in the configuration file:
    
    ```xml
    <groovy-post-processor>
          <script><POWSYBL_SAMPLES>/groovyScriptPostProcessor/increase-active-power-postprocessor.groovy</script>
    </groovy-post-processor>
    ```
3. Configure the `loadFlow` 

    The configuration for the loadflow is defined in [powsybl configuration file](../configuration/configuration.md).
    
    The loadflow implementation to use is read from the `LoadFlowFactory` tag of the `componentDefaultConfig` section. 
    
    Here is an example of a minimal configuration for a `mock` loadflow (i.e. an implementation that does nothing on the network). If you want to execute a true computation, you should configure a 'real' loadflow implementation 
    (e.g. RTE's [Hades2LF](http://www.rte.itesla-pst.org/), is currently free to use for academic/non commercial purposes).
    
    ### YAML version
    ```yaml
    componentDefaultConfig:
        LoadFlowFactory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
    ```
    ### XML version
    ```xml
    <componentDefaultConfig>
        <LoadFlowFactory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</LoadFlowFactory>
    </componentDefaultConfig>
    ```


# Java script (for the  JavaScript post processor)
The 'JavaScript' code can be found here  [here](../../samples/javascriptScriptPostProcessor/increaseActivePowerPostProcessor.js).


1 Write a `JavaScript` code that implements the processor's business logic. 

```javascript
var debug = true; 

function increaseLoadActivePower( load, percent) {
	if (load != null) {
		var p = load.getTerminal().getP();
		load.getTerminal().setP(p * percent);
		if (debug)
			print("Load id: "+load.getId() +" Increase load active power, from " + p + " to " +  load.getTerminal().getP());
	}
        
}

var percent = 1.01;

if (network == null) {
    throw new NullPointerException()
}

for each (load in network.getLoads()) {
    increaseLoadActivePower(load , percent);    
}
```
This script uses the `network` variable, that is binded by the [javaScript](../../docs/architecture/iidm/post-processor/javaScript.md) post processor.

2 Declare the `javaScript` post processor  (for more details refer to [import](../../configuration/importConfig.md)) in the configuration file:

```xml
<import>
    <postProcessors>javaScript</postProcessors>
</import>
```

and configure the javaScript code's path to use in the [javascript-post-processor](../../configuration/javaScriptPostProcessor/javaScriptPostProcessor.md) module section, also in the configuration file:


```xml
<javaScriptPostProcessor>
  <script><POWSYBL_SAMPLES>/javaScriptPostProcessor/increase-active-power-postprocessor.js</script>
</javaScriptPostProcessor>
```

# JavaPostProcessor

In order to implement a `JavaPostProcessor` you have to:

1. Write an implementation of `com.powsybl.iidm.import_.ImportPostProcessor` interface and declare it as a service implementation with `@AutoService` annotation.
2. Add the new post processor to the configuration file. 
3. Compile your project, add the jar to your powsybl installation

Here is an empty class *template*  that implements `com.powsybl.iidm.import_ImportPostProcessor` interface, where you will put the code to increase loads active power of the network.

```java
@AutoService(ImportPostProcessor.class)
public class IncreaseActivePowerPostProcessor implements ImportPostProcessor {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
    }

}
```

You have to declare the class as a service implementation, using `@Autoservice` annotation. This will allow you to have the new post processor available and recognized by the platform.  
The methods of the `ImportPostProcessor` interface to override in your class are: 
 
 - `getName` method, that returns the processor's name; it must be used to configure it.
 - `process` method, that  executes the processing on the imported network

```java
@AutoService(ImportPostProcessor.class)
public class IncreaseActivePowerPostProcessor implements ImportPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncreaseActivePowerPostProcessor.class);
    
    private static final String NAME = "increaseActivePower";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) {
    	Objects.requireNonNull(network);
        LOGGER.info("Execute {} post processor on network {}", getName(), network.getId());
        double percent = 1.01;
        LOGGER.info(" Dump LOADS ");
        LOGGER.info(" id | p | p+1%");
        network.getLoadStream().forEach(load -> {
        	if ( load.getTerminal() != null) {
        		double p = load.getTerminal().getP();
        		load.getTerminal().setP(p * percent);
        		LOGGER.info(" {} | {} |  {}", load.getId(), p, load.getTerminal().getP());
        	}
        });
        
        LOGGER.info("Execute loadFlow");
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        LoadFlowParameters loadFlowParameters = new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES);
        LoadFlow loadFlow = defaultConfig.newFactoryImpl(LoadFlowFactory.class).create(network, computationManager, 0);
        LoadFlowResult results = loadFlow.run(network.getStateManager().getWorkingStateId(), loadFlowParameters).join();        
        LOGGER.info("LoadFlow results {}, Metrics {} ", results.isOk(), results.getMetrics().toString());
        
    }
}
```

The `process` method is in charge of executing your processing, implementing your business logic.  
The `network` parameter provides access to the imported network (see `com.powsybl.iidm.network.Network` class), you can work on it using the IIDM API. In the sample code we use it to get the list of all network loads (`network.getLoads()`).  
The `computationManager` parameter provides you access to the computation platform. It can be used to distribute the computation (e.g. if you need to run a loadflow on the imported network, or some other kind of heavy computation).  
The rest of the code in our sample class, increases of 1% the active power of each load, using the IIDM API, and log old and updated values. For the logging we use the `org.slf4j.Logger` class.

JavaPostProcessor requires the following dependencies:

- `com.google.auto.service`: provider configuration file for ServiceLoader.
- `powsybl-iidm-converter-api`: API to import and export IIDM network.
- `powsybl-loadflow-api` : API to run loadflow.

If you use maven, add them to your pom.xml file:

```xml
<dependency>
    <groupId>com.google.auto.service</groupId>
    <artifactId>auto-service</artifactId>
    <version>1.0-rc2</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-converter-api</artifactId>
    <version>${powsybl.version}</version>	
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-loadflow-api</artifactId>
    <version>${powsybl.version}</version>	
</dependency>
```

In your project you also need to add the other dependencies required by your post processor business logic implementation.  


## Update your installation with the new import post processor
In the following sections we refer to installation and sample directories as:

* [\<POWSYBL_HOME\>](../configuration/directoryList.md)
* [\<POWSYBL_SAMPLES\>](../configuration/directoryList.md)


Run the following command to create your project jar:

```bash
$> cd <PROJECT_HOME>
$> mvn install
```

The generated jar will be located under the target folder of your project:
Copy the generated jar to `<POWSYBL_HOME>/share/java/` folder (you might need to copy in this directory other dependencies jars, specific to your new post processor).  
  
In order to enable the post processor on powsybl platform, you must declare its name in the [configuration file](../../configuration/configuration.md):
add the NAME specified for your processor to the `postProcessors` tag of the `import` section. In our example it will be `increaseActivePower`

```xml
<import>
    <postProcessors>increaseActivePower</postProcessors>
</import>
```

In the example above, there is just one post processor enabled. More processors names can be specified in the `postProcessors` tag, as a comma separated list (ref. [post-processors](../../architecture/iidm/post-processor/README.md))

Before test your postProcessor `increaseActivePower`, you must configure a load flow implementation. Referring to `Configure the loadFlow` described in `Groovy script (for the groovy script post processor)` section of this document.
  
In order to execute the new post processor run a command that involve a network import, for instance [run the convert-network command](../../tools/convert-network.md):
```bash
$> cd <POWSYBL_HOME>/bin
$> ./itools convert-network --input-file  NetworkfileName --output-format XIIDM --output-file /tmp/networkfilename.xiidm

```

where `NetworkfileName` is a the path of the input network file.

The log file will show:

* Imported newtwork file: networkfileName imported

* Network Id: (networkId)  Generators: (numGenerators)  Lines: (numLines)  Loads: (numLoads)
 
* Dump LOADS:  list of id | p | p+1%
  
* LF results - converge:true ; metrics: [:]

The converted network, will have the active power of all loads increased by 1%.
