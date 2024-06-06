# iTools action-simulator

The `action-simulator` command loads a grid file and run a [security analysis with remedial actions](../../simulation/security/index.md). This tool is used to create or validate a strategy in order to solve violations. 

## Usage
```
$> itools action-simulator --help
usage: itools [OPTIONS] action-simulator [--apply-if-solved-violations]
       --case-file <FILE> [--contingencies <CONTINGENCY1,CONTINGENCY2,...>]
       --dsl-file <FILE> [--export-after-each-round] [--help] [-I
       <property=value>] [--import-parameters <IMPORT_PARAMETERS>]
       [--output-case-folder <CASEFOLDER>] [--output-case-format <CASEFORMAT>]
       [--output-compression-format <COMPRESSION_FORMAT>] [--output-file <FILE>]
       [--output-format <FORMAT>] [--verbose]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --apply-if-solved-violations                       apply the first tested
                                                       action which solves all
                                                       violations
    --case-file <FILE>                                 the case path
    --contingencies <CONTINGENCY1,CONTINGENCY2,...>    contingencies to test
    --dsl-file <FILE>                                  the Groovy DSL path
    --export-after-each-round                          export case after each
                                                       round
    --help                                             display the help and quit
-I <property=value>                                    use value for given
                                                       importer parameter
    --import-parameters <IMPORT_PARAMETERS>            the importer configuation
                                                       file
    --output-case-folder <CASEFOLDER>                  output case folder path
    --output-case-format <CASEFORMAT>                  output case format [CSV,
                                                       AMPL, XIIDM]
    --output-compression-format <COMPRESSION_FORMAT>   output compression format
                                                       [BZIP2, GZIP, XZ, ZIP,
                                                       ZSTD]
    --output-file <FILE>                               the output file path
    --output-format <FORMAT>                           the output file format
                                                       [JSON]
    --verbose                                          verbose mode
```

### Required arguments

**--case-file**  
This option defines the path of the case file on which the power flow simulation is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path.

**--dsl-file**  
This option defines the path of the strategy to evaluate. This is a groovy script that respects the [action DSL](../../simulation/security/action-dsl.md) syntax.

### Optional parameters

**--apply-if-solved-violations**  
<span style="color: red">TODO</span>

**--contingencies**  
This option defines the list of contingencies to simulate. If this parameter is omitted, all the contingencies defined in the DSL file are simulated.

**--export-after-each-round**  
If this option is passed, a case file is exported after each round of the simulation. Otherwise, a single case file is exported at the end of the simulation (once there is no more violations or matching rules).

**\-\-import-parameters**  
This option defines the path of the importer's configuration file. It's possible to overload one or many parameters using the `-I property=value` syntax. The list of supported properties depends on the [input format](../../grid_exchange_formats/index.md).

**\-\-output-case-folder**  
This option defines the path to the folder in which the case files will be exported.

**\-\-output-case-format**  
This option defines the format of the output case files. The list of [supported formats](../../grid_exchange_formats/index.md) are listed between brackets in the command help.

**\-\-output-compression-format**  
This option defines the compression format of the case files. The list of supported formats are listed between brackets in the command help.

**\-\-output-file**  
This option defines the path of the result file. If this option is omitted, the results are displayed in the console.

**\-\-output-format**  
This option defines the format of the result file. This option is required if `--output-file` is used. The supported format are listed between brackets in the command help.

**\-\-verbose**  
This option enables the verbose mode, to display more information during the simulation.

## Simulators
Currently the only simulator which is supported is the [load-flow based]() simulator. 

## Parameters
<span style="color: red">TODO</span>

## Results
<span style="color: red">TODO</span>

## Examples
This example shows a small [action DSL](../../simulation/security/action-dsl.md) script:
```groovy
contingency('HV_line_1') {
    equipments 'NHV1_NHV2_1'
}

contingency('HV_line_2') {
    equipments 'NHV1_NHV2_2'
}

rule('apply_shedding_for_line_1') {
    description 'Test load sheddings when line 1 is overloaded'
    life 8
    when isOverloaded(['NHV1_NHV2_1'])
    apply 'load_shed_100'
}

rule('apply_shedding_for_line_2') {
    description 'Test load sheddings when line 2 is overloaded'
    life 8
    when isOverloaded(['NHV1_NHV2_2'])
    apply 'load_shed_100'
}

action('load_shed_100') {
    description 'load shedding 100 MW'
    tasks {
        script {
            load('LOAD').p0 -= 100
        }
    }
}
```


The following example show the results of the simulation of the previous script:
```shell
$> itools action-simulator --case-file $HOME/eurostag-tutorial.xiidm --dsl-file $HOME/actions.groovy
Loading network '$HOME/eurostag-tutorial.xiidm'
Loading DSL 'file:$HOME/actions.groovy'
Using 'loadflow' rules engine
Starting pre-contingency analysis
    Round 0
        No more violation
Starting post-contingency 'HV_line_1' analysis
    Round 0
        Violations:
+---------------+-------+---------+--------------+----------------+-----------------+-----------+----------+------------------+----------------+
| Equipment (2) | End   | Country | Base voltage | Violation type | Violation name  | Value     | Limit    | abs(value-limit) | Loading rate % |
+---------------+-------+---------+--------------+----------------+-----------------+-----------+----------+------------------+----------------+
| NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 1008.9287 | 500.0000 |         508.9287 |         201.79 |
| NHV1_NHV2_2   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 1047.8258 | 500.0000 |         547.8258 |         209.57 |
+---------------+-------+---------+--------------+----------------+-----------------+-----------+----------+------------------+----------------+
        Rule 'apply_shedding_for_line_2' evaluated to TRUE
        Applying action 'load_shed_100'
    Round 1
        Violations:
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| Equipment (2) | End   | Country | Base voltage | Violation type | Violation name  | Value    | Limit    | abs(value-limit) | Loading rate % |
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 831.3489 | 500.0000 |         331.3489 |         166.27 |
| NHV1_NHV2_2   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 871.7283 | 500.0000 |         371.7283 |         174.35 |
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
        Rule 'apply_shedding_for_line_2' evaluated to TRUE
        Applying action 'load_shed_100'
    Round 2
        Violations:
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| Equipment (2) | End   | Country | Base voltage | Violation type | Violation name  | Value    | Limit    | abs(value-limit) | Loading rate % |
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 667.6796 | 500.0000 |         167.6796 |         133.54 |
| NHV1_NHV2_2   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 711.4252 | 500.0000 |         211.4252 |         142.29 |
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
        Rule 'apply_shedding_for_line_2' evaluated to TRUE
        Applying action 'load_shed_100'
    Round 3
        Violations:
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| Equipment (2) | End   | Country | Base voltage | Violation type | Violation name  | Value    | Limit    | abs(value-limit) | Loading rate % |
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 516.0706 | 500.0000 |          16.0706 |         103.21 |
| NHV1_NHV2_2   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 566.1081 | 500.0000 |          66.1081 |         113.22 |
+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
        Max number of iterations reached
Starting post-contingency 'HV_line_2' analysis
    Round 0
        Violations:
+---------------+-------+---------+--------------+----------------+-----------------+-----------+-----------+------------------+----------------+
| Equipment (2) | End   | Country | Base voltage | Violation type | Violation name  | Value     | Limit     | abs(value-limit) | Loading rate % |
+---------------+-------+---------+--------------+----------------+-----------------+-----------+-----------+------------------+----------------+
| NHV1_NHV2_1   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 1008.9287 | 1000.0000 |           8.9287 |         100.89 |
| NHV1_NHV2_1   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 1047.8258 | 1000.0000 |          47.8258 |         104.78 |
+---------------+-------+---------+--------------+----------------+-----------------+-----------+-----------+------------------+----------------+
        Rule 'apply_shedding_for_line_1' evaluated to TRUE
        Applying action 'load_shed_100'
    Round 1
        No more violation
Final result
Pre-contingency violations:
+--------+---------------+-----+---------+--------------+----------------+----------------+-------+-------+------------------+----------------+
| Action | Equipment (0) | End | Country | Base voltage | Violation type | Violation name | Value | Limit | abs(value-limit) | Loading rate % |
+--------+---------------+-----+---------+--------------+----------------+----------------+-------+-------+------------------+----------------+
Post-contingency limit violations:
+-------------+----------+---------------+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| Contingency | Status   | Action        | Equipment (2) | End   | Country | Base voltage | Violation type | Violation name  | Value    | Limit    | abs(value-limit) | Loading rate % |
+-------------+----------+---------------+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
| HV_line_1   | converge |               | Equipment (2) |       |         |              |                |                 |          |          |                  |                |
|             |          | load_shed_100 |               |       |         |              |                |                 |          |          |                  |                |
|             |          | load_shed_100 |               |       |         |              |                |                 |          |          |                  |                |
|             |          | load_shed_100 |               |       |         |              |                |                 |          |          |                  |                |
|             |          |               | NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 516.0706 | 500.0000 |          16.0706 |         103.21 |
|             |          |               | NHV1_NHV2_2   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 566.1081 | 500.0000 |          66.1081 |         113.22 |
+-------------+----------+---------------+---------------+-------+---------+--------------+----------------+-----------------+----------+----------+------------------+----------------+
```

