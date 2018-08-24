# itools loadflow command

itools loadflow command allows you to run a loadflow on a IIDM network, imported from a case file.  

## Configuration for running loadflow command
The configuration for the loadflow is defined in [powsybl configuration file](../configuration/configuration.md).  
The loadflow implementation to use is read from the `LoadFlowFactory` tag of the `componentDefaultConfig` section:

```
<componentDefaultConfig>
    <LoadFlowFactory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</LoadFlowFactory>
</componentDefaultConfig>
```

Here we put a mock implementation (implementation that does nothing on the network), if you want to run a loadflow you should configure a 'real' implementation of the loadflow.  
  
The loadflow parameters can be defined in a load-flow-default-parameters section of the  [powsybl configuration file](../configuration/configuration.md):

```
<load-flow-default-parameters>
    <voltageInitMode>UNIFORM_VALUES</voltageInitMode>
    <transformerVoltageControlOn>false</transformerVoltageControlOn>
    <noGeneratorReactiveLimits>false</noGeneratorReactiveLimits>
    <phaseShifterRegulationOn>false</phaseShifterRegulationOn>
    <specificCompatibility>false</specificCompatibility>
</load-flow-default-parameters>
```

The available parameters are:
* *voltageInitMode*: default value is UNIFORM_VALUES
* *transformerVoltageControlOn*: default value is false
* *noGeneratorReactiveLimits*: default value is false
* *phaseShifterRegulationOn*: default value is false
* *specificCompatibility*: default value is false

## Running loadflow command 
Following is an example of how to use the `loadflow` command, and run a loadflow on a network. 
   
To show the command help, with its specific parameters and descriptions, enter: 

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
                                        
In order to run the `loadflow` itools command enter:

```
$> cd <POWSYBL_HOME>/bin
$> ./itools loadflow --case-file networkfileName
```

`NetworkfileName` is the path of the input network file and is a required argument (it must be specified)