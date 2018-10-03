# iTools run-impact-analysis command

itools `run-impact-analysis` command, allows you to run dynamic simulations on a selected network case (or a list of cases) 
for all the configured contingencies and re-calculate all the predefined security-indexes. 

## Configuration for running run-impact-analysis command
The impact analysis parameters are [defined](../configuration/modules/simulation-parameters.md) in the [configuration file](../configuration/configuration.md)

NOTE: no impact-analysis implementations are available in powsybl-core. Ref. to project [iPST](https://github.com/itesla/ipst) to find one implementation: [pclfsim](https://github.com/itesla/ipst/tree/master/pclfsim-integration)

## Running run-impact-analysis command 
Following is an example of how to use the `run-impact-analysis` command, and run it.  
To show the command help, with its specific parameters and descriptions, enter: 

```
$> cd <POWSYBL_HOME>/bin
$> ./itools run-impact-analysis --help
usage: itools [OPTIONS] run-impact-analysis --case-file <FILE> [--contingencies
       <LIST>] [--help] [--output-csv-file <FILE>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --case-file <FILE>         the case path
    --contingencies <LIST>     contingencies to test separated by , (all the db
                               in not set)
    --help                     display the help and quit
    --output-csv-file <FILE>   output CSV file path (pretty print on standard
                               output if not specified)
```

