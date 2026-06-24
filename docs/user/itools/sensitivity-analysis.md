# iTools sensitivity-analysis

The `sensitivity-analysis` command loads a grid file and runs a [sensitivity analysis](../../simulation/sensitivity/index.md) on it. The sensitivity factors to compute are read from an input file, and the results are exported to a file in JSON or CSV format. The analysis can optionally be run on a set of contingencies, with remedial actions and operator strategies.

## Usage
```
$> itools sensitivity-analysis --help
usage: itools [OPTIONS] sensitivity-analysis [--actions-file <FILE>]
       --case-file <FILE> [--contingencies-file <FILE>] [--factors-file
       <FILE>] [--help] [-I <property=value>] [--import-parameters
       <IMPORT_PARAMETERS>] [--operator-strategies-file <FILE>]
       [--output-file <FILE>] [--output-state-status-file <FILE>]
       [--parameters-file <FILE>] [--single-output] [--variable-sets-file
       <FILE>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --actions-file <FILE>                     actions input file path
    --case-file <FILE>                        the case path
    --contingencies-file <FILE>               contingencies input file path
    --factors-file <FILE>                     sensitivity factors input file
                                              path
    --help                                    display the help and quit
 -I <property=value>                          use value for given importer
                                              parameter
    --import-parameters <IMPORT_PARAMETERS>   the importer configuration file
    --operator-strategies-file <FILE>         operator strategies input file
                                              path
    --output-file <FILE>                      Sensitivity results output path
    --output-state-status-file <FILE>         State status output path (csv
                                              only)
    --parameters-file <FILE>                  sensitivity analysis parameters
                                              as JSON file
    --single-output                           Output sensitivity analysis
                                              results in a single json file
                                              using output file option
                                              (values, factors and contingency
                                              status).
    --variable-sets-file <FILE>               variable sets input file path
```

### Required options

`--case-file`<br>
This option defines the path of the case file on which the sensitivity analysis is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path.

`--factors-file`<br>
This option defines the path of the file listing the [sensitivity factors](../../simulation/sensitivity/index.md#sensitivity-factors) to compute. The file is expected in JSON format.

`--output-file`<br>
This option defines the path where to export the [results](../../simulation/sensitivity/index.md#outputs). The output format is deduced from the file extension: `.json` or `.csv`.

### Optional options

`--contingencies-file`<br>
This option defines the path of the [contingencies](../../simulation/sensitivity/index.md#contingencies) input file. When set, the sensitivity factors are also computed on the post-contingency states. The file is interpreted by the configured contingencies provider (see [`componentDefaultConfig`](../configuration/componentDefaultConfig.md)).

`--operator-strategies-file`<br>
This option defines the path of the operator strategies input file. Operator strategies describe the remedial actions to apply after a contingency. It must be used together with `--actions-file`.

`--actions-file`<br>
This option defines the path of the actions input file referenced by the operator strategies.

`--variable-sets-file`<br>
This option defines the path of the variable sets input file. Variable sets allow grouping several network variables (for example, to compute a sensitivity with respect to a zone) and referencing them from the sensitivity factors.

`--parameters-file`<br>
This option defines the path of the [parameters](../../simulation/sensitivity/configuration.md#parameters) file of the analysis, in JSON format. If this option is not used, the analysis is run with the default parameters.

`--output-state-status-file`<br>
This option defines the path where to export the status of each computed state (base case and contingencies). It is only available when `--output-file` is a CSV file. If it is not set while exporting to CSV, a file is created next to the output file, named after it with the `_contingency_status.csv` suffix.

`--single-output`<br>
When this flag is set, the sensitivity values, the factors and the contingency status are written into a single JSON file given by `--output-file`. This option is only compatible with a JSON output file.

`--import-parameters`<br>
This option defines the path of the importer's configuration file. It's possible to overload one or many parameters using the `-I property=value` syntax. The list of supported properties depends on the [input format](../../grid_exchange_formats/index.md).

## Implementations

The available sensitivity analysis implementations are described [here](../../simulation/sensitivity/index.md#implementations).

## Parameters
The available parameters are described [here](../../simulation/sensitivity/configuration.md#parameters).

## Results
The expected results are described in the [sensitivity analysis documentation](../../simulation/sensitivity/index.md#outputs).

## Examples
The following example shows how to run a sensitivity analysis, using the default configuration:
```
$> itools sensitivity-analysis --case-file case.xiidm --factors-file factors.json --output-file results.json
Loading network 'case.xiidm'
Running analysis...
Analysis done in 218 ms
```

The following example shows how to run a sensitivity analysis on a set of contingencies, exporting the results in CSV format:
```
$> itools sensitivity-analysis --case-file case.xiidm --factors-file factors.json --contingencies-file contingencies.json --output-file results.csv
Loading network 'case.xiidm'
Running analysis...
Analysis done in 442 ms
```
