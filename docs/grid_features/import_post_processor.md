# Import post-processor
The import post-processor is a feature that allows to do automatic modifications or simulations, just after a case is converted to an [IIDM](../grid_exchange_formats/iidm/index.md) network. These post-processors rely on the [plugin mechanism]() of PowSyBl meaning that they are discovered at runtime. To enable one or more post-processors, the `postprocessors` property of the `import` module must be defined in the configuration file. Note that if you configure several post-processors, they are executed in the declaration order, like a pipeline:
<span style="color: red">TODO: insert a picture

PowSyBl provides 2 different implementations of post-processors:
- [Groovy](#groovy-post-processor): to execute a groovy script
- [LoadFlow](#loadflow-post-processor): to run a power flow simulation

(groovy-post-processor)=
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

(loadflow-post-processor)=
## LoadFlow post-processor
Mathematically speaking, a [load flow](../simulation/loadflow/index) result is fully defined by the complex voltages at each node. The consequence is that most load flow algorithms converge very fast if they are initialized with voltages. As a result, it happens that load flow results include only voltages and not flows on branches. This post-processors computes the flows given the voltages. The equations (Kirchhoff law) used are the same as the one used in the [load flow validation](../user/itools/loadflow-validation.md#load-flow-results-validation) to compute $P_1^{\text{calc}}$, $Q_1^{\text{calc}}$, $P_2^{\text{calc}}$, $Q_2^{\text{calc}}$ for branches and $P_3^{\text{calc}}$, $Q_3^{\text{calc}}$ in addition for three-winding transformers.

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

**Note:** This post-processor relies on the [load flow results completion]() module.

(geographical-data-import-post-processor)=
## Geographical data import post-processor

One way to add geographical positions on a network is to use the import post-processor named GeoJsonAdderPostProcessor, that will automatically add the [LinePosition](../grid_model/extensions.md#line-position) and [SubstationPosition](../grid_model/extensions.md#substation-position) extensions to the network model.

This processor uses geographical position data formatted in two JSON files, as it can be obtained on the websites [Open Infra Map](https://openinframap.org) or [Open Street Map](https://www.openstreetmap.org).

<br/>

To use this import post-processor, add the `com.powsybl:powsybl-iidm-geodata` to your classpath and enable it setting the `postProcessors` and `geo-json-importer-post-processor` modules :

**YAML configuration:**
```yaml
import:
  postProcessors:
    - geoJsonImporter

geo-json-importer-post-processor:
  substations: /path/to/substations.geojson
  lines: /path/to/lines.geojson
```

**XML configuration:**
```xml
<import>
    <postProcessors>geoJsonImporter</postProcessors>
</import>
<geo-json-importer-post-processor>
    <substations>/path/to/substations.geojson</substations>
    <lines>/path/to/lines.geojson</lines>
</geo-json-importer-post-processor>
```

The paths to the different files can be absolute paths or paths relative to the directory where your command is launched.

## Going further
- [Create a post-processor](): Learn how to implement your own post-processor
