# Tutorial - Howto write an implementation of load-flow.

The purpose of this tutorial is to give developers some information about how to add to the powsybl platform a new tool to perform load-flow computation, either by implementing a new tool or by integrating an existing one,
using powsybl's load-flow API.  

Note:  it's beyond the scope of this tutorial to explain how to implement a load-flow algorithm: the code fragment with the ''computation'' in the example will be an empty placeholder.  

To add a new load-flow tool to the framework, you will have to:

- Write an implementation of `com.powsybl.loadflow.LoadFlow` interface, with the loadflow computation logic. 
- Write an implementation of `com.powsybl.loadflow.LoadFlowFactory` interface, taking care of the actual new LoadFlow instantiation
- (Optional) Extend a `com.powsybl.loadflow.LoadFlowParameters` class  and implement `LoadFlowParameters.ConfigLoader` interface, required only if extra parameters are needed.
- (Optional) Customize load-flow results, only for specific needs.
- Compile your project and add the new module's jar(s) to your powsybl installation.

To use the new load-flow tool, you will have to:  

- Declare the new load-flow implementation in the platform configuration file. 

## Dependencies

The loadflow API requires at least the following dependencies:

- `com.google.auto.service`: configuration/metadata generator for java.util.ServiceLoader-style service providers
- `powsybl-loadflow-api` : API to run loadflow.

you must add them to your project, in order to compile it.

If you use maven, update your pom.xml file:

```xml
<dependency>
    <groupId>com.google.auto.service</groupId>
    <artifactId>auto-service</artifactId>
    <version>1.0-rc2</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-loadflow-api</artifactId>
    <version>${powsybl.version}</version>	
</dependency>
```

In your project you might also need to add other dependencies required by your load-flow business logic implementation.  

## Write an implementation of com.powsybl.loadflow.LoadFlow interface 

Here is a class *template*  that implements `com.powsybl.loadflow.LoadFlow` interface, where you will put your load-flow implementation.  

```java
class SampleLoadFlow implements LoadFlow {
 
    @Override
    public String getName() {
    }

    @Override
    public String getVersion() {
    }

    @Override
    public CompletableFuture<LoadFlowResult> run(String workingStateId,LoadFlowParameters parameters) {
    }
 
}
```
The method `getName` returns the load-flow name.	

The method `getVersion` returns the load-flow version.

The method `run` is in charge of performing the actual load-flow computation. 	

The input parameter `workingStateId` identifies the working state of the network, subject of the loadflow computation.

The input parameter `parameters` are the properties for the load-flow, standard plus specific ones.  

		
## Write an implementation of com.powsybl.loadflow.LoadFlowFactory interface.

Here is an empty class *template*  that implements `com.powsybl.loadflow.LoadFlowFactory` interface. 
    
```java
public class LoadFlowFactorySample implements LoadFlowFactory {
    @Override
    public LoadFlow create(Network network, ComputationManager computationManager, int priority) {
    }
}
```
	
The `create` method returns the load-flow instance for a given network. 

The `network` parameter identifies the network to use as input for loadflow computation.

The `computationManager` parameter provides you access to the computation platform to use. It can be used by the specific implementation to integrate an external tool and to distribute the computation using the platform computation module.  

The `priority` parameter is used to set the computation's priority.
  
# (OPTIONAL) Extend a com.powsybl.loadflow.LoadFlowParameters class withand implement LoadFlowParameters.ConfigLoader interface, only if extra paramters are needed.

Powsybl platform comes with a set of standard [load-flow parameters](../../configuration/modules/load-flow-default-parameters.md).  If you need extra parameters, you have to create a custom extension of `LoadFlowParameters`.

For the sake of example, we define here a class with a custom boolean parameter: `debugActivated` 

```java
import java.util.Objects;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.loadflow.LoadFlowParameters;
    
public class SampleLoadFlowParameters extends AbstractExtension<LoadFlowParameters> {

    public static final boolean DEFAULT_DEBUG_ACTIVATED = false;
    private boolean debugActivated = DEFAULT_DEBUG_ACTIVATED;

    @Override
    public String getName() {
        return "SampleLoadFlowParameters";
    }

    public boolean isDebugActivated() {
        return debugActivated;
    }

    public SampleLoadFlowParameters setDebugActivated(boolean debugActivated) {
        this.debugActivated = debugActivated;
        return this;
    }

    public SampleLoadFlowParameters() {
    }

    public SampleLoadFlowParameters(SampleLoadFlowParameters other) {
        Objects.requireNonNull(other);
        this.debugActivated = other.debugActivated;
    }

    @Override
    public String toString() {
        ImmutableMap.Builder<String, Object> immutableMapBuilder = ImmutableMap.builder();
        immutableMapBuilder.put("debugActivated", debugActivated);
        return immutableMapBuilder.build().toString();
    }
}
```

Here below is a 'matching' class, in charge of loading the additional parameter `debugActivated` value from the platform configurations. The parameter is read from a `sample-loadflow-parameters` section,

```java
import java.util.Objects;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.loadflow.LoadFlowParameters;
    
import static com.powsybl.samples.loadflow.SampleLoadFlowParameters.*;
    
@AutoService(LoadFlowParameters.ConfigLoader.class)
public class SampleLoadFlowParametersConfigLoader implements LoadFlowParameters.ConfigLoader<SampleLoadFlowParameters> {

    static final String MODULE_NAME = "sample-loadflow-parameters";

    @Override
    public String getExtensionName() {
        return "SampleLoadFlowParameters";
    }

    @Override
    public String getCategoryName() {
        return "loadflow-parameters";
    }

    @Override
    public Class<? super SampleLoadFlowParameters> getExtensionClass() {
        return SampleLoadFlowParameters.class;
    }

    @Override
    public SampleLoadFlowParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        ModuleConfig config = platformConfig.getModuleConfigIfExists(MODULE_NAME);
        SampleLoadFlowParameters sampleLoadFlowParameters = new SampleLoadFlowParameters();
        if (config != null) {            
            sampleLoadFlowParameters.setDebugActivated(config.getBooleanProperty("debugActivated", DEFAULT_DEBUG_ACTIVATED));
        }
        return sampleLoadFlowParameters;
    }

}
```

The following code fragment shows how to retrieve the custom parameter when needed, in the loadflow `run` method (using the `parameters` input)
 
```java
boolean debugActivated = SampleLoadFlowParameters.DEFAULT_DEBUG_ACTIVATED;
SampleLoadFlowParameters myParameters = parameters.getExtension(SampleLoadFlowParameters.class);
if (myParameters != null) {
    debugActivated = myParameters.isDebugActivated();
}
```

## (OPTIONAL) Customize load-flow results, only for specific needs. 

Powsybl's load-flow api provides `com.powsybl.loadflow.LoadFlowResultImpl`, a default implementation of `com.powsybl.loadflow.LoadFlowResult` interface. 
If this class doesn't cover all your needs, you can write a new implementation.

Here is a class *template*  that implements `com.powsybl.loadflow.LoadFlowResult` interface.

```java
public class LoadFlowResultSampleImpl implements LoadFlowResult {

    @Override
    public boolean isOk() {
        return status;
    }

    @Override
    public Map<String, String> getMetrics() {
        return metrics;
    }

    @Override
    public String getLogs() {
        return logs;
    }
}
```
The `isOk` method returns a boolean value. It should be used to map load-flow's status: true if the load-flow computation convergence, false otherwise.

The `getMetrics` method could be used to provide additional information regarding the calculation process, such as: iterations number, computation time, etc.

The `getLogs` method returns a String, it could contain for example the actual log from an external tool, if deemed useful.  


## Compile your project, add the jar to your powsybl installation

To compile and create your project jar type this command:

```bash
$> cd <PROJECT_HOME>
$> mvn install
```

Copy the generated jar from your project's target folder to [\<POWSYBL_HOME\>](../../configuration/directoryList.md)`/share/java/` folder (you might need to copy in this directory other dependencies jars, specific to your new load flow).  


   
## Declare the new load-flow implementation in the configuration file. 

In order to configure powsybl to use the new load-flow implementation, you must declare it in the [configuration file](../../configuration/configuration.md): in the specifics,
add the loadflow factory implementation in the `LoadFlowFactory` tag of the [`componentDefaultConfig` section](../../configuration/modules/componentDefaultConfig.md). 

### YAML
```yaml
componentDefaultConfig:
    LoadFlowFactory: com.powsybl.samples.loadflow.LoadFlowFactorySample
```

### XML
```xml
<componentDefaultConfig>
    <LoadFlowFactory>com.powsybl.samples.loadflow.LoadFlowFactorySample</LoadFlowFactory>
</componentDefaultConfig>
```

To configure the new parameter `debugActivated` in the [configuration file](../../configuration/configuration.md), you must declare it in a new `sample-loadflow-parameter` section:

### YAML
```yaml
sample-loadflow-parameter:
    debugActivated: true
```

### XML
```xml
<sample-loadflow-parameter>
    <debugActivated>true</debugActivated>
</sample-loadflow-parameter>
```


## References

- A full  example of a load-load flow integration can be found in the project [pypsa-loadflow](https://github.com/murgeyseb/pypsa-loadflow). Here the computation is performed by the free python based software [PyPsa](https://github.com/murgeyseb/PyPSA).











