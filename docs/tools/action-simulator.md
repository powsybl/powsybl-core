# iTools action-simulator command

itools `action-simulator` command allows you to test remedial actions on imported network from a case file.

In the following sections we refer to installation and sample directories as:

* [\<POWSYBL_HOME\>](../configuration/directoryList.md)
* [\<POWSYBL_SAMPLES\>](../configuration/directoryList.md)


## Configuration for running action-simulator command
The configuration is defined in [powsybl configuration file](../configuration/configuration.md).

This module needs to be in the configuration if remedial actions defined in the DSL are to be simulated with load flows.

Here is an example of configuration for a 'mock' loadflow implementation  (an implementation that does nothing on the network). 
Note that different loadflow implementations might require specific configurations, in additional dedicated config file's sections.

## YAML version
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory
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
    <LoadFlowFactory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</LoadFlowFactory>
  </componentDefaultConfig>
  <load-flow-action-simulator>
    <load-flow-factory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</load-flow-factory>
    <max-iterations>4</max-iterations>
    <ignore-pre-contingency-violations>false</ignore-pre-contingency-violations>
  </load-flow-action-simulator>
</config>
```

 Property | Type | Default value | Required | Comment |
| -------- | ---- | ------------- | -------- | ------- |
| load-flow-factory | Class | | true | Implementation of the load flow |
| max-iterations | Integer | | true | Maximum loadflow rounds (avoid infinite loops) |
| ignore-pre-contingency-violations | Boolean | false | false | Proceed with N-k simulations even if there are pre-contingency violations |

## Running action-simulator command 
Following is an example of how to use the `action-simulator` command.
To show the command help, with its specific parameters and descriptions, enter: 

```shell
$> cd <POWSYBL_HOME>/bin
$> ./itools  action-simulator --help
usage: itools [OPTIONS] action-simulator [--apply-if-solved-violations]
       --case-file <FILE> [--contingencies <CONTINGENCY1,CONTINGENCY2,...>]
       --dsl-file <FILE> [--help] [--output-case-folder <CASEFOLDER>]
       [--output-case-format <CASEFORMAT>] [--output-compression-format
       <COMPRESSION_FORMAT>] [--output-file <FILE>] [--output-format <FORMAT>]
       [--task <TASKID>] [--task-count <NTASKS>] [--verbose]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --apply-if-solved-violations                       apply the first tested
                                                       action which solves all
                                                       violations
    --case-file <FILE>                                 the case path
    --contingencies <CONTINGENCY1,CONTINGENCY2,...>    contingencies to test
    --dsl-file <FILE>                                  the Groovy DSL path
    --help                                             display the help and quit
    --output-case-folder <CASEFOLDER>                  output case folder path
    --output-case-format <CASEFORMAT>                  output case format
                                                       [XIIDM, ADN, AMPL]
    --output-compression-format <COMPRESSION_FORMAT>   output compression format
                                                       [GZIP, BZIP2, ZIP]
    --output-file <FILE>                               the output file path
    --output-format <FORMAT>                           the output file format
                                                       [JSON]
    --task <TASKID>                                    task identifier
                                                       (task-index/task-count)
    --task-count <NTASKS>                              number of tasks used for
                                                       parallelization
    --verbose                                          verbose mode

```
                                        
In order to run the `action-simulator` command, you must provide at least two required arguments: 
- `case-file`: path of the input network to analyze.
- `dsl-file`: a file with your power system simulation scenario: rules, actions and/or contingencies, expressed in [**iAL**](../architecture/ial/README.md), the **i**Tesla **A**ction **L**anguage, a groovy based DSL (**D**omain **S**pecific **L**anguage).


## Example 
In the following example we will see how to simulate  load-shedding corrective actions, when two lines are overloaded.  

Contingencies, rules and and action must be define in a DSL grovvy file - `contingencies_actions_DSL.groovy` -  as showed in the following:

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

Run itools command:

```shell
   $> cd <POWSYBL_HOME>/bin
   $> ./itools action-simulator --case-file <POWSYBL_SAMPLES>/resources/eurostag-example.xiidm --dsl-file <POWSYBL_SAMPLES>/resources/contingencies_actions_DSL.groovy
```

The results printed on standard output will be the results of pre and post contingencies analysis.

**Note:** To get the results in this table, powsybl was configured to use a real loadflow engine (RTE's Hades2LF, ref. [Hades2LF](http://www.rte.itesla-pst.org/)) 


```shell
Loading network '<POWSYBL_SAMPLES>/resources/eurostag-example.xiidm'
Loading DSL 'file:<POWSYBL_SAMPLES>/resources/contingencies_actions_DSL.groovy'
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
        
