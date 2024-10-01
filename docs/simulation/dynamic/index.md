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
- a mapping between static components of the network and dynamic models
- optionally, a description of events occurring in the dynamic simulation (disconnection of a line, change of tap for a transformer, etc.)
- a set of parameters for the simulator itself (simulation start and stop time, solver parameters, etc.)
- a configuration file to configure the output variables to export at the end of the simulation

(dynamic-models-mapping)=
### Dynamic models mapping
For the moment, the only way to associate dynamic models to static components is through a groovy script. Note that the syntax of this script is specific to each simulator:
- [Dynawo dynamic model DSL](inv:powsybldynawo:*:*#dynamic_simulation/dynamic-models-dsl)

(event-models-mapping)=
### Event models mapping
For the moment, the only way to add events to the simulation is through a groovy script. Note that the syntax of this script is specific to each simulator:
- [Dynawo event model DSL](inv:powsybldynawo:*:*#dynamic_simulation/event-models-dsl)

(output-variables-configuration)=
### Output variables configuration
For the moment, the only way to add output variables configuration is to provide a groovy script to the simulation. Note that the syntax of this script is specific to each simulator:
- [Dynawo output variables DSL](inv:powsybldynawo:*:*#dynamic_simulation/output-variables-dsl)

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
- [Run a dynamic simulation through an iTools command](../../user/itools/dynamic-simulation.md): Learn how to perform a dynamic simulation from the command line