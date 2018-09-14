# iTools loadflow-validation command

itools `loadflow-validation` command, allows you to validate load-flow results of a network imported from a case file.

In the following sections we refer to installation, sample and  sources directories as:

* [\<POWSYBL_HOME\>](../configuration/directoryList.md)
* [\<POWSYBL_SAMPLES\>](../configuration/directoryList.md)
* [\<POWSYBL_SOURCES\>](../configuration/directoryList.md)


## Configuration for running loadflow-validation command
The loadflow-validation implementation to use is defined in powsybl's configuration file, whose default location is `$HOME/.itools/config.xml`, ref. powsybl-core [powsybl configuration file](../configuration/configuration.md).



## Running loadflow-validation command 
Following is an example of how to use the `loadflow-validation` command, and run it.  
To show the command help, with its specific parameters and descriptions, enter: 

```shell
$> cd <POWSYBL_HOME>/bin
$> ./itools loadflow-validation --help
usage: itools [OPTIONS] loadflow-validation --case-file <FILE>
       [--compare-case-file <FILE>] [--compare-results <COMPARISON_TYPE>]
       [--groovy-script <FILE>] [--help] [--load-flow] --output-folder <FOLDER>
       [--output-format <VALIDATION_WRITER>] [--run-computation <COMPUTATION>]
       [--types <VALIDATION_TYPE,VALIDATION_TYPE,...>] [--verbose]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --case-file <FILE>                              case file path
    --compare-case-file <FILE>                      path to the case file to compare
    --compare-results <COMPARISON_TYPE>             compare results of two
                                                    validations, printing output
                                                    files with results of both
                                                    ones. Available comparisons
                                                    are [COMPUTATION (compare
                                                    the validation of a basecase
                                                    before and after the
                                                    computation), BASECASE
                                                    (compare the validation of
                                                    two basecases)]
    --groovy-script <FILE>                          groovy script to run before
                                                    validation
    --help                                          display the help and quit
    --load-flow                                     run loadflow
    --output-folder <FOLDER>                        output folder path
    --output-format <VALIDATION_WRITER>             output format [CSV,
                                                    CSV_MULTILINE]
    --run-computation <COMPUTATION>                 run a computation on the
                                                    network before validation,
                                                    available computations are
                                                    [loadflowResultsCompletion,
                                                    loadflow]
    --types <VALIDATION_TYPE,VALIDATION_TYPE,...>   validation types [FLOWS,
                                                    GENERATORS, BUSES, SVCS,
                                                    SHUNTS, TWTS] to run, all of
                                                    them if the option if not
                                                    specified
    --verbose                                       verbose output



```

In order to run the `loadflow-validation` command, you must provide at least these required arguments: 
- case-file: path of the input network file.
- output-folder: folder path where validation results will be stored.

## Example1
In this example a loadflow validation will be run on an UCTE network model. 

```shell
$> cd <POWSYBL_HOME>/bin
$>./itools loadflow-validation --case-file <POWSYBL_SOURCES>/ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct --output-folder /tmp/loadFlowValidationResults
```

The validation results, printed to the standard output:
```shell
Loading case <POWSYBL_SOURCES>ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: TWTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: FLOWS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: BUSES - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SVCS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SHUNTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: GENERATORS - result: fail
```

Eventually, you will find in your output-folder one csv file for each validation type.

## Example2
In this example we are comparing results of two validation: before and after load flow computation. 
Two additional arguments are needed,
- `load-flow`
- `compare_results`: COMPUTATION

```shell
$> cd <POWSYBL_HOME>/bin
$>  ./itools loadflow-validation --case-file <POWSYBL_SOURCES>/ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct --output-folder tmp/loadFlowValidationResults --verbose --output-format CSV --load-flow --compare-results COMPUTATION
```

The validation results, printed to the standard output:
```shell
Loading case <POWSYBL_SOURCES>ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct
Running pre-loadflow validation on network 20170322_1844_SN3_FR2.uct.uct
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: TWTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: GENERATORS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: FLOWS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SHUNTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: BUSES - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SVCS - result: success
Running loadflow on network 20170322_1844_SN3_FR2.uct
Running post-loadflow validation on network 20170322_1844_SN3_FR2.uct
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: TWTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: GENERATORS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: FLOWS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SHUNTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: BUSES - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SVCS - result: success
```

Eventually, you will find in your output-folder one csv file for each validation type, pre and post computation.



