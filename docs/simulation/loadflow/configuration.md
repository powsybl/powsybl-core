# Configuration

```{toctree}
---
hidden: true
maxdepth: 1
---
load-flow-based-phase-shifter-optimizer.md
```


## Implementation
The `load-flow` module is used to configure the load flow default implementation name. Each load flow implementation provides a subclass of `com.powsybl.loadflow.LoadFlowProvider` correctly configured to be found by `java.util.ServiceLoader`.
A load flow provider exposes a name that can be used in the LoadFlow Java API to find a specific load flow implementation.
It can also be used to specify a default implementation in this platform config module. If only one `com.powsybl.loadflow.LoadFlowProvider` is present in the classpath, there is no need to specify a default LoadFlow implementation name. In the case where more
than one `com.powsybl.loadflow.LoadFlowProvider` is present in the classpath, specifying the default implementation name allows LoadFlow API user to use LoadFlow.run(...) and  LoadFlow.runAsync(...) methods to run a load flow. Using these methods when no default load flow name is configured and multiple implementations are in the classpath will throw an exception.
An exception is also thrown if no implementation at all is present in the classpath, or if specifying a load flow name that is not present on the classpath.

If you have several implementations in your classpath, you need to choose which implementation to use in your configuration file:
```yaml
load-flow:
  default-impl-name: "<IMPLEMENTATION_NAME>"
```

**XML configuration:**
```xml
<load-flow>
    <default-impl-name>Mock</default-impl-name>
</load-flow>
```

Each implementation is identified by its name, that should be unique in the classpath:
- use "OpenLoadFlow" to use PowSyBl OpenLoadFlow
- use "DynaFlow" to use DynaFlow implementation

### default-parameters-loader

To define a set of load flow parameters (generic or specific) that should be set for your application before any configuration file is read,
you can use an implementation of `LoadFlowDefaultParametersLoader`.
It uses a JSON parameters file in your java classpath to override default values with whenever a new `LoadFlowParameters` object is created.

If multiple `LoadFlowDefaultParametersLoader` classes are present in your classpath, you should specify which one you want to use using the `default-parameters-loader` parameter of module `load-flow`:

```yaml
load-flow:
  default-parameters-loader: "MyDefaultParameters"
```

## Parameters

### Optional properties

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
    componentMode: MAIN_CONNECTED
    twtSplitShuntAdmittance: false
    dcUseTransformerRatio: true
    dcPowerFactor: 1.0
    hvdcAcEmulation: true
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
  "componentMode": "MAIN_CONNECTED",
  "twtSplitShuntAdmittance": false,
  "dcUseTransformerRatio": true,
  "dcPowerFactor": 1.0,
  "hvdcAcEmulation": true
}
```

**dc**<br>
The `dc` property is an optional property that defines if you want to run an AC power flow (`false`) or a DC power flow (`true`).
The default value is `false`.

**voltageInitMode**<br>
The `voltageInitMode` property is an optional property that defines the policy used by the load flow to initialize the
voltage values. The available values are:
- `UNIFORM_VALUES`: $v = 1 pu$ , $\theta = 0$
- `PREVIOUS_VALUES`: use previous computed value from the network
- `DC_VALUES`: $v = 1 pu$, $\theta$ initialized using a DC load flow

The default value is `UNIFORM_VALUES`.

**distributedSlack**<br>
The `distributedSlack` property is an optional property that defines if the active power mismatch is distributed over the network or not.
The default value is `true`.

**balanceType**<br>
The `balanceType` property is an optional property that defines, if `distributedSlack` parameter is set to true, how to manage the distribution. Several algorithms are supported. All algorithms follow the same scheme: only some elements are participating in the slack distribution, with a given participation factor. Six options are available:
- If using `PROPORTIONAL_TO_GENERATION_P_MAX` then the participating elements are the generators. The participation factor is computed using the maximum active power target $MaxP$ and the active power control droop. The default droop value is `4`. If present, the simulator uses the droop of the generator given by the [active power control extension](../../grid_model/extensions.md#active-power-control).
- If using `PROPORTIONAL_TO_GENERATION_P` then the participating elements are the generators. The participation factor is computed using the active power set point $TargetP$.
- If using `PROPORTIONAL_TO_GENERATION_REMAINING_MARGIN` then the participating elements are the generators. The participation factor is computed using the difference between the active power limit and active power set point $TargetP$. The limit used is $MaxP$ if production needs to increase and $MinP$ if it needs to decrease.
- If using `PROPORTIONAL_TO_GENERATION_PARTICIPATION_FACTOR` then the participating elements are the generators. The simulator uses the participation factors of the generators given by the [active power control extension](../../grid_model/extensions.md#active-power-control).
- If using `PROPORTIONAL_TO_LOAD` then the participating elements are the loads. The participation factor is computed using the active power $P0$.
- If using `PROPORTIONAL_TO_CONFORM_LOAD` then the participating elements are the loads which have a conform active power part. The participation factor is computed using the [load detail extension](../../grid_model/extensions.md#load-detail), which specifies the variable and the fixed parts of $P0$. The slack is distributed only on loads that have a variable part. If the extension is not available on a load, the whole $P0$ is considered as a variable.

Some algorithms may not be supported by all LoadFlow providers or all simulation types. If you plan to use it, check the documentation of your LoadFlow provider. In the case of OpenLoadFlow there are limitations for sensitivity analysis, see [OpenLoadFlow sensitivity analysis documentation](inv:powsyblopenloadflow:*:*#sensitivity/sensitivity).

This default value is `PROPORTIONAL_TO_GENERATION_P_MAX`.


**countriesToBalance**<br>
The `countriesToBalance` property is an optional property that defines the list of [ISO-3166](https://en.wikipedia.org/wiki/ISO_3166-1)
country which participating elements are used for slack distribution. If the slack is distributed but this parameter is not set, the slack distribution is performed over all countries present in the network.

**readSlackBus**<br>
The `readSlackBus` is an optional property that defines if the slack bus has to be selected in the network through the [slack terminal extension](../../grid_model/extensions.md#slack-terminal).
The default value is `true`.

**writeSlackBus**<br>
The `writeSlackBus` is an optional property that says if the slack bus has to be written in the network using the [slack terminal extension](../../grid_model/extensions.md#slack-terminal) after a load flow computation.
The default value is `true`.

**useReactiveLimits**<br>  
The `useReactiveLimits` property is an optional property that defines whether the load flow should take into account equipment's reactive limits. Applies to generators, batteries, static VAR compensators, boundary lines, and HVDC VSCs.  
The default value is `true`.

**phaseShifterRegulationOn**<br>
The `phaseShifterRegulationOn` property is an optional property that defines whether phase shifter regulating controls should be simulated in the load flow.
The default value is `false`.

**transformerVoltageControlOn**<br>
The `transformerVoltageControlOn` property is an optional property that defines whether transformer voltage regulating controls should be simulated in the load flow.
The default value is `false`.

**shuntCompensatorVoltageControlOn**<br>
The `shuntCompensatorVoltageControlOn` property is an optional property that defines whether shunt compensator voltage regulating controls should be simulated in the load flow.
The default value is `false`.

**componentMode**<br>
The `componentMode` property is an optional property that defines 3 possibles modes to run power flow. These modes can be :
- `ALL_CONNECTED`: the power flow is computed over all synchronous components of all connected components
- `MAIN_CONNECTED` : the power flow is computed over all synchronous components of the main (largest) connected component
- `MAIN_SYNCHRONOUS` : the power flow is computed on the main (largest) synchronous component
The default value is `MAIN_CONNECTED`.
-
**twtSplitShuntAdmittance**<br>
The `twtSplitShuntAdmittance` property is an optional property that defines whether the shunt admittance is split at each side of the series impedance for transformers.
The default value is `false`.

**dcUseTransformerRatio**<br>
The `dcUseTransformerRatio` property is an optional property that defines if the ratio of transformers should be used in
the flow equations in a DC power flow.
The default value of this parameter is `true`.

**dcPowerFactor**<br>
The `dcPowerFactor` property is an optional property that defines the power factor used to convert current limits into active power limits in DC calculations.
The default value is `1.0`.

**hvdcAcEmulation**<br>
The `hvdcAcEmulation` property is an optional property that defines whether AC emulation for HVDC should be simulated in the load flow or not (HVDC that are in AC emulation mode should have the hvdc-angle-droop-active-power-control extension).
The default value is `true`.

### Specific parameters
Some implementations use specific parameters that can be defined in the configuration file or in the JSON parameters file:
- [PowSyBl OpenLoadFlow](inv:powsyblopenloadflow:*:*#loadflow/parameters)
- [DynaFlow](inv:powsybldynawo:*:*#load_flow/configuration)
