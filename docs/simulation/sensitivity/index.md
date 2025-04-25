# Sensitivity analysis

```{toctree}
---
hidden: true
maxdepth: 1
---
configuration.md
```

## Introduction

The sensitivity analysis module is dedicated to computing the linearized impact of small network variations on the state variables of some equipments.

A sensitivity value is the numerical estimation of the partial derivative of the observed function with respect to the variable of impact.
The sensitivity analysis can also be seen as the computation of partial derivatives on the network model.
For example, it may be used to know among a group of selected lines, which are the most impacted by a change in a generator production or a change of tap on a phase tap changer. The user story about [RSC capacity calculation](https://www.powsybl.org/pages/documentation/user/user-stories/capacity_calculation_rsc.html) provides an example of application of the sensitivity analysis.

(sensitivity-analysis-inputs)=
## Inputs

### Network
The first input for the sensitivity analysis module is an IIDM network.

(sensitivity-factors)=
### Sensitivity factors
Aside from providing an input network, it is necessary to specify which equipments are going to be studied:
- what impacted equipment is selected to be monitored (lines, for example)
- according to a change on which equipment (a generator's production or a group of generator's production, or the tap position of a phase tap changer, etc.)

It is also necessary to specify which quantity is being observed: the active power or the current on the monitored equipment, the voltage of a bus.

This set of information constitutes the sensitivity factors (`SensitivityFactor`). These factors correspond to the definition
of the expected partial derivatives to be extracted from the input network.
A standard sensitivity analysis input thus comprises a list of sensitivity factors, each one constituted of:
- a sensitivity variable (the variable of impact) which type is defined by a `SensitivityVariableType`.
- a sensitivity function (the observed function) which type is defined by a `SensitivityFunctionType`.
- a contingency context.
  Usually we compute the impact of an injection increase on a branch flow or current, the impact of a shift of a phase tap changer on a branch flow or current or the impact of a voltage target increase on a bus voltage.

A sensitivity variable represents a change on an equipment or on a group of equipments. The supported variable types are:
- Use `INJECTION_ACTIVE_POWER` to model a change on active production of a generator or on a group of generators, on the active consumption of a load or on a group of loads or on GLSK (for Generation and Load Shift keys) that describes a linear combination of active power injection shifts on generators and loads. The variable increase is in MW.
- Use `INJECTION_REACTIVE_POWER` to model a change on reactive production of a generator or on the reactive consumption of a load. The variable increase is in MVar.
- Use `TRANSFORMER_PHASE` to model the change of the tap position of a phase tap changer of a two-winding transformer. The increase is in degree.
- Use `BUS_TARGET_VOLTAGE` to model an increase of the voltage target of a generator, a static var compensator, a two or three-winding transformer, a shunt compensator or a VSC converter station. The increase is in KV.
- Use `HVDC_LINE_ACTIVE_POWER` to model the change of the active power set point of an HVDC line. The increase is in MW.
- Use `TRANSFORMER_PHASE_1`, `TRANSFORMER_PHASE_2` or `TRANSFORMER_PHASE_3` to model the change of the tap position of a phase tap changer of a three-winding transformer that contains several phase tap changers.

The supported sensitivity function types, related to the equipment to monitor, are:
- Use `BRANCH_ACTIVE_POWER_1` and `BRANCH_ACTIVE_POWER_2` if you want to monitor the active power in MW of a network branch (lines, two-winding transformer, dangling lines, etc.). Use 1 for side 1 and 2 for side 2. In case of a three-winding transformer, use `BRANCH_ACTIVE_POWER_3` to monitor the active power in MW of leg 3 (network side).
- Use `BRANCH_REACTIVE_POWER_1` and `BRANCH_REACTIVE_POWER_2` if you want to monitor the reactive power in MVar of a network branch (lines, two-winding transformer, dangling lines, etc.). Use 1 for side 1 and 2 for side 2. In case of a three-winding transformer, use `BRANCH_REACTIVE_POWER_3` to monitor the reactive power in MVar of leg 3 (network side).
- Use `BRANCH_CURRENT_1` and `BRANCH_CURRENT_2` if you want to monitor the current in A of a network branch (lines, two-winding transformer, dangling lines, etc.). Use 1 for side 1 and use 2 for side 2. In case of a three-winding transformer, use `BRANCH_CURRENT_3` to monitor the current in A of leg 3 (network side).
- `BUS_VOLTAGE` if you want to monitor the voltage in KV of a specific network bus.
- `BUS_REACTIVE_POWER` if you want to monitor the reactive power injection in MVar of a specific network bus.

A sensitivity variable can group some equipment and has to be modeled as a variable set. In a `SensitivityVariableSet`, we have a list of individual variables, each one with a weight (called `WeightedSensitivityVariable`). We use variable sets to model what it commonly called GLSK.

#### How to provide the sensitivity factors input

The sensitivity factors may be created directly through Java code, or be provided to PowSyBl via a JSON file. This file should contain a list of JSON objects, each one representing a sensitivity factor. The example below shows how to write a JSON file to perform a sensitivity analysis on the active power through a line, with respect to a GLSK and to an injection on the network.

```json
[ {
  "functionType" : "BRANCH_ACTIVE_POWER_1",
  "functionId" : "l45",
  "variableType" : "INJECTION_ACTIVE_POWER",
  "variableId" : "glsk",
  "variableSet" : true,
  "contingencyContextType" : "ALL"
}, {
  "functionType" : "BRANCH_ACTIVE_POWER_1",
  "functionId" : "l12",
  "variableType" : "INJECTION_ACTIVE_POWER",
  "variableId" : "g2",
  "variableSet" : false,
  "contingencyContextType" : "ALL"
} ]
```

### Contingencies
The sensitivity analysis may also take, optionally, a list of contingencies as an input. When contingencies are provided, the sensitivity values
shall be calculated on the network at state N, but also after the application of each contingency. The contingencies are provided in the same way as for the [security analysis](../security/index.md/). This then constitutes a systematic sensitivity analysis.

```json
{
  "type" : "default",
  "version" : "1.0",
  "name" : "default",
  "contingencies" : [ {
    "id" : "l34",
    "elements" : [ {
      "id" : "l34",
      "type" : "BRANCH"
    } ]
  } ]
}
```

At the moment, the only available sensitivity simulator officially compatible with PowSyBl is the one available through OpenLoadFlow. In this case, the network is provided only once in state N, and then all the calculations are done successively by modifying the Jacobian matrix directly in the solver based on the contingency input. The network is thus loaded only once, which improves performance.

(sensitivity-analysis-outputs)=
## Outputs

(sensitivity-values)=
### Sensitivity values
The outputs of the sensitivity analysis are called sensitivity values. A sensitivity value represents an elementary result given a sensitivity factor and a contingency, and contains:
- The actual value of the partial derivative
- The reference value of the function at linearization point in case of contingency context `NONE` or in its post-contingency state in case of a factor associated to a contingency.

These results may be serialized in JSON format.

### Example of interpretation
Let's imagine that one wants to compute the impact of an increase of active power generation of the
generator G on the branch B. The sensitivity analysis input will contain one sensitivity factor, with sensitivity function type `BRANCH_ACTIVE_POWER_1` and sensitivity variable type `INJECTION_ACTIVE_POWER`, and we do not provide any input contingencies.

After the computation, let us consider that the values of the three elements of the sensitivity result are:
- a value of -0.05 for the partial derivative
- a variable reference value of 150
- a function reference value of 265

This can be interpreted in the following way:
- an increase of 100 MW on generator G may be approximated on branch B as a 5MW decrease of the active flow from side 1 to side 2
- the initial generation on generator G is 150MW
- the initial active flow on branch B is 265MW from side 1 to side 2

## Implementations

The following sensitivity analysis implementations are supported:
- [PowSyBl OpenLoadFlow]()

## Going further

To go further about the sensitivity analysis, check the following content:
- [Sensitivity analysis tutorial](https://github.com/powsybl/powsybl-tutorials/tree/main/sensitivity)