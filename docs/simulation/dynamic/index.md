# Dynamic simulation

The dynamic simulation aims at capturing the transient response of the system, and not only to compute the steady state solution.
It may or not involve the activation of events like a line disconnection for example.

```{toctree}
---
hidden: true
maxdepth: 1
---
configuration.md
```

## Inputs

The inputs of a dynamic simulation are the following:
- a static network
- a set of dynamic models provided by the simulator
- a set of parameters associated to each dynamic model, with carefully chosen values
- optionally, a description of events occurring in the dynamic simulation (disconnection of a line, change of tap for a transformer, etc.)
- a set of parameters for the simulator itself (simulation start and stop time, solver parameters, etc.)
- a configuration file to configure the output variables to export at the end of the simulation

(dynamic-models-configuration)=
### Dynamic models configuration
The dynamic models may be provided through a groovy script thanks to the `GroovyDynamicModelsSupplier` provided in powsybl-dynamic-simulation-dsl artifact. Note that the syntax of this groovy script is specific to each simulator.
See [Dynawo dynamic model configuration](inv:powsybldynawo:*:*#dynamic_simulation/dynamic-models-configuration) for Dynawo specific DSL and others configuration methods. 

(event-models-configuration)=
### Event models configuration
The event models may be provided through a groovy script thanks to the `GroovyEventModelsSupplier` provided in powsybl-dynamic-simulation-dsl artifact. Note that the syntax of this groovy script is specific to each simulator.
See [Dynawo event model configuration](inv:powsybldynawo:*:*#dynamic_simulation/event-models-configuration) for Dynawo specific DSL and others configuration methods.

(output-variables-configuration)=
### Output variables configuration
The output variables configuration may be provided through a groovy script thanks to the `GroovyOutputVariablesSupplier` provided in powsybl-dynamic-simulation-dsl artifact. Note that the syntax of this groovy script is specific to each simulator.
See [Dynawo output variables configuration](inv:powsybldynawo:*:*#dynamic_simulation/output-variables-configuration) for Dynawo specific DSL and others configuration methods.

## Outputs

The outputs of a dynamic simulation are:
- the updated static network (which may have been topologically modified depending on the events or automatons defined as inputs)
- the different results of the dynamic simulation:
    - some curves or final state values asked for by the user to track the evolution of specific variables throughout the simulation
    - some aggregated data regarding constraints, like a security analysis output
    - timelines that contain the list of events that occurred during the dynamic simulation, be them planned beforehand through events, or not
    - logs about the execution of the dynamic simulator

## Implementations

For the moment, the only available implementation is provided by powsybl-dynawo, which links PowSyBl with [Dynaωo](http://dynawo.org) open source suite.

## Going further
- [Run a dynamic simulation through an iTools command](../../user/itools/dynamic-simulation.md): learn how to perform a dynamic simulation from the command line
- [List dynamic simulation models with an iTools command](../../user/itools/list-dynamic-simulation-models.md): learn how to load a list of all dynamic simulation models from the command line
