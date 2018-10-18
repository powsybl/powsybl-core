# iTools loadflow-validation command

The itools `loadflow-validation` command allows you to [validate load-flow results](../docs/architecture/loadflow-validation/README.md) of a network imported from a case file.
The command, besides validating the results, also print them (print the data of the validated equipments) in output files.  

In the following sections we refer to installation, sample and  sources directories as:

* [\<POWSYBL_HOME\>](../configuration/directoryList.md)
* [\<POWSYBL_SAMPLES\>](../configuration/directoryList.md)
* [\<POWSYBL_SOURCES\>](../configuration/directoryList.md)


## Configuration for running loadflow-validation command
The validation parameters to use are defined in [`loadflow-validation` section](../configuration/modules/loadflow-validation.md) of the [configuration file](../configuration/configuration.md).



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
- `case-file`: path of the input network file.
- `output-folder`: folder path where validation results will be stored.  
  
The `load-flow` and the `run-computation` parameters allow to run a loadflow, or a generic computation, before the validation. The available computations are `loadflow` and `loadflowResultsCompletion` (see the [loadflowResultsCompletion post processor](../architecture/iidm/post-processor/loadflowResultsCompletion.md) for details about the loadflow results completion).  
  
The `groovy-script` parameter allows to run a Groovy script on the network. The command provides the network to be validated and the computation platform as input to the script (variables named `network` and `computationManager`). This way the Groovy script has full access to the network data, and it is able to work with and possibly change it. The script is run after the import, before the validation (and before the computation/loadflow, if the computation/loadflow is called by the command).  
  
The validation process writes the results (the data of the validated equipments) in output files (the format depends on the `table-formatter-factory` used, defined in the [configuration file](../configuration/modules/loadflow-validation.md)), one file for each validation type (flows, generators, etc.). The files are stored in the output folder (see `output-folder` parameter).  
the `output-format` parameter defines the format of the content of the output files:
* `CSV`: in the output files a line contains all values of a validated equipment
* `CSV_MULTILINE`: in the output files the values of an equipment are split in multiple lines, one value for each line
  
Below are examples of content in csv output files, using the 2 values of this parameter.

### CSV
```csv
id;p;q;v;nominalV;reactivePowerSetpoint;voltageSetpoint;connected;regulationMode;bMin;bMax;mainComponent;validation
CSPCH.TC1;-0,00000;93,6368;238,307;225,000;0,00000;238,307;true;VOLTAGE;-0,00197531;0,00493827;true;success
CSPDO.TC1;-0,00000;0,00000;240,679;225,000;0,00000;240,713;true;VOLTAGE;-0,00493827;0,00493827;true;success
...
```

### CSV_MULTILINE
```csv
id;characteristic;value
CSPCH.TC1;p;-0,00000
CSPCH.TC1;q;93,6368
CSPCH.TC1;v;238,307
...
```

The `compare-results` parameter is used to run 2 validations.  
The values for the `compare-results` parameter are:
* `COMPUTATION`: perform the validation of a case (`case-file` parameter) before and after the loadflow/computation (`load-flow`/`run-computation` parameters) 
* `BASECASE`: perform the validation of two cases (`case-file` and `compare-case-file` parameters)

The output files will contain the data of both validations: the columns with the suffix `_postComp` refer to the second validation. Following is an example of a csv output file created using this parameter.   

```csv
id;p;q;v;nominalV;reactivePowerSetpoint;voltageSetpoint;connected;regulationMode;bMin;bMax;mainComponent;validation;p_postComp;q_postComp;v_postComp;nominalV_postComp;reactivePowerSetpoint_postComp;voltageSetpoint_postComp;connected_postComp;regulationMode_postComp;bMin_postComp;bMax_postComp;mainComponent_postComp;validation_postComp
CSPCH.TC1;-0,00000;93,6368;238,307;225,000;0,00000;238,307;true;VOLTAGE;-0,00197531;0,00493827;true;success;-0,00000;93,6363;238,307;225,000;0,00000;238,307;true;VOLTAGE;-0,00197531;0,00493827;true;success
CSPDO.TC1;-0,00000;0,00000;240,679;225,000;0,00000;240,713;true;VOLTAGE;-0,00493827;0,00493827;true;success;-0,00000;0,00000;240,679;225,000;0,00000;240,713;true;VOLTAGE;-0,00493827;0,00493827;true;success
...
```
  
The following table summarizes the possible combinations of `compare-results`, `run-computation` and `groovy-script` parameters, and the corresponding case states validated and written in the output files. Some remarks:
* State 1 is the state analyzed in the first validation, State 2 is the state analyzed in the second validation (columns with the suffix `_postComp` in the output files)
* Case 1 is the value of `case-file` parameter, Case 2 is the value of `compare-case-file` parameter
* `--run-computation loadflow` is equivalent to `--load-flow`
* some combinations are not available, e.g. if you use the `compare-results` parameter, with the `COMPUTATION` value, you have to use the `run-computation` (or `load-flow`) parameter
  
| Number  | `compare-results` | `run-computation` | `groovy-script` | State 1 | State 2 (`_postComp`) |
| ------- | ------- | ------- | ------- | ------- | ------- |
| 1 | absent | absent | absent | Case 1 after import | None |
| 2 | absent | `loadflow`/`loadflowResultsCompletion` | absent | Case 1 after import and computation | None |
| 3 | absent | absent | script | Case 1 after import and Groovy script | None |
| 4 | absent | `loadflow`/`loadflowResultsCompletion` | script | Case 1 after import, Groovy script and computation | None |
| 5 | `BASECASE` | absent | absent | Case 1 after import | Case 2 after import |
| 6 | `BASECASE` | `loadflow`/`loadflowResultsCompletion` | absent | Case 1 after import and computation | Case 2 after import |
| 7 | `BASECASE` | absent | script | Case 1 after import and Groovy script | Case 2 after import |
| 8 | `BASECASE` | `loadflow`/`loadflowResultsCompletion` | script | Case 1 after import, Groovy script and computation | Case 2 after import |
| 9 | `COMPUTATION` | `loadflow`/`loadflowResultsCompletion` | absent | Case 1 after import | Case 1 after import and computation |
| 10 | `COMPUTATION` | `loadflow`/`loadflowResultsCompletion` | script | Case 1 after import and Groovy script | Case 1 after import, Groovy script and computation |

The following table depicts, in another way, the states that can be validated by the itools command, referring to the combinations of parameters listed in the table above.  

| **State 1 \ State 2** | **None** | **Case 2 after import** | **Case 1 after import and computation** | **Case 1 after import, Groovy script and computation** |
| ------- | ------- | ------- | ------- | ------- |
| **Case 1 after import** | 1 | 5 | 9 | N.A. |
| **Case 1 after import and computation** | 2 | 6 | N.A. | N.A. |
| **Case 1 after import and Groovy script** | 3 | 7 | N.A. | 10 |
| **Case 1 after import, Groovy script and computation** | 4 | 8 | N.A. | N.A. |

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

Eventually, you will find in your output-folder one csv file for each validation type, containing the data pre and post computation (loadflow).

<!-- MRA: OK for me -->



