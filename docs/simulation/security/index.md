# Security analysis

```{toctree}
:hidden:
configuration.md
implementations.md
contingency-dsl.md
action-dsl.md
limit-reductions.md
```

The security analysis is a simulation that checks violations on a network. These checks can be done on the base case or
after a contingency, with or without remedial actions. A security analysis can monitor network states, in
pre-contingency state, after a contingency and after a remedial action.

There is a violation if the computed value is greater than the maximum allowed value. Depending on the equipment, the
violations can have different types:

- Current, active power and apparent power: this kind of violation can be detected on a branch (line, two-winding
  transformer, tie line) or on a three-winding transformer, if the computed value is greater than
  its [permanent limit](../../grid_model/additional.md#loading-limits) or one of
  its [temporary limits](../../grid_model/additional.md#loading-limits).
- Voltage: this kind of violation can be detected on a bus or a bus bar section, if the computed voltage is out of the
  low-high voltage limits of a [voltage level](../../grid_model/network_subnetwork.md#voltage-level).
- Voltage angle: this kind of violation can be detected if the voltage angle difference between the buses associated to
  two terminals is out of the low-high voltage angle limits defined in the network.

## Inputs

### Network

The first input of the security analysis is a network. As this simulation is based on a [load flow](../loadflow/index)
engine for a list of contingencies, this network should converge in the pre-contingency state.

### Contingencies

The security analysis needs a list of contingencies as an input. When contingencies are provided, the violations are
detected on the network at pre-contingency state, but also after applying each contingency. The supported elementary
contingencies are:

- Generator contingency
- Static var compensator contingency
- Load contingency
- Bus contingency for bus/breaker topologies
- Busbar section contingency for node/breaker topologies
- Line, two-winding transformer and tie line contingencies (branch contingency)
- Three-winding transformer contingency
- Dangling line contingency
- HVDC line contingency
- Battery contingency
- Shunt Compensator contingency

A contingency is made of contingency elements. A contingency can trigger one element at a time (N-1) or several elements
at a time (N-K). Bus bar and bus contingencies are special N-K contingencies as they trigger all the equipments
connected to a given bus bar section.

### Remedial actions

Remedial actions are actions that are applied when limit violations occur. Supported actions are:

- Open or close a switch
- Open or close a terminal
- Change the tap of a tap changer (phase or ratio)
- Change the active and/or reactive power of a load
- Change the section of a shunt compensator
- Change the regulation status of a tap changer
- Change `targetP`, `targetQ`, regulation status and `targetV` of a generator
- Change the regulation mode of a static var compensator and its associated set point.
- Enabled or disabled AC emulation for HVDC line (with the possibility to change `P0` and `droop` for AC emulation and
  active power set point and converter mode for set point operating mode)

Remedial actions can be *preventive* or *curative*:

- preventive: these actions are implemented before the violation occurs, for example if the flow of a monitored line is
  between `90%` and `100%`. Use contingency context `NONE` for that.
- curative: these actions are implemented after a violation occurs, for example, if the flow of the monitored line is
  greater than `100%`.

### Conditions

Actions are applied if a condition is met. The conditions can be diversified and extended in the future:

- True condition: meaning that the list of actions is applied.
- All violations condition on a list of elements: meaning that the list of actions is applied only if all elements
  provided are overloaded.
- At least one violation condition: meaning that the list of actions is applied only if a violation occurs on the
  network.
- Any violation condition on a list of elements: meaning that the list of actions is applied if one or more elements
  provided are overloaded.

### Operator strategies

An operator strategy is applied in pre-contingency or after a contingency, depending on the contingency context
provided. A contingency context can be a pre-contingency state only (`NONE`), a post-contingency state (on a specific
contingency (`SPECIFIC`) or on every contingency (`ONLY_CONTINGENCIES`)) or both pre-contingency and post-contingency
states (`ALL`).

An operator strategy groups a condition and a list of remedial actions.

### State monitors

A stateMonitor allows getting information about branch, bus and three-winding transformers on the network after a
security analysis computation. Contingency context allows to specify if the information asked are about pre-contingency
state or post-contingency state with a contingency id or both. For example:

- If we want information about a branch after security analysis on contingency `c1`, the contingencyContext will contain
  the contingencyId `c1`, contextType `SPECIFIC` and the state monitor will contain the id of the branch.
- If we want information about a branch in pre-contingency state, the contingencyContext will contain a null
  contingencyId, contextType `NONE` and the state monitor will contain the id of the branch.
- If we want information about a branch in pre-contingency state and after security analysis on contingency `c1`, the
  contingencyContext will contain contingencyId `c1`, contextType `ALL` and the state monitor will contain the id of the
  branch.

### Limit reductions

Limit reductions can be specified in order to detect when a specific limit is **nearly** reached, without having to
artificially modify the limit itself.
For instance, with a limit reduction set to 95% for a limit of 1000 MW, the security analysis will flag a limit
violation for any value exceeding 950 MW.

Each limit reduction has its own criteria specifying for which limits and under what conditions it should be applied.
These criteria can include:

- the type of limit (current, active power or apparent power);
- the use case (for monitoring only or also for applying remedial actions);
- the contingency context (pre-contingency, after a specific contingency or after all contingencies, etc.);
- the network elements targeted by the reduction (by ids, countries and/or nominal voltages);
- which operational limits are affected by the reduction (permanent or temporary + acceptable duration).

You can find more details about limit reductions [here](./limit-reductions).

## Outputs

### Pre-contingency results

The violations are detected on the network at state N, meaning before a contingency occurred. This determines a
reference for the simulation. For each violation, we get the ID of the overloaded equipment, the limit
type (`CURRENT`, `ACTIVE_POWER`, `APPARENT_POWER`, `LOW_VOLTAGE` or `HIGH_VOLTAGE`, `LOW_VOLTAGE_ANGLE`
or `HIGH_VOLTAGE_ANGLE`), the acceptable value and the computed value. For branches and three-winding transformers, we
also have the side where the violation has been detected.

The pre-contingency results also contain the network results based on given state monitors. A network result groups
branch results, bus results and three-winding transformer results. All elementary results are fully extendable.

### Post-contingency results

The post-contingency results contain the complete list of the contingencies that have been simulated, and for each of
them the violations detected. To limit information to the user, only new violations or worsened violations can be
listed.

The post-contingency results also contain the network results based on given state monitors.

### Operator strategy results

The post-contingency results contain the complete list of the contingencies that have been simulated, and for each of
them the violations detected in order to check if remedial actions were efficient.

The operator strategy results also contain the network results based on given state monitors.

### Extensions

The results of a security analysis are extendable, meaning you can have additional information attached to the network,
the contingencies or the violations.

### Example

The following example is a result of a security analysis with remedial action, exported in JSON:

```json
{
  "version" : "1.4",
  "network" : {
    "id" : "sim1",
    "sourceFormat" : "test",
    "caseDate" : "2018-01-01T11:00:00.000+01:00",
    "forecastDistance" : 0
  },
  "preContingencyResult" : {
    "status" : "CONVERGED",
    "limitViolationsResult" : {
      "limitViolations" : [ {
        "subjectId" : "NHV1_NHV2_1",
        "limitType" : "CURRENT",
        "limit" : 100.0,
        "limitReduction" : 0.95,
        "value" : 110.0,
        "side" : "ONE",
        "extensions" : {
          "ActivePower" : {
            "value" : 220.0
          }
        }
      } ],
      "actionsTaken" : [ ]
    },
    "networkResult" : {
      "branchResults" : [ {
        "branchId" : "branch1",
        "p1" : 1.0,
        "q1" : 2.0,
        "i1" : 3.0,
        "p2" : 1.1,
        "q2" : 2.2,
        "i2" : 3.3
      }, {
        "branchId" : "branch2",
        "p1" : 0.0,
        "q1" : 0.0,
        "i1" : 0.0,
        "p2" : 0.0,
        "q2" : 0.0,
        "i2" : 0.0,
        "flowTransfer" : 10.0
      } ],
      "busResults" : [ {
        "voltageLevelId" : "voltageLevelId",
        "busId" : "busId",
        "v" : 400.0,
        "angle" : 3.14
      } ],
      "threeWindingsTransformerResults" : [ {
        "threeWindingsTransformerId" : "threeWindingsTransformerId",
        "p1" : 1.0,
        "q1" : 2.0,
        "i1" : 3.0,
        "p2" : 1.1,
        "q2" : 2.1,
        "i2" : 3.1,
        "p3" : 1.2,
        "q3" : 2.2,
        "i3" : 3.2
      } ]
    }
  },
  "postContingencyResults" : [ {
    "contingency" : {
      "id" : "contingency",
      "elements" : [ {
        "id" : "NHV1_NHV2_2",
        "type" : "BRANCH",
        "voltageLevelId" : "VLNHV1"
      }, {
        "id" : "NHV1_NHV2_1",
        "type" : "BRANCH"
      }, {
        "id" : "GEN",
        "type" : "GENERATOR"
      }, {
        "id" : "BBS1",
        "type" : "BUSBAR_SECTION"
      } ]
    },
    "status" : "CONVERGED",
    "limitViolationsResult" : {
      "limitViolations" : [ {
        "subjectId" : "NHV1_NHV2_2",
        "limitType" : "CURRENT",
        "limitName" : "20'",
        "acceptableDuration" : 1200,
        "limit" : 100.0,
        "limitReduction" : 1.0,
        "value" : 110.0,
        "side" : "TWO",
        "extensions" : {
          "ActivePower" : {
            "preContingencyValue" : 220.0,
            "postContingencyValue" : 230.0
          },
          "Current" : {
            "preContingencyValue" : 95.0
          }
        }
      }, {
        "subjectId" : "GEN",
        "limitType" : "HIGH_VOLTAGE",
        "limit" : 100.0,
        "limitReduction" : 0.9,
        "value" : 110.0
      }, {
        "subjectId" : "GEN2",
        "limitType" : "LOW_VOLTAGE",
        "limit" : 100.0,
        "limitReduction" : 0.7,
        "value" : 115.0,
        "extensions" : {
          "Voltage" : {
            "preContingencyValue" : 400.0
          }
        }
      }, {
        "subjectId" : "NHV1_NHV2_2",
        "limitType" : "ACTIVE_POWER",
        "limitName" : "20'",
        "acceptableDuration" : 1200,
        "limit" : 100.0,
        "limitReduction" : 1.0,
        "value" : 110.0,
        "side" : "ONE"
      }, {
        "subjectId" : "NHV1_NHV2_2",
        "limitType" : "APPARENT_POWER",
        "limitName" : "20'",
        "acceptableDuration" : 1200,
        "limit" : 100.0,
        "limitReduction" : 1.0,
        "value" : 110.0,
        "side" : "TWO"
      } ],
      "actionsTaken" : [ "action1", "action2" ]
    },
    "networkResult" : {
      "branchResults" : [ ],
      "busResults" : [ ],
      "threeWindingsTransformerResults" : [ ]
    },
    "connectivityResult" : {
      "createdSynchronousComponentCount" : 0,
      "createdConnectedComponentCount" : 0,
      "disconnectedLoadActivePower" : 0.0,
      "disconnectedGenerationActivePower" : 0.0,
      "disconnectedElements" : [ ]
    }
  } ],
  "operatorStrategyResults" : [ {
    "operatorStrategy" : {
      "id" : "strategyId",
      "contingencyContextType" : "SPECIFIC",
      "contingencyId" : "contingency1",
      "condition" : {
        "type" : "AT_LEAST_ONE_VIOLATION",
        "violationIds" : [ "violationId1" ]
      },
      "actionIds" : [ "actionId1" ]
    },
    "status" : "CONVERGED",
    "limitViolationsResult" : {
      "limitViolations" : [ ],
      "actionsTaken" : [ ]
    },
    "networkResult" : {
      "branchResults" : [ ],
      "busResults" : [ ],
      "threeWindingsTransformerResults" : [ ]
    }
  } ]
}
```

## Going further

- Different implementations are available to run security analyses on [page](implementations.md).
- [Run a security analysis through an iTools command](../../user/itools/security-analysis.md): Learn how to perform a security analysis from the command line. 
