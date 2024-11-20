# iTools dynamic-security-analysis

The `dynamic-security-analysis` command loads a grid file, apply dynamic models, and run a [dynamic security analysis](../../simulation/dynamic_security/index.md) simulation, to detect security violations on pre- or post-contingencies states. At the end of the simulation, the results are printed or exported to a file.

## Usage
```
$> itools dynamic-security-analysis --help
usage: itools [OPTIONS] dynamic-security-analysis --case-file <FILE>
       [--contingencies-file <FILE>] --dynamic-models-file <FILE> [--external]
       [--help] [-I <property=value>] [--import-parameters <IMPORT_PARAMETERS>]
       [--limit-types <LIMIT-TYPES>] [--log-file <FILE>] [--monitoring-file
       <FILE>] [--output-file <FILE>] [--output-format <FORMAT>]
       [--parameters-file <FILE>] [--with-extensions <EXTENSIONS>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --case-file <FILE>                        the case path
    --contingencies-file <FILE>               the contingencies path
    --dynamic-models-file <FILE>              dynamic models description as a
                                              Groovy file: defines the dynamic
                                              models to be associated to chosen
                                              equipments of the network
    --external                                external execution
    --help                                    display the help and quit
 -I <property=value>                          use value for given importer
                                              parameter
    --import-parameters <IMPORT_PARAMETERS>   the importer configuration file
    --limit-types <LIMIT-TYPES>               limit type filter (all if not set)
    --log-file <FILE>                         log output path (.zip)
    --monitoring-file <FILE>                  monitoring file (.json) to get
                                              network's infos after computation
    --output-file <FILE>                      the output path
    --output-format <FORMAT>                  the output format [JSON]
    --parameters-file <FILE>                  loadflow parameters as JSON file
    --with-extensions <EXTENSIONS>            the extension list to enable

Allowed LIMIT-TYPES values are [ACTIVE_POWER, APPARENT_POWER, CURRENT,
LOW_VOLTAGE, HIGH_VOLTAGE, LOW_VOLTAGE_ANGLE, HIGH_VOLTAGE_ANGLE,
LOW_SHORT_CIRCUIT_CURRENT, HIGH_SHORT_CIRCUIT_CURRENT, OTHER]
Allowed EXTENSIONS values are []

```

### Required arguments

`--case-file`  
This option defines the path of the case file on which the security analysis is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path.

`--dynamic-models-file`  
This option defines the path of the mapping file used to associate dynamic models to static equipments of the network or add dynamic automation systems. At the moment, only groovy scripts are supported. The [dynamic models DSL](../../simulation/dynamic/index.md#dynamic-models-mapping) depends on the simulator used.

### Optional arguments

`--contingencies-file`  
This option defines the path of the contingency files. If this parameter is not set, the security violations are checked on the base state only. This file is a groovy script that respects the [contingency DSL](../../simulation/security/contingency-dsl.md) syntax.

`--external`  
<span style="color: red">TODO:</span> Use this argument to run the security analysis as an external process.

`--import-parameters`  
This option defines the path of the importer's configuration file. It's possible to overload one or many parameters using the `-I property=value` syntax. The list of supported properties depends on the [input format](../../grid_exchange_formats/index.md).

`--limit-types`  
This option allows filtering certain types of violations. It overrides the default configuration defined in the [limit-violation-default-filter](../configuration/limit-violation-default-filter.md) configuration module. The supported types are the following: `CURRENT`, `LOW_VOLTAGE`, `HIGH_VOLTAGE`, `LOW_SHORT_CIRCUIT_CURRENT`, `HIGH_SHORT_CIRCUIT_CURRENT` and `OTHER`.

`--log-file`  
<span style="color: red">TODO</span>

`--output-file`  
This option defines the path of the result file. If this option is not set, the results are printed to the console.

`--output-format`
This option defines the format of the output file. This option is required if the `--output-file` is set. The only supported format is `JSON`.  

`--parameters-file`  
This option defines the path of the [parameters](#parameters) file of the simulation. If this option is not used, the simulation is run with the default parameters. 

`--with-extensions`  
This option defines the list of extensions to complete the simulation results with additional data. The available extensions are listed in the usage of the command.

## Simulators
<span style="color: red">TODO</span>

## Contingencies
<span style="color: red">TODO</span>

## Parameters
<span style="color: red">TODO</span>

## Results
<span style="color: red">TODO</span>

### with-extensions
Use the `--with-extensions` parameter to activate a list of `com.powsybl.security.interceptors.SecurityAnalysisInterceptor`
implementations.

## See also
- [List dynamic simulation models with an iTools command](../../user/itools/list-dynamic-simulation-models.md): Learn how to load a list of all dynamic simulation models from the command line.