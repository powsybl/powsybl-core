# Module simulation-parameters

The `simulation-parameters` module is used in the [run-impact-analysis](../../tools/run-impact-analysis.md) command or when a dynamic simulation is run, its properties define a simulation scenario. 


## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| preFaultSimulationStopInstant | double | si | - | Stop instant event, pre fault simulation (in seconds) |
| faultEventInstant | double | si | - | Fault instant event (in seconds) |
| branchSideOneFaultShortCircuitDuration | double | si | - | Branch side one short circuit fault duration (in seconds)|
| branchSideTwoFaultShortCircuitDuration | double | si | - | Branch side two short circuit fault duration (in seconds) |
| generatorFaultShortCircuitDuration| double | si | - | Duration of genarator short circuit fault (in seconds) |
| postFaultSimulationStopInstant | double | si | - | Stop instant event, post fault simulation (in seconds ) |
| branchShortCircuitDistance | double | no | 50 | branch short circuit distance |
| branchFaultResistance | double | no | 0 | branch fault resistance |
| branchFaultReactance |double | no | 0.01 | branch fault reactance |
| generatorFaultResistance |double | no | 0.00001 | generator  fault resistance  |
| generatorFaultReactance | double | no | 0.00001 | generator fault reactance  |

## Examples

### YAML
```yaml
simulation-parameters:
    preFaultSimulationStopInstant: 0.1
    faultEventInstant: 0.2
    branchSideOneFaultShortCircuitDuration: 0.35
    branchSideTwoFaultShortCircuitDuration: 0.75
    generatorFaultShortCircuitDuration: 0.3
    postFaultSimulationStopInstant: 0.8   
```

### XML
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
