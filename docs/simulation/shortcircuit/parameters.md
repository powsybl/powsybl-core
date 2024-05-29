# Parameters

## Available parameters
The parameters to be used for the short-circuit calculation should be defined in the config.yml file. For example, here are some valid short-circuit parameters:

```yaml
short-circuit-parameters:
  with-voltage-result: false
  with-feeder-result: true
  with-limit-violations: true
  study-type: TRANSIENT
  with-fortescue-result: false
  min-voltage-drop-proportional-threshold: 20
  with-loads: true
  with-shunt-compensators: true
  with-vsc-converter-stations: false
  with-neutral-position: true
  initial-voltage-profile-mode: CONFIGURED
  voltage-ranges: /path/to/voltage/ranges/file
```

Available parameters in the short-circuit API are stored in `com.powsybl.shortcircuit.ShortCircuitParameters`. They are all optional.

**with-limit-violations**

This property indicates whether limit violations should be returned after the computation. The violations that should be used are `LOW_SHORT_CIRCUIT_CURRENT` and `HIGH_SHORT_CIRCUIT_CURRENT`.
It can be used to filter results where the computed short-circuit current is too high or too low. The default value is `true`.

**with-fortescue-result**

This property indicates if the computed results, like currents and voltages, should be returned only in three-phased magnitude or detailed with magnitude and angle on each phase.
According to this property, different classes to return results can be used. If it is set to false, the classes `MagnitudeFaultResult`, `MagnitudeFeederResult` and `MagnitudeShortCircuitBusResult` should be used.
If the property is true, the classes `FortescueFaultResult`, `FortescueFeederResult` and `FortescueShortCircuitBusResult` should be used. All these classes are in `com.powsybl.shortcircuit`.
The default value is `true`.

**with-feeder-result**

This property indicates if the contributions of each feeder to the short-circuit current at the fault should be computed.
If the property is set to true, the results can be stored in class `com.powsybl.shortcircuit.FeederResult`.
The default value is `true`.

**study-type**

This property indicates the type of short-circuit study. It can be:
- `SUB_TRANSIENT`: it is the first stage of the short circuit, right when the fault happens. In this case, it is the sub-transient reactance of generators that is used.
  This reactance can either be stored in the network or calculated from the transient reactance of generators with a coefficient defined by the parameter `sub-transient-coefficient`.
- `TRANSIENT`: the second stage of the short circuit, before the system stabilizes. The transient reactance of generators will be used.
- `STEADY_STATE`: the last stage, once all transient effects are gone.

The default value is `TRANSIENT`. The transient and sub-transient reactance of the generators are stored in the [short-circuit generator extension.](../../grid/model/extensions.md#generator-short-circuit)

**sub-transient-coefficient**

This property allows to define an optional coefficient, in case of a sub-transient study, to apply to the transient reactance of generators to get the sub-transient one:

$$X''_d = c \times X'_d$$

with:

- $$X''_d$$: the sub-transient reactance
- $$c$$: the sub-transient coefficient defined in this property
- $$X'_d$$: the transient reactance

By default, the value of the coefficient is 0.7, and it should not be higher than 1.

**with-voltage-result**

This property indicates if the voltage profile should be computed on every node of the network. The results, if this property is `true`, should be stored in class `com.powsybl.shortcircuit.ShortCircuitBusResult`. The default value is `true`.

**min-voltage-drop-proportional-threshold**

This property indicates a threshold to filter the voltage results. Thus, it only makes sense if `with-voltage-result` is set to true.
Only the nodes where the voltage drop due to the short circuit in absolute value is above this property are kept.
The voltage drop is calculated as the ratio between the initial voltage magnitude on the node and the voltage magnitude on the node after the fault. The default value is `0`.

**with-loads**

This property indicates whether loads should be taken into account during the calculation. If `true`, the loads are modelled as an equivalent reactance and the equivalent reactance at the fault point will be lowered. If `false`, then they will be ignored.

**with-shunt-compensators**

This property indicates if shunt compensators should be taken into account during the computation. If `true`, the shunt compensators will be modelled as an equivalent reactance.
If `false`, then the shunt compensators will be ignored.

**with-vsc-converter-stations**

This property is a boolean property that indicates whether the VSC converter stations should be included in the calculation.
If `true`, the VSC converter stations will be modeled as an equivalent reactance. If `false`, they will be ignored.

**with-neutral-position**

This property indicates which position of the tap changer of transformers should be used for the calculation. If `true`, the neutral step of the tap changer
is used. The neutral step is the one for which $$\rho = 1$$ and $$\alpha = 0$$. If `false`, then the step that is in the model will be used.
By default, this property is set to false.
For more information about tap changers, see [the documentation about it](../../grid/model/index.md#phase-tap-changer).

**initial-voltage-profile-mode**

This property defines the voltage profile that should be used for the calculation. Three options are available:
- `NOMINAL`: the nominal voltage profile is used for the calculation
- `PREVIOUS`: the voltage profile from the load flow is used for the calculation
- `CONFIGURED`: the voltage profile is specified by the user
  In the case of `CONFIGURED` voltage profile, ranges of nominal voltages with multiplicative coefficients must be specified in the `voltage-ranges` property.
  By default, the initial voltage profile mode is set to `NOMINAL`.

**voltage-ranges**

This property specifies a path to a JSON file containing the voltage ranges and associated coefficients to be used when `initial-voltage-profile-mode` is set to `CONFIGURED`.
The JSON file must contain a list of voltage ranges and coefficients. Then, for each nominal voltage in the network that belongs to the range, the given coefficient is applied to calculate the voltage to be used
in the calculation. All the coefficients should be between 0.8 and 1.2.
Here is an example of this JSON file:
````json
[
    {
      "minimumNominalVoltage": 350.0, 
      "maximumNominalVoltage": 400.0,
      "voltageRangeCoefficient": 1.1
    },
    {
      "minimumNominalVoltage": 215.0,
      "maximumNominalVoltage": 235.0,
      "voltageRangeCoefficient": 1.2
    },
    {
      "minimumNominalVoltage": 80.0,
      "maximumNominalVoltage": 150.0,
      "voltageRangeCoefficient": 1.05
    }
]
````


## FaultParameters

It is possible to override parameters for each fault by creating an instance of `com.powsybl.shortcircuit.FaultParameters`. This object will take the fault to which it applies and all the parameters
for this specific fault. One `FaultParameters` corresponds to one `Fault`.
A list of `FaultParameters` can be given as an input to the API with specific parameters for one or multiple faults. If a fault has no `FaultParameters` corresponding, then the general parameters will be used.
