# iTools loadflow

The `loadflow` command loads a grid file and run a [load flow](../../simulation/loadflow/index.md) simulation. In the end, the results and the modified network can be exported to files.

## Usage
```
$> itools loadflow --help
usage: itools [OPTIONS] loadflow --case-file <FILE> [-E <property=value>]
              [--export-parameters <EXPORT_PARAMETERS>] [--help] [-I <property=value>]
              [--import-parameters <IMPORT_PARAMETERS>] [--output-case-file <FILE>]
              [--output-case-format <CASEFORMAT>] [--output-file <FILE>]
              [--output-format <FORMAT>] [--parameters-file <FILE>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --case-file <FILE>                            the case path
-E <property=value>                               use value for given exporter
                                                  parameter
     --export-parameters <EXPORT_PARAMETERS>      the exporter configuration file
     --help                                       display the help and quit
-I <property=value>                               use value for given importer
                                                  parameter
     --import-parameters <IMPORT_PARAMETERS>      the importer configuation file
     --output-case-file <FILE>                    modified network base name
     --output-case-format <CASEFORMAT>            modified network output format
                                                  [CGMES, AMPL, XIIDM]
     --output-file <FILE>                         loadflow results output path
     --output-format <FORMAT>                     loadflow results output format
                                                  [CSV, JSON]
     --parameters-file <FILE>                     loadflow parameters as JSON file
```

### Required options

`--case-file`: This option defines the path of the case file on which the power flow simulation is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path. 

### Optional options

`--export-parameters`  
This option defines the path of the exporter's configuration file. It's possible to overload one or many parameters using the `-E property=value` syntax. The list of supported properties depends on the [output format](../../grid_exchange_formats/index.md).

`--import-parameters`  
This option defines the path of the importer's configuration file. It's possible to overload one or many parameters using the `-I property=value` syntax. The list of supported properties depends on the [input format](../../grid_exchange_formats/index.md).

`--output-case-file`  
This option defines the path where to export the modified network.

`--output-case-format`  
This option defines the format of the output case file. The list of [supported formats](../../grid_exchange_formats/index.md) are listed between brackets in the command help. This option is required if `--output-case-file` is used.

`--output-file`  
This option defines the path where to export the [results](../../simulation/loadflow/index.md#outputs) of the load flow.

`--output-format`  
This option defines the format of the output file. The supported format are `CSV` and `JSON`. This option is required if the `--output-file` is used.

`--parameters-file`  
This option defines the path of the [parameters](#parameters) file of the simulation. If this option is not used, the simulation is run with the default parameters. 

## Simulators

The available power flow simulators implementations are described [here](../../simulation/loadflow/index.md#implementations).

## Parameters
The available parameters are described [here](../../simulation/loadflow/configuration.md#parameters).

## Results
The expected results are described in the [load flow documentation](../../simulation/loadflow/index.md#outputs)

## Examples
The following example shows how to run a power flow simulation, using the default configuration:
```
$> itools loadflow --case-file case.xiidm
Loading network 'case.xiidm'
loadflow results:
+--------+-----------------------------------------------------------------------------------------+
| Ok     | Metrics                                                                                 |
+--------+-----------------------------------------------------------------------------------------+
| true   | {nbIter=4, dureeCalcul=0.001569, cause=0, contraintes=0, statut=OK, csprMarcheForcee=0} |
+--------+-----------------------------------------------------------------------------------------+
Components results:
+------------------+-----------+-----------------+--------------+--------------------+
| Component number | Status    | Iteration count | Slack bus ID | Slack bus mismatch |
+------------------+-----------+-----------------+--------------+--------------------+
| 0                | CONVERGED | 8               | BUS_0        | -0,00954794        |
+------------------+-----------+-----------------+--------------+--------------------+
```

The following example shows how to run a power flow simulation, using a parameter file:
```
$> itools loadflow --case-file case.xiidm --parameters-file loadflowparameters.json
loadflow results:
+--------+-----------------------------------------------------------------------------------------+
| Ok     | Metrics                                                                                 |
+--------+-----------------------------------------------------------------------------------------+
| true   | {nbIter=4, dureeCalcul=0.001569, cause=0, contraintes=0, statut=OK, csprMarcheForcee=0} |
+--------+-----------------------------------------------------------------------------------------+
Components results:
+------------------+-----------+-----------------+--------------+--------------------+
| Component number | Status    | Iteration count | Slack bus ID | Slack bus mismatch |
+------------------+-----------+-----------------+--------------+--------------------+
| 0                | CONVERGED | 8               | BUS_0        | -0,00954794        |
+------------------+-----------+-----------------+--------------+--------------------+
```

## See also
<span style="color: red">TODO</span> 
