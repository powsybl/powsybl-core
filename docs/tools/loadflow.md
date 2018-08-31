# itools loadflow command

itools loadflow command allows you to run a [loadflow](../architecture/loadflow) on a IIDM network, imported from a case file.  

## Configuration for running loadflow command
The configuration for the loadflow is defined in [powsybl configuration file](../configuration/configuration.md): 

The loadflow implementation to use is read from the `LoadFlowFactory` tag of the `componentDefaultConfig` section. 

Here is an example of a minimal configuration for a 'mock' loadflow (i.e. an implementation that does nothing on the network). If you want to execute a true computation, you should configure a 'real' loadflow implementation 
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

*Note*: different loadflow implementations might require specific configurations, in additional config file's sections.


  
The available generic loadflow computation parameters are:

| Property | Type | Default value | Required | Comment |
| -------- | ---- | ------------- | -------- | ------- |
| voltageInitMode | [VoltageInitMode](../../loadflow/loadflow-api/src/main/java/com/powsybl/loadflow/LoadFlowParameters.java) | UNIFORM_VALUES | false | |
| transformerVoltageControlOn | Boolean | false | false | |
| noGeneratorReactiveLimits | Boolean | false | false | |
| phaseShifterRegulationOn | Boolean | false | false | |
| specificCompatibility | Boolean | false | false | |

they can be defined in the configuration file's  ```load-flow-default-parameters``` section; examples:

### YAML version
```yaml
load-flow-default-parameters:
    voltageInitMode: UNIFORM_VALUES
    transformerVoltageControlOn: false
    noGeneratorReactiveLimits: false
    phaseShifterRegulationOn: false
    specificCompatibility: false
```
### XML version
```xml
<load-flow-default-parameters>
    <voltageInitMode>UNIFORM_VALUES</voltageInitMode>
    <transformerVoltageControlOn>false</transformerVoltageControlOn>
    <noGeneratorReactiveLimits>false</noGeneratorReactiveLimits>
    <phaseShifterRegulationOn>false</phaseShifterRegulationOn>
    <specificCompatibility>false</specificCompatibility>
</load-flow-default-parameters>
```


## Running loadflow command 
To show the `loadflow` command help, with its specific parameters and descriptions, enter: 

```
$> cd <POWSYBL_HOME>/bin
$> ./itools loadflow --help
usage: itools [OPTIONS] loadflow --case-file <FILE> [--help] [--output-case-file
       <FILE>] [--output-case-format <CASEFORMAT>] [--output-file <FILE>]
       [--output-format <FORMAT>] [--parameters-file <FILE>] [--skip-postproc]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --case-file <FILE>                  the case path
    --help                              display the help and quit
    --output-case-file <FILE>           modified network base name
    --output-case-format <CASEFORMAT>   modified network output format [AMPL,
                                        XIIDM]
    --output-file <FILE>                loadflow results output path
    --output-format <FORMAT>            loadflow results output format [CSV,
                                        JSON]
    --parameters-file <FILE>            loadflow parameters as JSON file
    --skip-postproc                     skip network importer post processors
                                        (when configured)
```
                                        
In order to run the `loadflow` itools command on a network, enter:

```
$> cd <POWSYBL_HOME>/bin
$> ./itools loadflow --case-file networkfileName
```

Note that `NetworkfileName` is the path of the input network file and is a required argument (it must be specified)

The outcome of the loadflow computation is a table:

| Result | Metrics |
| :--- | :--- |
| boolean stating whether the computation converged (true) or not (false) | some information (implementation dependent) about the computation: it might be the computation time, the number of iterations, etc. |

By default, the command writes its output as an ASCII formatted table to the standard output. However, the output can be set to a file, formatted as CSV or JSON, by using the parameters ```--output-file``` and ```--output-format```.

Command line parameters  ```--output-case-file``` and ```--output-case-format``` allow exporting the post loadflow network to a file, in a specific format (e.g. XIIDM, AMPL. The available options are shown in the command help).

If there is a need to override the configuration with some specific loadflow attributes, it can be done without changing the configuration file, by using the command line parameter ```--parameters-file``` (it must point to a json file with the specific attributes) .

It is also possible to disable the network importer's postprocessors (if configured),  with the command line parameter```--skip-postproc``` 