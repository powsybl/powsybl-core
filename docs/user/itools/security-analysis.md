# iTools security-analysis

The `security-analysis` command loads a grid file and runs a [security analysis](../../simulation/security/index.md) simulation, to detect security violations on pre- or post-contingencies states. At the end of the simulation, the results are printed or exported to a file.

## Configuration

To set up the security-analysis command, a configuration file is required. See [iTools Configuration](../itools/index.md#configuration).

If you have several implementations in your classpath, you need to choose which implementation to use in your configuration file.
```yaml
componentDefaultConfig:
  ContingenciesProviderFactory: "<IMPLEMENTATION_NAME>"
load-flow:
  default-impl-name: "<IMPLEMENTATION_NAME>"
```

There are two main implementations for the `ContingenciesProviderFactory` that can be used.
- JSON contingencies list: `com.powsybl.contingency.JsonContingenciesProviderFactory`
- Groovy DSL contingencies list: `com.powsybl.contingency.dsl.GroovyDslContingenciesProviderFactor`
## Usage
``` shell
$> itools security-analysis --help
usage: itools [OPTIONS] security-analysis [--actions-file <FILE>] --case-file
       <FILE> [--contingencies-file <FILE>] [--external] [--help] [-I
       <property=value>] [--import-parameters <IMPORT_PARAMETERS>]
       [--limit-reductions-file <FILE>] [--limit-types <LIMIT-TYPES>]
       [--log-file <FILE>] [--monitoring-file <FILE>] [--output-file <FILE>]
       [--output-format <FORMAT>] [--parameters-file <FILE>] [--strategies-file
       <FILE>] [--with-extensions <EXTENSIONS>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --actions-file <FILE>                     actions file (.json)
    --case-file <FILE>                        the case path
    --contingencies-file <FILE>               the contingencies path
    --external                                external execution
    --help                                    display the help and quit
 -I <property=value>                          use value for given importer
                                              parameter
    --import-parameters <IMPORT_PARAMETERS>   the importer configuration file
    --limit-reductions-file <FILE>            limit reductions file (.json)
    --limit-types <LIMIT-TYPES>               limit type filter (all if not set)
    --log-file <FILE>                         log output path (.zip)
    --monitoring-file <FILE>                  monitoring file (.json) to get
                                              network's infos after computation
    --output-file <FILE>                      the output path
    --output-format <FORMAT>                  the output format [JSON]
    --parameters-file <FILE>                  loadflow parameters as JSON file
    --strategies-file <FILE>                  operator strategies file (.json)
    --with-extensions <EXTENSIONS>            the extension list to enable

Allowed LIMIT-TYPES values are [ACTIVE_POWER, APPARENT_POWER, CURRENT,
LOW_VOLTAGE, HIGH_VOLTAGE, LOW_VOLTAGE_ANGLE, HIGH_VOLTAGE_ANGLE,
LOW_SHORT_CIRCUIT_CURRENT, HIGH_SHORT_CIRCUIT_CURRENT, OTHER]
Allowed EXTENSIONS values are []
```

### Required arguments

`--case-file`<br>
This option defines the path of the case file on which the power flow simulation is run. The [supported formats](../../grid_exchange_formats/index.md) depend on the execution class path.

### Optional arguments

`--contingencies-file`<br>
This option defines the path of the contingency files. If this parameter is not set, the security violations are checked on the base state only. This file is a groovy script that respects the [contingency DSL](../../simulation/security/contingency-dsl.md) syntax.

The supported format depends on the configured `ContingenciesProviderFactory` (see the [`componentDefaultConfig`](../configuration/componentDefaultConfig.md) module):
- Groovy DSL: script that respects the [contingency DSL](../../simulation/security/contingency-dsl.md) syntax
- JSON: a contingencies list (type `ContingencyList`) — minimal example below.
```json
{
  "type" : "default",
  "version" : "1.0",
  "name" : "list",
  "contingencies" : [{
    "id" : "contingency1",
    "elements" : [ {
      "id" : "id1",
      "type" : "BRANCH"
    }]
  }]
}
```

`--strategies-file`  
Path to a JSON file describing operator strategies (sets of “conditional actions” triggered under some conditions, in a contingency context).
 
Example

```json
{
  "version" : "1.2",
  "operatorStrategies" : [ {
    "id" : "strategy_gen_load",
    "contingencyContextType" : "SPECIFIC",
    "contingencyId" : "contingency1",
    "conditionalActions" : [ {
      "id" : "stage1",
      "condition" : {
        "type" : "TRUE_CONDITION"
      },
      "actionIds" : [ "load-action-id", "generator-action-id" ]
    } ]
  } ]
}
```
`--actions-file`  
Path to a JSON file describing the available actions. These actions can then be referenced by operator strategies through their `actionIds`.

Example
```json
{
  "version" : "1.2",
  "actions" : [ {
    "type" : "TERMINALS_CONNECTION",
    "id" : "closeLine",
    "elementId" : "elementId",
    "open" : false
  } ]
}
```

`--limit-reductions-file`  
Path to a JSON file defining limit reductions. See [Limit reductions](../../simulation/security/limit-reductions.md) for principles and details.

`--external`<br>
<span style="color: red">TODO:</span> Use this argument to run the security analysis as an external process.


`--import-parameters`<br>
This option defines the path of the importer's configuration file. It's possible to overload one or many parameters using the `-I property=value` syntax. The list of supported properties depends on the [input format](../../grid_exchange_formats/index.md).

`--limit-types`<br>
This option allows filtering certain types of violations. It overrides the default configuration defined in the [limit-violation-default-filter](../configuration/limit-violation-default-filter.md) configuration module. The supported types are the following: `CURRENT`, `LOW_VOLTAGE`, `HIGH_VOLTAGE`, `LOW_SHORT_CIRCUIT_CURRENT`, `HIGH_SHORT_CIRCUIT_CURRENT` and `OTHER`.

`--log-file`<br>
<span style="color: red">TODO</span>

`--output-file`<br>
This option defines the path of the result file. If this option is not set, the results are printed to the console.

`--output-format`<br>
This option defines the format of the output file. This option is required if the `--output-file` is set. The only supported format is `JSON`.

`--parameters-file`<br>
This option defines the path of the [parameters](#parameters) file of the simulation. If this option is not used, the simulation is run with the default parameters.

`--with-extensions`<br>
This option defines the list of extensions to complete the simulation results with additional data. The available extensions are listed in the usage of the command.

## Simulators
The security analysis computation is performed by a `SecurityAnalysisFactory` implementation available on the classpath (typically provided by a simulator module such as `powsybl-open-loadflow`).

The default implementation is selected through the [`componentDefaultConfig`](../configuration/componentDefaultConfig.md) module (`SecurityAnalysisFactory`). If several implementations are available, the `security-analysis` configuration module may also be used to select a specific one (property `default-impl-name`).

## Contingencies
Contingencies are provided through `--contingencies-file`. The file is interpreted by the configured `ContingenciesProviderFactory` (see [`componentDefaultConfig`](../configuration/componentDefaultConfig.md)).

Typical inputs are:
- Groovy DSL file (see [Contingency DSL](../../simulation/security/contingency-dsl.md))
- JSON contingencies list (type `ContingencyList`, see examples above)

## Parameters
<span style="color: red">TODO</span>

## Results
If `--output-file` is not set, results are printed to the console (tables). If `--output-file` is set, results are exported to the given path and `--output-format` must be provided.

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

### Example 3
The following example shows how to run a security analysis simulation with an `operator strategy` with `actions` for `contingency` of a given network.

```shell
itools security-analysis \
  --case-file network.xiidm \
  --contingencies-file contingency.json \
  --strategies-file strategies.json \
  --actions-file action-reclose.json
```

#### Inputs

<details>
<summary>Network</summary>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<iidm:network xmlns:iidm="http://www.powsybl.org/schema/iidm/1_15" id="sim1" caseDate="2013-01-15T18:45:00.000+01:00" forecastDistance="0" sourceFormat="test" minimumValidationLevel="STEADY_STATE_HYPOTHESIS">
  <iidm:substation id="P1" country="FR" tso="RTE" geographicalTags="A">
    <iidm:voltageLevel id="VLGEN" nominalV="24.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NGEN"/>
      </iidm:busBreakerTopology>
      <iidm:generator id="GEN" energySource="OTHER" minP="-9999.99" maxP="9999.99" voltageRegulatorOn="true" targetP="607.0" targetV="24.5" targetQ="301.0" bus="NGEN" connectableBus="NGEN">
        <iidm:minMaxReactiveLimits minQ="-9999.99" maxQ="9999.99"/>
      </iidm:generator>
    </iidm:voltageLevel>
    <iidm:voltageLevel id="VLHV1" nominalV="380.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NHV1"/>
      </iidm:busBreakerTopology>
    </iidm:voltageLevel>
    <iidm:twoWindingsTransformer id="NGEN_NHV1" r="0.26658461538461536" x="11.104492831516762" g="0.0" b="0.0" ratedU1="24.0" ratedU2="400.0" voltageLevelId1="VLGEN" bus1="NGEN" connectableBus1="NGEN" voltageLevelId2="VLHV1" bus2="NHV1" connectableBus2="NHV1"/>
  </iidm:substation>
  <iidm:substation id="P2" country="FR" tso="RTE" geographicalTags="B">
    <iidm:voltageLevel id="VLHV2" nominalV="380.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NHV2"/>
      </iidm:busBreakerTopology>
    </iidm:voltageLevel>
    <iidm:voltageLevel id="VLLOAD" nominalV="150.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NLOAD"/>
      </iidm:busBreakerTopology>
      <iidm:load id="LOAD" loadType="UNDEFINED" p0="600.0" q0="200.0" bus="NLOAD" connectableBus="NLOAD"/>
    </iidm:voltageLevel>
    <iidm:twoWindingsTransformer id="NHV2_NLOAD" r="0.04724999999999999" x="4.049724365620455" g="0.0" b="0.0" ratedU1="400.0" ratedU2="158.0" voltageLevelId1="VLHV2" bus1="NHV2" connectableBus1="NHV2" voltageLevelId2="VLLOAD" bus2="NLOAD" connectableBus2="NLOAD">
      <iidm:ratioTapChanger regulating="true" lowTapPosition="0" tapPosition="1" targetDeadband="0.0" loadTapChangingCapabilities="true" regulationMode="VOLTAGE" regulationValue="158.0">
        <iidm:terminalRef id="NHV2_NLOAD" side="TWO"/>
        <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="0.8505666905244191"/>
        <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="1.0006666666666666"/>
        <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="1.150766642808914"/>
      </iidm:ratioTapChanger>
    </iidm:twoWindingsTransformer>
  </iidm:substation>
  <iidm:line id="NHV1_NHV2_1" r="3.0" x="33.0" g1="0.0" b1="1.93E-4" g2="0.0" b2="1.93E-4" voltageLevelId1="VLHV1" bus1="NHV1" connectableBus1="NHV1" voltageLevelId2="VLHV2" bus2="NHV2" connectableBus2="NHV2" selectedOperationalLimitsGroupId1="DEFAULT">
    <iidm:operationalLimitsGroup1 id="DEFAULT">
      <iidm:currentLimits permanentLimit="460"/>
    </iidm:operationalLimitsGroup1>
  </iidm:line>
  <iidm:line id="NHV1_NHV2_2" r="3.0" x="33.0" g1="0.0" b1="1.93E-4" g2="0.0" b2="1.93E-4" voltageLevelId1="VLHV1" bus1="NHV1" connectableBus1="NHV1" voltageLevelId2="VLHV2" bus2="NHV2" connectableBus2="NHV2"/>
</iidm:network>
```
</details>

<details>
<summary>Contingency</summary>

```json lines
{
  "type" : "default",
  "version" : "1.0",
  "name" : "list",
  "contingencies" : [{
    "id" : "contingency1",
    "elements" : [ {
      "id" : "NHV1_NHV2_2",
      "type" : "BRANCH"
    }]
  }]
}
```
</details>

<details>
<summary>Strategies</summary>

```json lines
{
  "version" : "1.2",
  "operatorStrategies" : [ {
    "id" : "strategy_gen_load",
    "contingencyContextType" : "SPECIFIC",
    "contingencyId" : "contingency1",
    "conditionalActions" : [ {
      "id" : "stage1",
      "condition" : {
        "type" : "TRUE_CONDITION"
      },
      "actionIds" : [ "decreaseLoadP", "decreaseGenP" ]
    } ]
  } ]
}
```
</details> 

<details>
<summary>Actions</summary>

```json lines
{
  "version" : "1.2",
  "actions" : [
    {
      "type" : "LOAD",
      "id" : "decreaseLoadP",
      "loadId" : "LOAD",
      "relativeValue" : false,
      "activePowerValue" : 250.0
    }, {
      "type" : "GENERATOR",
      "id" : "decreaseGenP",
      "generatorId" : "GEN",
      "activePowerRelativeValue" : false,
      "activePowerValue" : 250.0
    }
  ]
}
```
</details>

#### Output

``` shell
Pre-contingency violations:
+--------+---------------+-----+---------+--------------+----------------+----------------+-------+-------+------------------+----------------+
| Action | Equipment (0) | End | Country | Base voltage | Violation type | Violation name | Value | Limit | abs(value-limit) | Loading rate % |
+--------+---------------+-----+---------+--------------+----------------+----------------+-------+-------+------------------+----------------+
Post-contingency limit violations:
+--------------+-----------+--------+---------------+-------+---------+--------------+----------------+----------------+-----------+----------+------------------+----------------+
| Contingency  | Status    | Action | Equipment (1) | End   | Country | Base voltage | Violation type | Violation name | Value     | Limit    | abs(value-limit) | Loading rate % |
+--------------+-----------+--------+---------------+-------+---------+--------------+----------------+----------------+-----------+----------+------------------+----------------+
| contingency1 | CONVERGED |        | Equipment (1) |       |         |              |                |                |           |          |                  |                |
|              |           |        | NHV1_NHV2_1   | VLHV1 | FR      |          380 | CURRENT        | permanent      | 1008,9288 | 460,0000 |         548,9288 |         219,33 |
+--------------+-----------+--------+---------------+-------+---------+--------------+----------------+----------------+-----------+----------+------------------+----------------+
```

- After adding operator strategies and actions, the simulation result written out by itools does not yet include information about operator strategies remedial action violations.
- `itools` print:
  - ✅ Pre-contingency violations
  - ✅ Post-contingency limit violations
  - ⬜️ Operator strategy remedial action violations
- All simulation results are contained in the output result, which can be retrieved using `--output-file` and `--output-format`.

Example
```shell
itools security-analysis \
  --case-file network.xiidm \
  --contingencies-file contingency.json \
  --strategies-file strategies.json \
  --actions-file action-load-gen.json \
  --output-file "/tmp/result.json" --output-format JSON
```
The result shows that no more limit violations exist.
```json 
...
"operatorStrategyResults" : [ {
    "conditionalActionsResults" : [ {
      "conditionalActionsId" : "strategy_gen_load",
      "status" : "CONVERGED",
      "limitViolationsResult" : {
        "limitViolations" : [ ],  <== no more limit violations
        "actionsTaken" : [ ]
      }
    }]
}]
```

### Example 4
The following example shows how to run a security analysis simulation with `limit reductions` of a given network.

```shell
itools security-analysis \
  --case-file network.xiidm \
  --limit-reductions-file limit-reductions.json
```

#### Inputs

<details>
<summary>Network</summary>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<iidm:network xmlns:iidm="http://www.powsybl.org/schema/iidm/1_15" id="sim1" caseDate="2013-01-15T18:45:00.000+01:00" forecastDistance="0" sourceFormat="test" minimumValidationLevel="STEADY_STATE_HYPOTHESIS">
  <iidm:substation id="P1" country="FR" tso="RTE" geographicalTags="A">
    <iidm:voltageLevel id="VLGEN" nominalV="24.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NGEN"/>
      </iidm:busBreakerTopology>
      <iidm:generator id="GEN" energySource="OTHER" minP="-9999.99" maxP="9999.99" voltageRegulatorOn="true" targetP="607.0" targetV="24.5" targetQ="301.0" bus="NGEN" connectableBus="NGEN">
        <iidm:minMaxReactiveLimits minQ="-9999.99" maxQ="9999.99"/>
      </iidm:generator>
    </iidm:voltageLevel>
    <iidm:voltageLevel id="VLHV1" nominalV="380.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NHV1"/>
      </iidm:busBreakerTopology>
    </iidm:voltageLevel>
    <iidm:twoWindingsTransformer id="NGEN_NHV1" r="0.26658461538461536" x="11.104492831516762" g="0.0" b="0.0" ratedU1="24.0" ratedU2="400.0" voltageLevelId1="VLGEN" bus1="NGEN" connectableBus1="NGEN" voltageLevelId2="VLHV1" bus2="NHV1" connectableBus2="NHV1"/>
  </iidm:substation>
  <iidm:substation id="P2" country="FR" tso="RTE" geographicalTags="B">
    <iidm:voltageLevel id="VLHV2" nominalV="380.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NHV2"/>
      </iidm:busBreakerTopology>
    </iidm:voltageLevel>
    <iidm:voltageLevel id="VLLOAD" nominalV="150.0" topologyKind="BUS_BREAKER">
      <iidm:busBreakerTopology>
        <iidm:bus id="NLOAD"/>
      </iidm:busBreakerTopology>
      <iidm:load id="LOAD" loadType="UNDEFINED" p0="600.0" q0="200.0" bus="NLOAD" connectableBus="NLOAD"/>
    </iidm:voltageLevel>
    <iidm:twoWindingsTransformer id="NHV2_NLOAD" r="0.04724999999999999" x="4.049724365620455" g="0.0" b="0.0" ratedU1="400.0" ratedU2="158.0" voltageLevelId1="VLHV2" bus1="NHV2" connectableBus1="NHV2" voltageLevelId2="VLLOAD" bus2="NLOAD" connectableBus2="NLOAD">
      <iidm:ratioTapChanger regulating="true" lowTapPosition="0" tapPosition="1" targetDeadband="0.0" loadTapChangingCapabilities="true" regulationMode="VOLTAGE" regulationValue="158.0">
        <iidm:terminalRef id="NHV2_NLOAD" side="TWO"/>
        <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="0.8505666905244191"/>
        <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="1.0006666666666666"/>
        <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="1.150766642808914"/>
      </iidm:ratioTapChanger>
    </iidm:twoWindingsTransformer>
  </iidm:substation>
  <iidm:line id="NHV1_NHV2_1" r="3.0" x="33.0" g1="0.0" b1="1.93E-4" g2="0.0" b2="1.93E-4" voltageLevelId1="VLHV1" bus1="NHV1" connectableBus1="NHV1" voltageLevelId2="VLHV2" bus2="NHV2" connectableBus2="NHV2" selectedOperationalLimitsGroupId1="DEFAULT">
    <iidm:operationalLimitsGroup1 id="DEFAULT">
      <iidm:currentLimits permanentLimit="460"/>
    </iidm:operationalLimitsGroup1>
  </iidm:line>
  <iidm:line id="NHV1_NHV2_2" r="3.0" x="33.0" g1="0.0" b1="1.93E-4" g2="0.0" b2="1.93E-4" voltageLevelId1="VLHV1" bus1="NHV1" connectableBus1="NHV1" voltageLevelId2="VLHV2" bus2="NHV2" connectableBus2="NHV2"/>
</iidm:network>
```
</details>

<details>
<summary>Limit Reductions: reduce the current limit of NHV1_NHV2_1 by 10%</summary>

```json lines
{
  "version": "1.0",
  "limitReductions": [
    {
      "value": 0.9,
      "limitType": "CURRENT",
      "monitoringOnly": false,
      "contingencyContext": {
        "contextType": "ALL"
      }
    }
  ]
}
```
</details>

#### Output
The reduction affect results as pre-contingency violations
``` shell
Pre-contingency violations:
+--------+---------------+-------+---------+--------------+----------------+----------------+----------+----------+------------------+----------------+
| Action | Equipment (1) | End   | Country | Base voltage | Violation type | Violation name | Value    | Limit    | abs(value-limit) | Loading rate % |
+--------+---------------+-------+---------+--------------+----------------+----------------+----------+----------+------------------+----------------+
|        | NHV1_NHV2_1   | VLHV1 | FR      |          380 | CURRENT        | permanent      | 456,7690 | 414,0000 |          42,7690 |          99,30 |
+--------+---------------+-------+---------+--------------+----------------+----------------+----------+----------+------------------+----------------+
```
***

<span style="color: red">TODO</span>: to be clean and completed with the following information


### with-extensions
Use the `--with-extensions` parameter to activate a list of `com.powsybl.security.interceptors.SecurityAnalysisInterceptor`
implementations.
