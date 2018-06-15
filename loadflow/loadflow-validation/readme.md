# Loadflow validation

The goal of the loadflow validation tool is to check that loadflow results, either read from an iidm Network model, or computed by a LoadFlow implementation, are consistent with power systems equations.

The tool reports validation results in output files, with validation data and a success indicator for each equipment which has been tested.


## Configuration

You may configure the following properties in your platform configuration file.

```
<loadflow-validation>
	<threshold>0.1</threshold>
	<verbose>false</verbose>
	<load-flow-factory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</load-flow-factory>
	<table-formatter-factory>com.powsybl.commons.io.table.CsvTableFormatterFactory</table-formatter-factory>
	<epsilon-x>0.1</epsilon-x>
	<apply-reactance-correction>false</apply-reactance-correction>
	<output-writer>CSV_MULTILINE</output-writer>
	<ok-missing-values>false</ok-missing-values>
	<no-requirement-if-reactive-bound-inversion>false</no-requirement-if-reactive-bound-inversion>
	<compare-results>false</compare-results>
	<check-main-component-only>true</check-main-component-only>
	<no-requirement-if-setpoint-outside-power-bounds>false</no-requirement-if-setpoint-outside-power-bounds>
</loadflow-validation>
```

* *threshold*: margin used for values comparison; default value is 0
* *verbose*: verbose output, default value is false
* *load-flow-factory*: load flow factory class; if not defined, the default configuration is used
* *table-formatter-factory* table formatter factory class; default value is com.powsybl.commons.io.table.CsvTableFormatterFactory
* *epsilon-x*: value used to correct the reactance in flows validation, used only if apply-reactance-correction is true; default value is 0.1
* *apply-reactance-correction*: apply reactance correction in flows validation; default value is false
* *output-writer*: output format [CSV, CSV_MULTILINE], default is CSV_MULTILINE
* *ok-missing-values*: perform validation check even if some parameters of connected components have NaN values; default value is false (i.e. validation check fails if some parameters of connected components have NaN Values)
* *no-requirement-if-reactive-bound-inversion*: return validation success if there is a reactive bounds inversion (maxQ < minQ); default is false
* *compare-results*: compare results of two validations, printing output files with results of both ones
* *check-main-component-only*: validate only the equipment in the main connected component, default is true
* *no-requirement-if-setpoint-outside-power-bounds*: return validation success if the set point is outside the active power bounds (targetP < minP or targetP > maxP); default is false

## itools command

You can find below the help of the tool, as returned by the "--help" option.

```
$ ./itools loadflow-validation --help
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
    --compare-case-file <FILE>                      path to the case file to
                                                    compare
    --compare-results <COMPARISON_TYPE>             compare results of two
                                                    validations, printing output
                                                    files with results of both
                                                    ones. Available comparisons
                                                    are [COMPUTATION(compare the
                                                    validation of a basecase
                                                    before and after the
                                                    computation),
                                                    BASECASE(compare the
                                                    validation of two
                                                    basecases)]
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
                                                    [loadflow,
                                                    loadflowResultsCompletion]
    --types <VALIDATION_TYPE,VALIDATION_TYPE,...>   validation types [FLOWS,
                                                    GENERATORS, BUSES, SVCS,
                                                    SHUNTS, TWTS] to run, all of
                                                    them if the option if not
                                                    specified
    --verbose                                       verbose output


```
