# iTools shortcircuit

The `shortcircuit` command loads a grid file and runs a [short-circuit analysis](../../simulation/shortcircuit/index.md) on a list of faults. At the end of the computation, the results are printed or exported to a file.

## Usage
```
$> itools shortcircuit --help
usage: itools [OPTIONS] shortcircuit --case-file <FILE> [--fault-parameters-file
       <FILE>] [--help] --input-file <FILE> [--output-file <FILE>]
       [--output-format <FORMAT>] [--parameters-file <FILE>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --case-file <FILE>              the case path
    --fault-parameters-file <FILE>  fault parameters file (.json) to get
                                    network's info after computation
    --help                          display the help and quit
    --input-file <FILE>             fault list as JSON file
    --output-file <FILE>            the output path
    --output-format <FORMAT>        the output format [JSON]
    --parameters-file <FILE>        short circuit parameters as JSON file
```

### Required options

`--case-file`<br>
This option defines the path of the case file on which the short-circuit analysis is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path.

`--input-file`<br>
This option defines the path of the JSON file listing the [faults](../../simulation/shortcircuit/inputs.md) to compute.

### Optional options

`--parameters-file`<br>
This option defines the path of the [parameters](../../simulation/shortcircuit/parameters.md) file of the analysis, in JSON format. If this option is not used, the analysis is run with the default parameters.

`--fault-parameters-file`<br>
This option defines the path of a JSON file providing the fault-specific parameters used to complete the network information after the computation.

`--output-file`<br>
This option defines the path where to export the [results](../../simulation/shortcircuit/outputs.md). If this option is not set, the results are printed to the console.

`--output-format`<br>
This option defines the format of the output file. This option is required if `--output-file` is set. The only supported format is `JSON`.

## Results
The expected results are described in the [short-circuit analysis documentation](../../simulation/shortcircuit/outputs.md).

## Examples
The following example shows how to run a short-circuit analysis, using the default configuration:
```
$> itools shortcircuit --case-file case.xiidm --input-file faults.json
```
