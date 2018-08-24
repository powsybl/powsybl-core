# How to create a new import post processor
Implementing an import post processor allows the execution of custom operations on imported network data.

In order to create a new post processor:
1. Create a new maven project and add all the required dependencies.
2. Implement the `com.powsybl.iidm.import_.ImportPostProcessor` interface. 
3. Compile your project, add the jar to your powsybl installation, and add the new post processor to the configuration file.

In the following sections we will see how, following these steps, you can implement a new post processor for increasing the active power of all loads of an imported network.  
A sample maven project implementing this post processor can be found [here](../samples/increase-active-power-postprocessor).  

## Maven dependencies
  
After creating the Maven project, you need to add the necessary dependencies to your pom.xml file.  
Maven dependencies required for implementing a new post processor are the following:  

```xml
<dependency>
    <groupId>com.google.auto.service</groupId>
    <artifactId>auto-service</artifactId>
    <version>1.0-rc2</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-converter-api</artifactId>
    <version>${project.version}</version>
</dependency>
```

In your project you also need to add the other dependencies required by your post processor business logic implementation.  

## Implement the ImportPostProcessor interface

For creating a new post processor, you need to implement the `com.powsybl.iidm.import_ImportPostProcessor` interface.  
Following is a sample class, where you will put the code to increase loads active power of the network.

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
 
 - `getName` method, that returns the processor's name
 - `process` method, that  executes the processing on the imported network

```java
    public static final String NAME = "increaseActivePower";
    private static final Logger LOGGER = LoggerFactory.getLogger(IncreaseActivePowerPostProcessor.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
        Objects.requireNonNull(network);
        LOGGER.info("Execute {} post processor on network {}", getName(), network.getId());
        double percent = 1.01;
        network.getLoads().forEach(load -> {
            load.setP0(load.getP0() * percent);
            double p = load.getTerminal().getP();
            load.getTerminal().setP(p * percent);
            LOGGER.info("Load {} : p {} -> {}", load.getId(), p, load.getTerminal().getP());
        });
    }
```

The `process` method is in charge of executing your processing, implementing your business logic.  
The `network` parameter provides access to the imported network (see `com.powsybl.iidm.network.Network` class), you can work on it using the IIDM API. In the sample code we use it to get the list of all network loads (`network.getLoads()`).  
The `computationManager` parameter provides you access to the computation platform. It can be used to distribute the computation (e.g. if you need to run a loadflow on the imported network, or some other kind of heavy computation).  
The rest of the code in our sample class increases of 1% the active power of each load, using the IIDM API, and log old and updated values. For the logging we use the `org.slf4j.Logger` class.


## Update your installation with the new import post processor

Run the following command to create your project jar:

```bash
$> cd <PROJECT_HOME>
$> mvn install
```

The generated jar will be located under the target folder of your project.  
Copy the generated jar to `<POWSYBL_HOME>/share/java/` folder (you might need to copy in this directory other dependencies jars, specific to your new post processor).  
  
In order to make the powsybl platform call your new post processor after network import, it's necessary to update the [configuration file](../configuration/configuration.md).  
Add the NAME specified for your processor to the `postProcessors` tag of the `import` section. In our example will be `increaseActivePower`

```xml
<import>
    <postProcessors>increaseActivePower</postProcessors>
</import>
```
  
In order to execute the new post processor run a command that involve a network import, for instance [run the loadflow command](../tools/loadflow.md):
```bash
$> cd <POWSYBL_HOME>/bin
$> ./itools loadflow --case-file NetworkfileName
```

where `NetworkfileName` is a the path of the input network file.

The log file will list, for each load, the active power changed, for instance:

```
...
- Load 8500_3_LOAD : p 864.3330078125 -> 872.976337890625
- Load 8500_2_LOAD : p 864.3330078125 -> 872.976337890625
- Load 8500_1_LOAD : p 864.3330078125 -> 872.976337890625
...

```
The network, input to the loadflow, will have the active power of all loads increased by 1%.
