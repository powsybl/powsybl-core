# Security analysis implementations

## Load flow based implementation
This implementation of the Security Analysis API relies on a load flow engine. Basically, a load flow computation is run on the base case to compute a reference. Each contingency is then applied on a temporary variant and a new load flow is run. If the load flow converges, the violations are listed.

To use this implementation, you have to add the following lines to your configuration file:

**YAML configuration:**
```yaml
componentDefaultConfig:
    SecurityAnalysisFactory: com.powsybl.security.SecurityAnalysisFactoryImpl
```

**XML configuration:**
```xml
<componentDefaultConfig>
    <SecurityAnalysisFactory>com.powsybl.security.SecurityAnalysisFactoryImpl</SecurityAnalysisFactory>
</componentDefaultConfig>
```

Remember that this implementation relies on a load flow engine. It will automatically use the default implementation, except if there are many in your classpath. In that specific case, you have to adjust your configuration defining the name of the load flow engine to use. Please refer to the [load flow](../loadflow/loadflow.md) page to know the list of available implementations.

**YAML configuration:**
```yaml
load-flow:
    default-impl-name: Default
```

**XML configuration:**
```xml
<load-flow>
    <default-impl-name>Default</default-impl-name>
</load-flow>
```

## OpenLoadFlow
<span style="color: red">TODO</span>

### Configuration
<span style="color: red">TODO</span>

#### Specific parameters
<span style="color: red">TODO</span>

