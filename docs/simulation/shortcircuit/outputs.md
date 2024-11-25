# Outputs
The results of the short-circuit analysis are stored in `com.powsybl.shortcircuit.ShortCircuitAnalysisResult`.
This class gathers the results for every fault, they are accessible either by the ID of the fault or the ID of the 
element on which the fault is simulated.
For each fault, an object `com.powsybl.shortcircuit.FaultResult` is returned.

Depending on `with-fortescue-result`, the returned result should either be an instance of 
`com.powsybl.shortcircuit.MagnitudeFaultResult` or `com.powsybl.shortcircuit.FortescueFaultResult`.

Both classes contain the following attributes:

| Attribute              | Type                        | Unit | Required | Default value | Description                                                                                                |
|------------------------|-----------------------------|------|----------|---------------|------------------------------------------------------------------------------------------------------------|
| fault                  | Fault                       | -    | yes      | -             | The fault that was simulated                                                                               |
| status                 | Status                      | -    | yes      | -             | The status of the computation, see below for more details                                                  |
| shortCircuitPower      | double                      | MVA  | yes      | -             | The value of the short-circuit power                                                                       |
| timeConstant           | Duration                    | -    | yes      | -             | The duration before reaching the permanent short-circuit current                                           |
| feederResults          | List<FeederResult>          | -    | no       | Empty list    | A list of FeederResult, should not be empty if the parameter `with-feeder-result` is set to `true`.        |
| limitViolations        | List<LimitViolation>        | -    | no       | Empty list    | A list of LimitViolation, should be empty if the parameter `with-limit-violations` is set to `false`.      |
| shortCircuitBusResults | List<ShortCircuitBusResult> | -    | no       | Empty list    | A list of ShortCircuitBusResult, should be empty if the parameter `with-voltage-result` is set to `false`. |

However, in these classes, the short-circuit current and voltage are represented differently.

In `MagnitudeFaultResult`, the additional attributes are:

| Attribute | Type   | Unit | Required | Default value | Description                                                      |
|-----------|--------|------|----------|---------------|------------------------------------------------------------------|
| current   | double | A    | yes      | -             | The three-phased magnitude of the computed short-circuit current |
| voltage   | double | kV   | yes      | -             | The three-phased magnitude of the computed short-circuit voltage |


In `FortescueFaultResult`, they are:

| Attribute | Type             | Unit | Required | Default value | Description                                                                                |
|-----------|------------------|------|----------|---------------|--------------------------------------------------------------------------------------------|
| current   | `FortescueValue` | A    | yes      | -             | The magnitude and angle of the computed short-circuit current detailed on the three phases |
| voltage   | `FortescueValue` | kV   | yes      | -             | The magnitude and angle of the computed short-circuit voltage detailed on the three phases |



**The status of the computation**

This status can be:
- `SUCCESS`: the computation went as planned, and the results are full considering the parameters.
- `NO_SHORT_CIRCUIT_DATA`: this status should be returned if no short-circuit data are available in the network, i.e., the sub-transient or transient reactance of generators and the minimum and maximum admissible short-circuit currents.
- `SOLVER_FAILURE`: the computation failed because of an error linked to the solver.
- `FAILURE`: the computation failed for any other reason.

**FeederResults**

This field contains the contributions of each feeder to the short-circuit current as a list. It should only be returned if `with-feeder-result` is set to `true`.
Depending on the value of `with-fortescue-result`, it should be an instance of `com.powsybl.shortcircuit.MagnitudeFeederResult` or `com.powsybl.shortcircuit.FortescueFeederResult`.

The attributes of `MagnitudeFeederResults` are:

| Attribute     | Type       | Unit | Required | Default value | Description                                                                                                |
|---------------|------------|------|----------|---------------|------------------------------------------------------------------------------------------------------------|
| connectableId | String     | -    | yes      | -             | ID of the feeder                                                                                           |
| current       | double     | A    | yes      | -             | Three-phased current magnitude of the feeder participating to the short-circuit current at the fault point | 
| side          | ThreeSides | -    | no       | -             | If the feeder is a branch or a three-winding transformer, the side on which the result is                  | 


The attributes of `FortescueFeederResuts` are:

| Attribute     | Type             | Unit | Required | Default value | Description                                                                                                                   |
|---------------|------------------|------|----------|---------------|-------------------------------------------------------------------------------------------------------------------------------|
| connectableId | String           | -    | yes      | -             | ID of the feeder                                                                                                              |
| current       | `FortescueValue` | A    | yes      | -             | Current magnitudes and angles on the three phases of the feeder participating to the short-circuit current at the fault point |
| side          | ThreeSides       | -    | no       | -             | If the feeder is a branch or a three-winding transformer, the side on which the result is                                     | 

Note: For results on branches, the side can be retrieved as a `TwoSides` object by using the method `getSideAsTwoSides`.  

**LimitViolations**

This field contains a list of all the violations after the fault. This implies to have defined in the network the maximum or the minimum acceptable short-circuit currents on the voltage levels.
Then, with comparing to the computed short-circuit current, two types of violations can be created: `LOW_SHORT_CIRCUIT_CURRENT` and `HIGH_SHORT_CIRCUIT_CURRENT`.
This list should be empty if the property `with-limit-violations` is set to `false`.

**ShortCircuitBusResults**

This field contains a list of voltage results on every bus of the network after simulating the fault. It should be empty if `with-voltage-result` is set to `false`.
The value of the property `with-voltage-drop-threshold` allows to filter these results by keeping only those where the voltage drop is higher than this defined threshold.
Depending on the value of `with-fortescue-result`, the list should contain instances of either `com.powsybl.shortcircuit.MagnitudeShortCircuitBusResult` or `com.powsybl.shortcircuit.FortescueShortCircuitBusResult` objects.


The attributes of `MagnitudeShortCircuitBusResult` are:

| Attribute               | Type   | Unit | Required | Default value | Description                                            |
|-------------------------|--------|------|----------|---------------|--------------------------------------------------------|
| voltageLevelId          | String | -    | yes      | -             | ID of the voltage level containing the bus             |
| busId                   | String | -    | yes      | -             | ID of the bus                                          | 
| initialVoltageMagnitude | double | kV   | yes      | -             | Magnitude of the three-phased voltage before the fault |
| voltageDropProportional | double | %    | yes      | -             | Voltage drop after the fault                           |
| voltage                 | double | kV   | yes      | -             | Magnitude of the three-phased voltage after the fault  |

The attributes of `FortescueShortCircuitBusResult` are:

| Attribute               | Type             | Unit | Required | Default value | Description                                                              |
|-------------------------|------------------|------|----------|---------------|--------------------------------------------------------------------------|
| voltageLevelId          | String           | -    | yes      | -             | ID of the voltage level containing the bus                               |
| busId                   | String           | -    | yes      | -             | ID of the bus                                                            | 
| initialVoltageMagnitude | double           | kV   | yes      | -             | Magnitude of the three-phased voltage before the fault                   |
| voltageDropProportional | double           | %    | yes      | -             | Voltage drop after the fault                                             |
| voltage                 | `FortescueValue` | kV   | yes      | -             | Magnitudes and angles of the voltage on the three phases after the fault |
