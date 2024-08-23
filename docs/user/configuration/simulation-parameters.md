---
layout: default
---

# simulation-parameters
The `simulation-parameters` module is used by the [run-impact-analysis]() command or when a dynamic simulation is run. Its properties define a simulation scenario.

## Required properties

**branchSideOneFaultShortCircuitDuration**  
The `branchSideOneFaultShortCircuitDuration` property is a required property that defines the duration of the short
circuit in seconds for the side `ONE` of a branch. 

**branchSideTwoFaultShortCircuitDuration**  
The `branchSideTwoFaultShortCircuitDuration` property is a required property that defines the duration of the short
circuit in seconds for the side `TWO` of a branch.

**faultEventInstant**  
The `faultEventInstant` property is a required property that defines the fault instant event in seconds.

**generatorFaultShortCircuitDuration**  
The `generatorFaultShortCircuitDuration` property is a required property that defines the duration of the short circuit in seconds for a generator.

**preFaultSimulationStopInstant**  
The `preFaultSimulationStopInstant` property is a required property that defines the stop instant event for pre fault
simulations in seconds.

**postFaultSimulationStopInstant**  
The `postFaultSimulationStopInstant` property is a required property that defines the stop instant event for post fault simulations in seconds.

## Optional properties

**branchFaultReactance**  
The `branchFaultReactance` property is an optional property that defines the branch fault reactance. The default
value of this property is `0.01`.

**branchFaultResistance**  
The `branchFaultResistance` property is an optional property that defines the branch fault resistance. The default
value of this property is 0.

**branchShortCircuitDistance**  
The `branchShortCircuitDistance` property is an optional property that defines the location of the short circuit on the branch in %. The
default value of this property is `50`.

**generatorFaultReactance**  
The `generatorFaultReactance` property is an optional property that defines the generator fault reactance. The default
value of this property is `0.00001`.

**generatorFaultResistance**  
The `generatorFaultResistance` property is an optional property that defines the generator fault resistance. The default value of this property is `0.00001`.

## Examples

**YAML configuration:**
```yaml
simulation-parameters:
    preFaultSimulationStopInstant: 0.1
    faultEventInstant: 0.2
    branchSideOneFaultShortCircuitDuration: 0.35
    branchSideTwoFaultShortCircuitDuration: 0.75
    generatorFaultShortCircuitDuration: 0.3
    postFaultSimulationStopInstant: 0.8
```

**XML configuration:**
```xml
<simulation-parameters>
    <preFaultSimulationStopInstant>0.1</preFaultSimulationStopInstant>
    <faultEventInstant>0.2</faultEventInstant>
    <branchSideOneFaultShortCircuitDuration>0.35</branchSideOneFaultShortCircuitDuration>
    <branchSideTwoFaultShortCircuitDuration>0.75</branchSideTwoFaultShortCircuitDuration>
    <generatorFaultShortCircuitDuration>0.3</generatorFaultShortCircuitDuration>
    <postFaultSimulationStopInstant>0.8</postFaultSimulationStopInstant>    
</simulation-parameters>
```
