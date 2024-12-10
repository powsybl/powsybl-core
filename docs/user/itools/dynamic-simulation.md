# iTools dynamic-simulation

The `dynamic-simulation` command loads a grid file and run a [time domain](../../simulation/dynamic/index.md) simulation.
In the end, the results and the modified network can be exported to files.

## Usage
```
usage: itools [OPTIONS] dynamic-simulation --case-file <FILE> [--output-variables-file
       <FILE>] --dynamic-models-file <FILE> [--event-models-file <FILE>]
       [--help] [-I <property=value>] [--import-parameters <IMPORT_PARAMETERS>]
       [--output-file <FILE>] [--parameters-file <FILE>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --case-file <FILE>                        the case path
    --output-variables-file <FILE>            output variables description as
                                              Groovy file: defines a list of
                                              variables to plot or get the final
                                              value
    --dynamic-models-file <FILE>              dynamic models description as a
                                              Groovy file: defines the dynamic
                                              models to be associated to chosen
                                              equipments of the network
    --event-models-file <FILE>                dynamic event models description
                                              as a Groovy file: defines the
                                              dynamic event models to be
                                              associated to chosen equipments of
                                              the network
    --help                                    display the help and quit
 -I <property=value>                          use value for given importer
                                              parameter
    --import-parameters <IMPORT_PARAMETERS>   the importer configuation file
    --output-file <FILE>                      dynamic simulation results output
                                              path
    --parameters-file <FILE>                  dynamic simulation parameters as
                                              JSON file
```

### Required options

`--case-file`  
This option defines the path of the case file on which the simulation is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path. 

`--dynamic-models-file`  
This option defines the path of the mapping file used to associate dynamic models to static equipments of the network or add dynamic automation systems. At the moment, only groovy scripts are supported. The [dynamic models DSL](../../simulation/dynamic/index.md#dynamic-models-mapping) depends on the simulator used.

### Optional options

`--output-variables-file`  
This option defines the path of the configuration for the output variables to export at the end of the simulation. This configuration file is a groovy script that respects the [outputVariables DSL](../../simulation/dynamic/index.md#output-variables-configuration) syntax.

`--event-models-file`  
This option defines the path of the configuration for the events to simulate during the simulation. At the moment, only groovy scripts are supported. The [event models DSL](../../simulation/dynamic/index.md#event-models-mapping) depends on the simulator used.

`--import-parameters`  
This option defines the path of the importer's configuration file. It's possible to overload one or many parameters using the `-I property=value` syntax. The list of supported properties depends on the [input format](../../grid_exchange_formats/index.md).

`--output-file`  
This option defines the path where to export the [results](#results) of the simulation.

`--parameters-file`  
This option defines the path of the [parameters](#parameters) file of the simulation. If this option is not used, the simulation is run with the default parameters. 

## Simulators
The available power flow simulators implementations are described [here](../../simulation/dynamic/index.md#implementations).

## Parameters
The available parameters are described [here](../../simulation/dynamic/configuration.md).

## Results
The expected results are described in the [time domain documentation](../../simulation/dynamic/index.md#outputs)

## Examples
The following example shows how to run a power flow simulation, using the default configuration:
```
$> itools dynamic-simulation --case-file IEEE14.iidm --dynamic-models-file dynamicModels.groovy --output-variables-file outputVariables.groovy
Loading network '/tmp/mathbagu/IEEE14.iidm'
dynamic simulation results:
+--------+
| Result |
+--------+
| true   |
+--------+
```

The following example shows how to run a time domain simulation, using a parameter file:
```
$> itools dynamic-simulation --case-file IEEE14.iidm --dynamic-models-file dynamicModels.groovy --parameters-file dynawoParameters.json
dynamic simulation results:
+--------+
| Result |
+--------+
| true   |
+--------+
```

## See also
- [List dynamic simulation models with an iTools command](../../user/itools/list-dynamic-simulation-models.md): learn how to load a list of all dynamic simulation models from the command line.
