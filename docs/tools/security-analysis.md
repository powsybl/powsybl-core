# iTools security-analysis command

itools `security-analysis` command allows you test *pre* and *post* contingencies security status of an imported network, from a case file.

In the following sections we refer to installation and sample directories as:

* [\<POWSYBL_HOME\>](../configuration/directoryList.md)
* [\<POWSYBL_SAMPLES\>](../configuration/directoryList.md)
* [\<POWSYBL_SOURCES\>](../configuration/directoryList.md)


## Configuration for running security-analysis command
The parameters are [defined](../configuration/modules/load-flow-action-simulator.md) in [powsybl configuration file](../configuration/configuration.md).  

The factory implementations to use are read from the [`componentDefaultConfig` section](../configuration/modules/componentDefaultConfig.md).

Here is an example of a configuration to start a security analysis for the 'mock' loadflow implementation (an implementation that does nothing on the network).  
Contingencies are defined in a DSL Groovy format.

Note that different loadflow implementations might require specific configurations in additional dedicated config file's sections.

## YAML version
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory
    SecurityAnalysisFactory: com.powsybl.security.SecurityAnalysisFactoryImpl
    LoadFlowFactory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
```

## XML version
```xml
<config>
    <componentDefaultConfig>
        <ContingenciesProviderFactory>com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
        <SecurityAnalysisFactory>com.powsybl.security.SecurityAnalysisFactoryImpl</SecurityAnalysisFactory>
        <LoadFlowFactory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</LoadFlowFactory>
    </componentDefaultConfig>
</config>
```

## Running security-analysis command 
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
    --external                       external execution
    --help                           display the help and quit
    --limit-types <LIMIT-TYPES>      limit type filter (all if not set)
    --output-file <FILE>             the output path
    --output-format <FORMAT>         the output format [JSON]
    --parameters-file <FILE>         loadflow parameters as JSON file
    --task <TASKID>                  task identifier(task-index/task-count)
    --task-count <NTASKS>            number of tasks used for parallelization
    --with-extensions <EXTENSIONS>   the extension list to enable

Allowed LIMIT-TYPES values are [CURRENT, LOW_VOLTAGE, HIGH_VOLTAGE,
LOW_SHORT_CIRCUIT_CURRENT, HIGH_SHORT_CIRCUIT_CURRENT, OTHER]
Allowed EXTENSIONS values are []
```

In order to run the `security-analysis` command, you should provide at least one required argument: 
- `case-file`: path of the input network to analyze.


## Example
In the following example we will see how to run security analysis to detect only pre-contingency violation, for a given network.

```shell
$> cd <POWSYBL_HOME>/bin
$> ./itools security-analysis --case-file <POWSYBL_SOURCES>/ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct

```
The analysis results will be printed to the standard output: 

```shell
Loading network '<POWSYBL_SOURCES>/ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct'
Pre-contingency violations:
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+
| Action | Equipment (1)       | End     | Country | Base voltage | Violation type | Violation name  | Value      | Limit     | abs(value-limit) | Loading rate % |
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+
|        | FFNGEN71 FFNHV111 1 | FFNHV17 | FR      |           27 | CURRENT        | Permanent limit | 15350.0808 | 9999.0000 |        5351.0808 |         153.52 |
+--------+---------------------+---------+---------+--------------+----------------+-----------------+------------+-----------+------------------+----------------+

```

In the following example, we will see how to run security-analysis to identify the post-contingency security status of given network.

To test this scenario we need to list contingencies in a file, expressed in [**iAL**](../architecture/ial/README.md), the **i**Tesla **A**ction **L**anguage, a groovy based DSL (**D**omain **S**pecific **L**anguage).
Following is a sample `contingencies_dsl.groovy`, with two contingencies:

```java
    contingency('HV_line_1') {
        equipments 'NHV1_NHV2_1'
    }
    
    contingency('HV_line_2') {
        equipments 'NHV1_NHV2_2'
    }
 ```
Contingencies_dsl.groovy file, must be declared in the itools security-analysis command line as an additional argument 'contingencies-file':

```shell
$> cd <POWSYBL_HOME>/bin
$>./itools security-analysis  --case-file <POWSYBL_SAMPLES>/resources/eurostag_example.xiidm --contingencies-file <POWSYBL_SAMPLES>/resources//contingencies_dsl.groovy
```


The analysis' results, printed to the standard output: 

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









