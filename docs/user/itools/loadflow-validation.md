# iTools loadflow-validation

The `loadflow-validation` command is used to validate load-flow results of a network. The command, besides validating
the results, also prints the data of the validated equipments in output files.
The consistency checks performed by the load flow validation may also be applied to results obtained with an optimal power flow or to the final state of a long dynamic simulation.

## Usage

```
$> itools loadflow-validation --help
usage: itools [OPTIONS] loadflow-validation --case-file <FILE>
       [--compare-case-file <FILE>] [--compare-results <COMPARISON_TYPE>]
       [--help] [-I <property=value>] [--import-parameters <IMPORT_PARAMETERS>]
       [--load-flow] --output-folder <FOLDER> [--output-format
       <VALIDATION_WRITER>] [--run-computation <COMPUTATION>] [--types
       <VALIDATION_TYPE,VALIDATION_TYPE,...>] [--verbose]
       [--with-extensions-validation]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --case-file <FILE>                              case file path
    --compare-case-file <FILE>                      path to the case file to
                                                    compare
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
    --help                                          display the help and quit
 -I <property=value>                                use value for given importer
                                                    parameter
    --import-parameters <IMPORT_PARAMETERS>         the importer configuation
                                                    file
    --load-flow                                     run loadflow
    --output-folder <FOLDER>                        output folder path
    --output-format <VALIDATION_WRITER>             output format [CSV,
                                                    CSV_MULTILINE]
    --run-computation <COMPUTATION>                 run a computation on the
                                                    network before validation,
                                                    available computations are
                                                    [loadflow,
                                                    loadflowResultsCompletion]
    --types <VALIDATION_TYPE,VALIDATION_TYPE,...>   validation types [FLOWS,
                                                    GENERATORS, BUSES, SVCS,
                                                    SHUNTS, TWTS, TWTS3W] to
                                                    run, all of them if the
                                                    option if not specified
    --verbose                                       verbose output
```

### Required arguments

`--case-file`<br>
Use the `--case-file` parameter to define the path of the case file.

`--output-folder`<br>
Use the `--output-folder` parameter to define the path of the folder where the output files will be stored.


### Optional arguments

`--compare-case-file`<br>
Use the `--compare-case-file` parameter to define the path of the second case file, in order to compare the load flow
results of two case files.

`--compare-results`<br>
Use the `--compare-results` parameter to define the type of results to compare. The available types are:
- `BASECASE`: compare results of the two base cases
- `COMPUTATION`: run a computation on the two base cases and compare results of the resulting states.

`--import-parameters`<br>
Use the `--import-parameters` parameter to specify the path of the configuration file of the importer. It is possible to
overload one or many parameters using the `-I property=value` parameter. The properties depend on the input format.
Refer to the documentation page of each [importer](../../grid_exchange_formats/index.md) to know their specific configuration.

`--load-flow`<br>
Use the `--load-flow` parameter to run a load-flow before the validation. This option is equivalent to
`--run-computation loadflow`.

`--output-format`<br>
Use the `--output-format` parameter to specify the format of the output files. The available output formats are `CSV` or `CSV_MULTILINE`.

If this parameter is set to `CSV`, in the output files a line contains all values of validated equipment. If the parameter
is set to `CSV_MULTILINE`, in the output files the values of a piece of equipment are split in multiple lines, one value for each
line, see examples below:

**CSV**
```
id;p;q;v;nominalV;reactivePowerSetpoint;voltageSetpoint;connected;regulationMode;bMin;bMax;mainComponent;validation
CSPCH.TC1;-0,00000;93,6368;238,307;225,000;0,00000;238,307;true;VOLTAGE;-0,00197531;0,00493827;true;success
CSPDO.TC1;-0,00000;0,00000;240,679;225,000;0,00000;240,713;true;VOLTAGE;-0,00493827;0,00493827;true;success
...
```

**CSV_MULTILINE**
```
id;characteristic;value
CSPCH.TC1;p;-0,00000
CSPCH.TC1;q;93,6368
CSPCH.TC1;v;238,307
...
```

`--run-computation`<br>
Use the `--run-computation` parameter to run a computation before the validation. The supported computations are:
- `loadflow`: run a load-flow
- `loadflowResultsCompletion`: compute the missing `P`, `Q`, `V` and $\theta$ values

`--types`<br>
Use the `--types` parameter to define the types of checks to run. If this parameter is not set, run all the checks.
The supported types are `FLOWS`, `GENERATORS`, `BUSES`, `SVCS`, `SHUNTS`, `TWTS`.

`--with-extensions-validation`<br>
Use the `--with-extensions-validation` parameter to perform extensions validation.

To learn more about the different checks, read the [loadflow validation](../../grid_features/loadflow_validation/index.md) documentation page.

### Summary
The following table summarizes the possible combinations of `compare-results` and `run-computation` parameters, and the
corresponding case states validated and written in the output files. Some remarks:
- State 1 is the state analyzed in the first validation
- State 2 is the state analyzed in the second validation (columns with the suffix `_postComp` in the output files)
- Case 1 is the value of `case-file` parameter
- Case 2 is the value of `compare-case-file` parameter
- some combinations are not available, e.g., if you use the `compare-results` parameter, with the `COMPUTATION` value,
you have to use the `run-computation` (or `load-flow`) parameter.

| Number | compare-results | run-computation                        | State 1                             | State 2 (_postComp)                 |
|--------|-----------------|----------------------------------------|-------------------------------------|-------------------------------------|
| 1      | absent          | absent                                 | Case 1 after import                 | None                                |
| 2      | absent          | `loadflow`/`loadflowResultsCompletion` | Case 1 after import and computation | None                                |
| 3      | `BASECASE`      | absent                                 | Case 1 after import                 | Case 2 after import                 |
| 4      | `BASECASE`      | `loadflow`/`loadflowResultsCompletion` | Case 1 after import and computation | Case 2 after import                 |
| 5      | `COMPUTATION`   | `loadflow`/`loadflowResultsCompletion` | Case 1 after import                 | Case 1 after import and computation |

## Parameters

To learn how to configure the `loadflow-validation` command, read the documentation of the
[loadflow validation](../../grid_features/loadflow_validation/configuration.md) configuration page.

You may also configure the load flow itself to tune the load flow validation using the `--run-computation` option (check the [loadflow configuration page](../../simulation/loadflow/configuration.md#implementation)).

## Examples

### Example 1
The following example shows how to run a load flow validation on a UCTE network model:
```
$> itools loadflow-validation --case-file 20170322_1844_SN3_FR2.uct --output-folder /tmp/results
```

The validation results, printed to the standard output:
```
Loading case 20170322_1844_SN3_FR2.uct
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: TWTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: FLOWS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: BUSES - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SVCS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SHUNTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: GENERATORS - result: fail
```

Eventually, you will find in your output-folder one csv file for each validation type.

### Example 2
In this example, we are comparing results of two validations: before and after load flow computation. Two additional
arguments are needed:
- `load-flow`
- `compare_results`: COMPUTATION

```
$> itools loadflow-validation --case-file 20170322_1844_SN3_FR2.uct --output-folder tmp/loadFlowValidationResults
--verbose --output-format CSV --load-flow --compare-results COMPUTATION
```

The validation results, printed to the standard output:
```
Loading case 20170322_1844_SN3_FR2.uct
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

Eventually, you will find in your output-folder one csv file for each validation type, containing the data pre- and post-computation (load flow).

