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
Mathematically speaking, a [power flow](../../simulation/powerflow/index.md) result is fully defined by the complex voltages at each node. The consequence is that most load flow algorithms converge very fast if they are initialized with voltages. As a result, it happens that load flow results include only voltages and not flows on branches. This post-processors computes the flows given the voltages. The equations (Kirchhoff law) used are the same as the one used in the [load flow validation](../../user/itools/loadflow-validation.md#load-flow-results-validation) to compute $P_1^{\text{calc}}$, $Q_1^{\text{calc}}$, $P_2^{\text{calc}}$, $Q_2^{\text{calc}}$ for branches and $P_3^{\text{calc}}$, $Q_3^{\text{calc}}$ in addition for three-windings transformers.

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

## Geographical data import post-processor

One way to add geographical positions on a network is to use the import post-processor named odreGeoDataImporter, that will automatically add the [LinePosition](../grid_model/extensions.md#line-position) and [SubstationPosition](../grid_model/extensions.md#substation-position) extensions to the network model.

This processor uses geographical position data formatted in multiple csv files, as it can be obtained on the website [OpenData Réseaux-Énergie](https://odre.opendatasoft.com) for the network of the French TSO RTE.

Using the links in the table below, you can obtain the RTE data CSV files, to be used as reference for input data formatting.

| Network element type | RTE data CSV file link                                                                                                                                               |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Substations          | [https://odre.opendatasoft.com/explore/dataset/postes-electriques-rte/export/](https://odre.opendatasoft.com/explore/dataset/postes-electriques-rte/export/)         |
| Aerial lines         | [https://odre.opendatasoft.com/explore/dataset/lignes-aeriennes-rte-nv/export/](https://odre.opendatasoft.com/explore/dataset/lignes-aeriennes-rte-nv/export/)       |
| Underground lines    | [https://odre.opendatasoft.com/explore/dataset/lignes-souterraines-rte-nv/export/](https://odre.opendatasoft.com/explore/dataset/lignes-souterraines-rte-nv/export/) |

<span style="font-size:0.75em;">(To download these files, you should first accept the usage conditions of the ODRÉ website, which can be found - in French only - at the bottom of the pages, and the Etalab Open License v2.0, available in English [here](https://www.etalab.gouv.fr/wp-content/uploads/2018/11/open-licence.pdf).)</span>

<br/>

To use this import post-processor, add the `com.powsybl:powsybl-iidm-geodata` to your classpath and enable it setting the `postProcessors` and `odre-geo-data-importer-post-processor` modules :

**YAML configuration:**
```yaml
import:
  postProcessors:
    - odreGeoDataImporter

odre-geo-data-importer-post-processor:
  substations: /path/to/substations.csv
  aerial-lines: /path/to/aerial-lines.csv
  underground-lines: /path/to/underground-lines.csv
```

**XML configuration:**
```xml
<import>
    <postProcessors>odreGeoDataImporter</postProcessors>
</import>
<odre-geo-data-importer-post-processor>
    <substations>/path/to/substations.csv</substations>
    <aerial-lines>/path/to/aerial-lines.csv</aerial-lines>
    <underground-lines>/path/to/underground-lines.csv</underground-lines>
</odre-geo-data-importer-post-processor>
```

The paths to the different files can be absolute paths or paths relative to the directory where your command is launched.

## Going further
- [Create a post-processor](): Learn how to implement your own post-processor 
