# iTools security-analysis command

itools `security-analysis` command, allows you test pre and post contingencies violation, on imported network from a case file.

In the following sections we refer to installation and sample directories as:

* [\<POWSYBL_HOME\>](../configuration/directoryList.md)
* [\<POWSYBL_SAMPLES\>](../configuration/directoryList.md)
* [\<POWSYBL_SOURCES\>](../configuration/directoryList.md)


## Configuration for running security-analysis command
The configuration  is defined in [powsybl configuration file](../configuration/configuration.md).

Here is an example of a configuration to start a security analysis for 'mock' loadflow implementation(an implementation that does nothing on the network).  Contingencies are defined in a DSL Groovy format.

Note that different loadflow implementations might require specific configurations, in additional dedicated config file's sections.

## YAML version
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory
    SecurityAnalysisFactory: com.powsybl.security.SecurityAnalysisFactoryImpl
    LoadFlowFactory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
    
load-flow-action-simulator:
    load-flow-factory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
    max-iterations: 4
    ignore-pre-contingency-violations: false
    
```

## XML version
```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
   <componentDefaultConfig>
       <ContingenciesProviderFactory>com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
       <SecurityAnalysisFactory>com.powsybl.security.SecurityAnalysisFactoryImpl</SecurityAnalysisFactory>
       <LoadFlowFactory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</LoadFlowFactory>
   </componentDefaultConfig>
    <load-flow-action-simulator>
    <load-flow-factory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</load-flow-factory>
    <max-iterations>4</max-iterations>
    <ignore-pre-contingency-violations>true</ignore-pre-contingency-violations>
  </load-flow-action-simulator>
  
</config>

```

## Running security-analysis command 
Following is an example of how to use the `security-analysis` command, and run it.  
To show the command help, with its specific parameters and descriptions, enter: 

```shell
$>cd  <POWSYBL_HOME>/bin
$> ./itools security-analysis --help
usage: itools [OPTIONS] security-analysis --case-file <FILE>
       [--contingencies-file <FILE>] [--help] [--limit-types <LIMIT-TYPES>]
       [--output-file <FILE>] [--output-format <FORMAT>] [--parameters-file
       <FILE>] [--with-extensions <EXTENSIONS>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --case-file <FILE>               the case path
    --contingencies-file <FILE>      the contingencies path
    --help                           display the help and quit
    --limit-types <LIMIT-TYPES>      limit type filter (all if not set)
    --output-file <FILE>             the output path
    --output-format <FORMAT>         the output format [JSON]
    --parameters-file <FILE>         loadflow parameters as JSON file
    --with-extensions <EXTENSIONS>   the extension list to enable

Allowed LIMIT-TYPES values are [CURRENT, LOW_VOLTAGE, HIGH_VOLTAGE,
LOW_SHORT_CIRCUIT_CURRENT, HIGH_SHORT_CIRCUIT_CURRENT, OTHER]
Allowed EXTENSIONS values are []
```

In order to run the `security-analysis` command, you should provide at least one required argument: 
- `case-file`: path of the input network to analyze.


## Example
In the following example, we will see how to run security analysis without contingencies.

```shell
$> cd <POWSYBL_HOME>/bin
$> ./itools security-analysis --case-file <POWSYBL_SOURCES>/ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct

```
The security analisys will detect only pre-contingency violation.
The results printed on standard output will be:

```shell
Loading network '<POWSYBL_SOURCES>/ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct'
Pre-contingency violations:
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+
| Action | Equipment (1)       | End     | Country | Base voltage | Violation type | Violation name  | Value      | Limit     | abs(value-limit) | Loading rate % |
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+
|        | FFNGEN71 FFNHV111 1 | FFNHV17 | FR      |           27 | CURRENT        | Permanent limit | 15350.0808 | 9999.0000 |        5351.0808 |         153.52 |
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+

```

In the following example, we will see how to run security analysis with contingencies, using optional argument `--contingencies-file`

Define your contingencies using a DSL groovy file, `contingencies_dsl.groovy`,  as showed in the following example:

```java
    contingency('HV_line_1') {
        equipments 'NHV1_NHV2_1'
    }
    
    contingency('HV_line_2') {
        equipments 'NHV1_NHV2_2'
    }
 ```

Run itools security-analysis command, using optional argument:'contingencies-file':

```shell
	$> cd <POWSYBL_HOME>/bin
	$>./itools security-analysis  --case-file <POWSYBL_SAMPLES>/resources/eurostag_example.xiidm --contingencies-file <POWSYBL_SAMPLES>/resources//contingencies_dsl.groovy
```

The security analisys will detect  pre-contingency and post-contingecies violation.
The log will list:

```shell
Loading network '<POWSYBL_SAMPLES>/resources/eurostag_example.xiidm'
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









