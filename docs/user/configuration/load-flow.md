# load-flow
The `load-flow` module is used to configure the load flow default implementation name. Each load flow implementation
provides a subclass of `com.powsybl.loadflow.LoadFlowProvider` correctly configured to be found by `java.util.ServiceLoader`.
A load flow provider exposes a name that can be used in the LoadFlow Java API to find a specific load flow implementation.
It can also be used to specify a default implementation in this platform config module. If only one `com.powsybl.loadflow.LoadFlowProvider`
is present in the classpath, there is no need to specify a default LoadFlow implementation name. In the case where more
than one `com.powsybl.loadflow.LoadFlowProvider` is present in the classpath, specifying the default implementation name
allows LoadFlow API user to use LoadFlow.run(...) and  LoadFlow.runAsync(...) methods to run a load flow. Using these
methods when no default load flow name is configured and multiple implementations are in the classpath will throw an exception.
An exception is also thrown if no implementation at all is present in the classpath, or if specifying a load flow name that
is not present on the classpath.

## Properties

**default-impl-name**  
Use the `default-impl-name` property to specify the name of the default load flow implementation.

## Examples

**YAML configuration:**
```yaml
load-flow:
    default-impl-name: Mock
```

**XML configuration:**
```xml
<load-flow>
    <default-impl-name>Mock</default-impl-name>
</load-flow>
```
