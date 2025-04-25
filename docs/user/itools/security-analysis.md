# iTools security-analysis

The `security-analysis` command loads a grid file and run a [security analysis](../../simulation/security/index.md) simulation, to detect security violations on pre- or post-contingencies states. At the end of the simulation, the results are printed or exported to a file.

## Usage
```
$> itools security-analysis --help
usage: itools [OPTIONS] security-analysis --case-file <FILE>
       [--contingencies-file <FILE>] [--external] [--help] [-I <property=value>]
       [--import-parameters <IMPORT_PARAMETERS>] [--limit-types <LIMIT-TYPES>]
       [--log-file <FILE>] [--output-file <FILE>] [--output-format <FORMAT>]
       [--parameters-file <FILE>] [--skip-postproc] [--with-extensions
       <EXTENSIONS>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --case-file <FILE>                        the case path
    --contingencies-file <FILE>               the contingencies path
    --external                                external execution
    --help                                    display the help and quit
 -I <property=value>                          use value for given importer
                                              parameter
    --import-parameters <IMPORT_PARAMETERS>   the importer configuation file
    --limit-types <LIMIT-TYPES>               limit type filter (all if not set)
    --log-file <FILE>                         log output path (.zip)
    --output-file <FILE>                      the output path
    --output-format <FORMAT>                  the output format [JSON]
    --parameters-file <FILE>                  loadflow parameters as JSON file
    --skip-postproc                           skip network importer post
                                              processors (when configured)
    --with-extensions <EXTENSIONS>            the extension list to enable

Allowed LIMIT-TYPES values are [CURRENT, LOW_VOLTAGE, HIGH_VOLTAGE,
LOW_SHORT_CIRCUIT_CURRENT, HIGH_SHORT_CIRCUIT_CURRENT, OTHER]
Allowed EXTENSIONS values are []
```

### Required arguments

`--case-file`  
This option defines the path of the case file on which the power flow simulation is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path.

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

## Examples

### Example 1
The following example shows how to run a security analysis simulation to detect only pre-contingency violations for a given network:
```
$> itools security-analysis --case-file 20170322_1844_SN3_FR2.uct
```

The analysis results are printed to the console:
```
Loading network '20170322_1844_SN3_FR2.uct'
Pre-contingency violations:
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+
| Action | Equipment (1)       | End     | Country | Base voltage | Violation type | Violation name  | Value      | Limit     | abs(value-limit) | Loading rate % |
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+
|        | FFNGEN71 FFNHV111 1 | FFNHV17 | FR      |           27 | CURRENT        | Permanent limit | 15350.0808 | 9999.0000 |        5351.0808 |         153.52 |
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+
```

### Example 2
The following example shows how to run a security analysis simulation to detect the post-contingency violations status of a given network and a set of contingencies.  

**Content of the contingencies.groovy file:**``
```
$> cat contingencies.groovy
contingency('HV_line_1') {
    equipments 'NHV1_NHV2_1'
}
contingency('HV_line_2') {
    equipments 'NHV1_NHV2_2'
}
```

To run a post-contingencies security analysis, use the `--contingencies-file` argument:
```
$> itools security-analysis --case-file eurostag_example.xiidm --contingencies-file contingencies.groovy
Loading network 'eurostag_example.xiidm'
Pre-contingency violations:
+--------+---------------+-----+---------+--------------+----------------+----------------+-------+-------+------------------+----------------+
| Action | Equipment (0) | End | Country | Base voltage | Violation type | Violation name | Value | Limit | abs(value-limit) | Loading rate % |
+--------+---------------+-----+---------+--------------+----------------+----------------+-------+-------+------------------+----------------+
Post-contingency limit violations:
+-------------+----------+--------+---------------+-------+---------+--------------+----------------+-----------------+-----------+-----------+------------------+----------------+
| Contingency | Status   | Action | Equipment (4) | End   | Country | Base voltage | Violation type | Violation name  | Value     | Limit     | abs(value-limit) | Loading rate % |
+-------------+----------+--------+---------------+-------+---------+--------------+----------------+-----------------+-----------+-----------+------------------+----------------+
| HV_line_1   | converge |        | Equipment (2) |       |         |              |                |                 |           |           |                  |                |
|             |          |        | NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 1008.9289 |  500.0000 |         508.9289 |         201.79 |
|             |          |        | NHV1_NHV2_2   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 1047.8260 |  500.0000 |         547.8260 |         209.57 |
| HV_line_2   | converge |        | Equipment (2) |       |         |              |                |                 |           |           |                  |                |
|             |          |        | NHV1_NHV2_1   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 1008.9289 | 1000.0000 |           8.9289 |         100.89 |
|             |          |        | NHV1_NHV2_1   | VLHV2 | FR      |          380 | CURRENT        | Permanent limit | 1047.8260 | 1000.0000 |          47.8260 |         104.78 |
+-------------+----------+--------+---------------+-------+---------+--------------+----------------+-----------------+-----------+-----------+------------------+----------------+
```

***

<span style="color: red">TODO</span>: to be clean and completed with the following information


### with-extensions
Use the `--with-extensions` parameter to activate a list of `com.powsybl.security.interceptors.SecurityAnalysisInterceptor`
implementations.
