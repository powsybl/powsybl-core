# Configuration

## Implementation
If you have several implementations in your classpath, you need to choose which implementation to use in your configuration file:
```yaml
load-flow:
  default-impl-name: "<IMPLEMENTATION_NAME>"
```

Each implementation is identified by its name, that should be unique in the classpath:
- use "OpenLoadFlow" to use PowSyBl OpenLoadFlow
- use "DynaFlow" to use DynaFlow implementation

## Parameters

(loadflow-generic-parameters)=
### Generic parameters

You may configure some generic parameters for all load flow implementations:
```yaml
load-flow-default-parameters:
    dc: false
    voltageInitMode: UNIFORM_VALUES
    distributedSlack: true
    balanceType: PROPORTIONAL_TO_GENERATION_P_MAX
    countriesToBalance:
      - FR
      - BE
    readSlackBus: false
    writeSlackBus: false
    useReactiveLimits: true
    phaseShifterRegulationOn: false
    transformerVoltageControlOn: false
    shuntCompensatorVoltageControlOn: false
    connectedComponentMode: MAIN
    twtSplitShuntAdmittance: false
    dcUseTransformerRatio: true
    dcPowerFactor: 1.0
```

The parameters may also be overridden with a JSON file, in which case the configuration will look like:
```json
{
  "version": "1.8",
  "dc": false,
  "voltageInitMode": "UNIFORM_VALUES",
  "distributedSlack": true,
  "balanceType": "PROPORTIONAL_TO_GENERATION_P_MAX",
  "countriesToBalance": ["FR", "BE"],
  "readSlackBus": false,
  "writeSlackBus": false,
  "useReactiveLimits": true,
  "phaseShifterRegulationOn": false,
  "transformerVoltageControlOn": false,
  "shuntCompensatorVoltageControlOn": false,
  "connectedComponentMode": "MAIN",
  "twtSplitShuntAdmittance": false,
  "dcUseTransformerRatio": true,
  "dcPowerFactor": 1.0
}
```

**dc**  
The `dc` property is an optional property that defines if you want to run an AC power flow (`false`) or a DC power flow (`true`).  
The default value is `false`.

**voltageInitMode**  
The `voltageInitMode` property is an optional property that defines the policy used by the load flow to initialize the
voltage values. The available values are:
- `UNIFORM_VALUES`: $v = 1 pu$ , $\theta = 0$
- `PREVIOUS_VALUES`: use previous computed value from the network
- `DC_VALUES`: $v = 1 pu$, $\theta$ initialized using a DC load flow

The default value is `UNIFORM_VALUES`.

**distributedSlack**  
The `distributedSlack` property is an optional property that defines if the active power mismatch is distributed over the network or not.  
The default value is `true`.

**balanceType**  
The `balanceType` property is an optional property that defines, if `distributedSlack` parameter is set to true, how to manage the distribution. Several algorithms are supported. All algorithms follow the same scheme: only some elements are participating in the slack distribution, with a given participation factor. Three options are available:
- If using `PROPORTIONAL_TO_GENERATION_P_MAX` then the participating elements are the generators. The participation factor is computed using the maximum active power target $MaxP$ and the active power control droop. The default droop value is `4`. If present, the simulator uses the droop of the generator given by the [active power control extension](../../grid_model/extensions.md#active-power-control).
- If using `PROPORTIONAL_TO_GENERATION_P` then the participating elements are the generators. The participation factor is computed using the active power set point $TargetP$.
- If using `PROPORTIONAL_TO_GENERATION_REMAINING_MARGIN` then the participating elements are the generators. The participation factor is computed using the difference between the active power limit with active power set point $TargetP$. The limit used is $MaxP$ if production needs to increase and $MinP$ if it needs to decrease.
- If using `PROPORTIONAL_TO_GENERATION_PARTICIPATION_FACTOR` then the participating elements are the generators. The simulator uses the participation factors of the generators given by the [active power control extension](../../grid_model/extensions.md#active-power-control).
- If using `PROPORTIONAL_TO_LOAD` then the participating elements are the loads. The participation factor is computed using the active power $P0$.
- If using `PROPORTIONAL_TO_CONFORM_LOAD` then the participating elements are the loads which have a conform active power part. The participation factor is computed using the [load detail extension](../../grid_model/extensions.md#load-detail), which specifies the variable and the fixed parts of $P0$. The slack is distributed only on loads that have a variable part. If the extension is not available on a load, the whole $P0$ is considered as a variable.

This default value is `PROPORTIONAL_TO_GENERATION_P_MAX`.

**countriesToBalance**  
The `countriesToBalance` property is an optional property that defines the list of [ISO-3166](https://en.wikipedia.org/wiki/ISO_3166-1)
country which participating elements are used for slack distribution. If the slack is distributed but this parameter is not set, the slack distribution is performed over all countries present in the network.

**readSlackBus**  
The `readSlackBus` is an optional property that defines if the slack bus has to be selected in the network through the [slack terminal extension](../../grid_model/extensions.md#slack-terminal).  
The default value is `false`.

**writeSlackBus**   
The `writeSlackBus` is an optional property that says if the slack bus has to be written in the network using the [slack terminal extension](../../grid_model/extensions.md#slack-terminal) after a load flow computation.  
The default value is `false`.

**useReactiveLimits**  
The `useReactiveLimits` property is an optional property that defines whether the load flow should take into account equipment's reactive limits. Applies to generators, batteries, static VAR compensators, dangling lines, and HVDC VSCs.  
The default value is `true`.

**phaseShifterRegulationOn**  
The `phaseShifterRegulationOn` property is an optional property that defines whether phase shifter regulating controls should be simulated in the load flow.  
The default value is `false`.

**transformerVoltageControlOn**  
The `transformerVoltageControlOn` property is an optional property that defines whether transformer voltage regulating controls should be simulated in the load flow.  
The default value is `false`.

**shuntCompensatorVoltageControlOn**  
The `shuntCompensatorVoltageControlOn` property is an optional property that defines whether shunt compensator voltage regulating controls should be simulated in the load flow.  
The default value is `false`.

**connectedComponentMode**  
The `connectedComponentMode` property is an optional property that defines if the power flow has to be computed over all connected components (choose `ALL` mode) or just on the main connected component (choose `MAIN` mode).  
The default value of this parameter is `MAIN`.

**twtSplitShuntAdmittance**  
The `twtSplitShuntAdmittance` property is an optional property that defines whether the shunt admittance is split at each side of the series impedance for transformers.  
The default value is `false`.

**dcUseTransformerRatio**  
The `dcUseTransformerRatio` property is an optional property that defines if the ratio of transformers should be used in 
the flow equations in a DC power flow.  
The default value of this parameter is `true`.

**dcPowerFactor**  
The `dcPowerFactor` property is an optional property that defines the power factor used to convert current limits into active power limits in DC calculations.  
The default value is `1.0`.

### Specific parameters
Some implementations use specific parameters that can be defined in the configuration file or in the JSON parameters file:
- [PowSyBl OpenLoadFlow](inv:powsyblopenloadflow:*:*#loadflow/parameters)
- [DynaFlow](inv:powsybldynawo:*:*#load_flow/configuration)