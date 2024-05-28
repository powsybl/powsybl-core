---
layout: default
latex: true
---

# Dynamic simulation

The dynamic simulation aims at capturing the transient response of the system, and not only to compute the steady state solution.
It may or not involve the activation of events like a line disconnection for example.

* TOC
{:toc}

## Inputs

The inputs of a dynamic simulation are the following:
- a static network
- a set of dynamic models provided by the simulator
- a set of parameters associated to each dynamic model, with carefully chosen values
- a mapping between static components of the network and dynamic models
- optionally, a description of events occurring in the dynamic simulation (disconnection of a line, change of tap for a transformer, etc.)
- a set of parameters for the simulator itself (simulation start and stop time, solver parameters, etc.)
- a configuration file to configure the curves to export at the end of the simulation

### Dynamic models mapping
At the moment, the only way to associate dynamic models to static components is through a groovy script. Note that the syntax of this script is specific to each simulator:
- [Dynawo dynamic model DSL](dynawo/index.md#dynamic-models-dsl)

### Event models mapping
At the moment, the only way to add events to the simulation is through a groovy script. Note that the syntax of this script is specific to each simulator:
- [Dynawo event model DSL](dynawo/index.md#event-models-dsl)

### Curves configuration
At the moment, the only way to monitor dynamic variables of the simulation in order to export curves at the end of the simulation is to provide a groovy script to the simulation. Note that the syntax of this script is specific to each simulator:
- [Dynawo curves DSL](dynawo/index.md#curves-dsl)

## Outputs

The outputs of a dynamic simulation are:
- the updated static network (which may have been topologically modified depending on the events or automatons defined as inputs)
- a zipped file containing the different results of the dynamic simulation:
    - some curves, asked for by the user to track the evolution of specific variables throughout the simulation
    - some aggregated data regarding constraints, like a security analysis output
    - timelines, that contain the list of events that occurred during the dynamic simulation, be them planned beforehand through events, or not
    - logs about the execution of the dynamic simulator

## Implementations

At the moment, the only available implementation of dynamic simulation compatible with PowSyBl is the one provided by [Dynawo](dynawo/index.md).

## Configuration

You first need to choose which implementation to use in your configuration file:
```yaml
dynamic-simulation:
  default-impl-name: "<IMPLEMENTATION_NAME>"
```

Each implementation is identified by its name, that may be unique in the classpath:
- use "dynawo" to use [Dynawo](dynawo/index.md) implementation

## Parameters

Then, configure some generic parameters for all implementations:
```yaml
dynamic-simulation-default-parameters:
    startTime: 0
    stopTime: 1
```

The parameters may also be overridden with a JSON file, in which case the configuration will look like:
```json
{
  "version" : "1.0",
  "startTime" : 0,
  "stopTime" : 1,
  "extensions" : {
    ...
  }
}
```

### Available parameters

**startTime**  
The `startTime` parameter is an optional parameter that defines when the simulation begins, in seconds. By default, it's set to `0s`.

**stopTime**  
The `stopTime` parameter is an optional parameter that defines when the simulation stops, in seconds. By default, it's set to `1s`.

### Default parameters
The default values of all the optional properties are read from the {doc}`parameters` module, defined in the configuration file.

### Specific parameters
Some implementation use specific parameters that can be defined in the configuration file or in the JSON parameters file:
- [Dynawo](dynawo/index.md#specific-parameters)


```{toctree}
---
maxdepth: 2
hidden: true
---
api_guide_dynamic-simulation
configuration.md
parameters.md
itools_dynamic-simulation.md
```