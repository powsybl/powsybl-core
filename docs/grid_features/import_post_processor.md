# Import post-processor
The import post-processor is a feature that allows to do automatic modifications or simulations, just after a case is converted to an [IIDM](../grid_exchange_formats/iidm/index.md) network. These post-processors rely on the [plugin mechanism]() of PowSyBl meaning that they are discovered at runtime. To enable one or more post-processors, the `postprocessors` property of the `import` module must be defined in the configuration file. Note that if you configure several post-processors, they are executed in the declaration order, like a pipeline:  
<span style="color: red">TODO: insert a picture

PowSyBl provides 3 different implementations of post-processors:
- [Groovy](#groovy-post-processor): to execute a groovy script
- [JavaScript](#javascript-post-processor): to execute a JS script
- [LoadFlow](#loadflow-post-processor): to run a power flow simulation

## Groovy post-processor
This post-processor executes a groovy script, loaded from a file. The script can access to the network and the [computation manager]() using the variables `network` and `computationManager`. To use this post-processor, add the `com.powsybl:powsybl-iidm-scripting` dependency to your classpath, and configure both `import` and `groovy-post-processor` modules:

**YAML configuration:**
```yaml
import:
    postProcessors:
        - groovyScript

groovy-post-processor:
    script: /path/to/the/script
```

**XML configuration:**
```xml
<import>
    <postProcessors>groovyScript</postProcessors>
</import>
<groovy-post-processor>
    <script>/path/to/the/script</script>
</groovy-post-processor>
```

**Note**: the `script` property is optional. If it is not defined, the `import-post-processor.groovy` script from the PowSyBl configuration folder is used.

### Example
The following example prints meta-information from the network:
```groovy
println "Network " + network.getId() + " (" + network.getSourceFormat()+ ") is imported"
```

## JavaScript post-processor
This post-processor executes a JS script, loaded from a file. The script can access to the network and the [computation manager]() using the variables `network` and `computationManager`. To use this post-processor, add the `com.powsybl:powsybl-iidm-scripting` dependency to your classpath, and configure both `import` and `javaScriptPostProcessor` modules:

**YAML configuration:**
```yaml
import:
    postProcessors:
        - javaScript

javaScriptPostProcessor:
    script: /path/to/the/script
```

**XML configuration:**
```xml
<import>
    <postProcessors>javaScript</postProcessors>
</import>
<javaScriptPostProcessor>
    <script>/path/to/the/script</script>
</javaScriptPostProcessor>
```

**Note**: the `script` property is optional. If it is not defined, the `import-post-processor.js` script from the PowSyBl configuration folder is used.

### Example
The following example prints meta-information from the network:
```javascript
print("Network " + network.getId() + " (" + network.getSourceFormat()+ ") is imported");
```

## LoadFlow post-processor
Mathematically speaking, a [load flow](../simulation/loadflow/loadflow.md) result is fully defined by the complex voltages at each node. The consequence is that most load flow algorithms converge very fast if they are initialized with voltages. As a result, it happens that load flow results include only voltages and not flows on branches. This post-processors computes the flows given the voltages. The equations (Kirchhoff law) used are the same as the one used in the [load flow validation](../user/itools/loadflow-validation.md#load-flow-results-validation) to compute $P_1^{\text{calc}}$, $Q_1^{\text{calc}}$, $P_2^{\text{calc}}$, $Q_2^{\text{calc}}$ for branches and $P_3^{\text{calc}}$, $Q_3^{\text{calc}}$ in addition for three-windings transformers.

To use this post-processor, add the `com.powsybl:powsybl-loadflow-results-completion` to your classpath and enable it setting the `postProcessors` property of the `import` module.

**YAML configuration:**
```yaml
import:
    postProcessors:
        - loadflowResultsCompletion
```

**XML configuration:**
```xml
<import>
    <postProcessors>loadflowResultsCompletion</postProcessors>
</import>
```

**Note:** This post-processor rely on the [load flow results completion]() module.

## Going further
- [Create a post-processor](): Learn how to implement your own post-processor 
