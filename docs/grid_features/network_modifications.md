# Network modifications

The `powsybl-iidm-modification` module gathers classes and methods used to modify the network easily.  
Each modification must first be created with the right attributes or parameters and then applied on the network.  
A `NetworkModification` offers a method to check whether or not its application would have an impact on the given network.

## Scaling
<span style="color: red">TODO</span>

## Topology modifications
<span style="color: red">TODO</span>

## Tripping

### Battery tripping
<span style="color: red">TODO</span>

### Branch tripping
<span style="color: red">TODO</span>

### Busbar section tripping
<span style="color: red">TODO</span>

### Bus tripping
<span style="color: red">TODO</span>

### Dangling line tripping
<span style="color: red">TODO</span>

### Generator tripping
<span style="color: red">TODO</span>

### Hvdc line tripping
<span style="color: red">TODO</span>

### Line tripping
<span style="color: red">TODO</span>

### Load tripping
<span style="color: red">TODO</span>

### Shunt compensator tripping
<span style="color: red">TODO</span>

### Static Var compensator tripping
<span style="color: red">TODO</span>

### Switch tripping
<span style="color: red">TODO</span>

### Three-winding transformer tripping
<span style="color: red">TODO</span>

### Tie line tripping
<span style="color: red">TODO</span>

### two-winding transformer tripping
<span style="color: red">TODO</span>

## Other modifications

### List
This modification is used to apply a list of any Powsybl `NetworkModification`.

Class: `NetworkModificationList`

### Area interchange
This modification is used to update the target of an area interchange.

The target is in MW in load sign convention (negative for export, positive for import).  
Providing `Double.NaN` removes the target.

Class: `AreaInterchangeTargetModification`

### Battery
This modification is used to update the target powers (active `targetP` and reactive `targetV`) of a battery.

Class: `BatteryModification`

### Connection
This modification is used to connect a network element to the closest bus or bus bar section.

It works on:
- `Connectable` elements by connecting their terminals
- HVDC lines, by connecting the terminals of their converter stations
- Tie lines, by connecting the terminals of their underlying dangling lines

It is possible to specify a side of the element to connect. If no side is specified, the network modification will try to connect every side.

Class: `ConnectableConnection`

### Dangling line
This modification is used to update the active and reactive powers of a dangling line.

If `relativeValue` is set to true, then the new constant active power (`P0`) and reactive power (`Q0`) are set as the addition of the given values to the previous ones.  
If `relativeValue` is set to false, then the new constant active power (`P0`) and reactive power (`Q0`) are updated to the new given values.

Class: `DanglingLineModification`

### Disconnections

#### Planned
This modification is used to disconnect a network element from the bus or bus bar section to which it is currently connected. It should be used if the disconnection is planned. If it is not,
`UnplannedDisconnection` should be used instead.

It works on:
- `Connectable` elements.
- HVDC lines, by disconnecting their converter stations
- Tie lines, by disconnecting their underlying dangling lines

It is possible to specify a side of the element to connect. If no side is specified, the network modification will try to connect every side.

Class: `PlannedDisconnection`

#### Unplanned
This modification is used to disconnect a network element from the bus or bus bar section to which it is currently connected. It should be used if the disconnection is unplanned. If it is not,
`PlannedDisconnection` should be used instead.

It works on:
- `Connectable` elements.
- HVDC lines, by disconnecting their converter stations
- Tie lines, by disconnecting their underlying dangling lines

It is possible to specify a side of the element to connect. If no side is specified, the network modification will try to connect every side.

Class: `UnplannedDisconnection`

### Generator

#### Modification
This modification is used to apply a set of modifications on a generator.

The data to be updated are optional among:
- `minP`, the minimum active power boundary in MW.
- `maxP`, the maximum active power boundary in MW.
- `targetV`, the target voltage value in kV.
- `targetQ`, the target reactive power value in MVAR.
- `connected`, the connection state of the generator terminal.
- `voltageRegulatorOn`, to activate or deactivate the generator voltage regulator status. If `true` and the generator target voltage is not set then an acceptable value for the generator `targetV` is computed before activating.
- The active power if `targetP` or `deltaTargetP` are given. An active power is determined by the new `targetP` if given, and if not then the `deltaTargetP` is considered instead and the new value of the generator `targetP` is the addition of the old generator value with the given delta target P value. Then, according to the given `ignoreCorrectiveOperations` parameter:
  - If `ignoreCorrectiveOperations` is true, this determined active power is applied as the new generator target P value.
  - If `ignoreCorrectiveOperations` is false, then the new active power will also depend on the limits and will be the minimum value between the generator `maxP` and the maximum value between the generator `minP` and the previously determined active power value. Besides, if the generator connection state has not been updated before within this `NetworkModification` then the generator is connected if necessary.

Class: `GeneratorModification`

#### Connection
This modification is used to connect a given generator.

If the generator terminal is regulating then it will also set its target voltage if an acceptable value is found.

Class: `ConnectGenerator`

#### Set to local regulation
This modification is used to set the generator regulating terminal to a local regulation.

The target voltage value is set to the same value for all the generators of the bus that are regulating locally.
In case other generators are already regulating locally on the same bus, targetV value is determined by being the closest value to the voltage level nominal voltage among the regulating terminals.
If no other generator is regulating on the same bus, targetV engineering unit value is adapted to the voltage level nominal voltage, but the per unit value remains the same.

Class:`SetGeneratorToLocalRegulation`

### HVDC line
This modification is used to modify a given HVDC line (and potentially its angle droop active power control extension).

- Modify the HVDC line `activePowerSetpoint` if given, relatively to the existent `activePowerSetpoint` if `relativeValue` is true or as a replacement value if not.
- Modify the `convertersMode` with the given one if set
- Modify the angle droop active power control extension (if existing but will not crash if not found for the HVDC line):
  - Enable or disable the AC emulation if `acEmulationEnabled` is provided
  - Update the active power if `p0` is provided
  - Update the droop in MW/degree if `droop` is provided

Class: `HvdcLineModification`

### Load

#### Modification
This modification updates the `P` and `Q` values of the load.

If `relativeValue` is true, then the new constant active power (`P0`) and reactive power (`Q0`) are set as the addition of the given value to the previous ones.  
If `relativeValue` is false, then the new constant active power (`P0`) and reactive power (`Q0`) are updated to the new given value.

Class: `LoadModification`

#### Percent modification
This modification is used to add or remove a percentage of the P and Q of the load. The percentage to add or remove for P and Q cannot be less than -100 (in percentage).

Class: `PercentChangeLoadModification`

### Phase shifters

#### Optimize tap modification
This modification is used to find the optimal phase-tap changer position of a given two-winding transformer phase shifter id.

A phase shifter optimization load flow is run with the configured `load-flow-based-phase-shifter-optimizer` to determine the optimal tap position.

Class: `PhaseShifterOptimizeTap`

#### Fixed tap modification
This modification updates the phase-tap changer of a given two-winding transformer phase shifter id.

It updates its `tapPosition` with the given value and set the phase-tap changer as not regulating with a `FIXED_TAP` regulation mode.

Class: `PhaseShifterSetAsFixedTap`

#### Shift tap modification
This modification is used to update the phase-tap changer of a given two-winding transformer phase shifter id.

It sets the phase-tap changer as not regulating with a `FIXED_TAP` regulation mode and updates its `tapPosition` by adjusting it with the given `tapDelta` applied on the current tap position. The resulting tap position is bounded by the phase-tap changer lowest and highest possible positions.

Class: `PhaseShifterShiftTap`

### Replace tie lines by lines
This modification is used to replace all the tie lines of a network to simple lines built from the original tie line and its 2 dangline lines.

- The two voltage levels are set from the tie line dangling lines terminal voltage levels (the first voltage level from the first dangling line and the second from the second one).
- For each voltage level the topology kind is taken into account to create node (for `NODE_BREAKER` kind) or bus and connectable bus (for `BUS_BREAKER` kind)
- The tie line id, name, r, x, b1, b2, g1, g2 are set in the new line
- Active power limits, apparent power limits and current limits are set on each side of the line from the limits of the 2 dangling lines
- Terminal active and reactive powers are set for both terminals from each dangling line active and reactive powers
- Line properties are set from the merge of the tie line and its 2 dangling lines properties
- Line aliases are set from the merge of the tie line and its 2 dangling lines aliases
- If the tie line has a pairing key then it is added to the new line as a pairing key alias
- The tie line and its dangling lines are removed from the network

Class: `ReplaceTieLinesByLines`

### Shunt compensator
This modification is used to (dis)connect a shunt compensator and/or change its section count in service.

If the modification connects the shunt compensator and its terminal is regulating then it will also set its target voltage if an acceptable value is found.

Class: `ShuntCompensatorModification`

### Static var compensator
This modification modifies the voltage and reactive power setpoints of a static var compensator, following a load convention.

Class: `StaticVarCompensatorModification`

### Switch
#### Close
This modification is used to close a switch.

Class: `CloseSwitch`

#### Open
This modification is used to open a switch.

Class: `OpenSwitch`

### Transformers

#### Three-winding transformers legs rated voltage
This modification is used to modify the rated voltage of each leg of a three-winding transformer.

On each leg the new rated voltage is computed from the given common rated voltage multiplied by the ratio (leg old rated voltage / rated voltage of the three-winding transformer (the `ratedU0` also used as nominal voltage) at the fictitious bus (in kV)).

Class: `ThreeWindingsTransformerModification`

#### Replace 1 three-winding transformer by 3 two-winding transformers
This modification is used to replace all or a given list of `ThreeWindingsTransformer` by triplets of `TwoWindingsTransformer`.

For each `ThreeWindingsTransformer` to be replaced:
- A new voltage level is created for the star node with nominal voltage of ratedU0.
- Three `TwoWindingsTransformers` are created, one for each leg of the `ThreeWindingsTransformer` to transform.
- The following attributes are copied from each leg to the new associated `TwoWindingsTransformer`:
  - Electrical characteristics, ratio tap changers, and phase-tap changers. No adjustments are required.
  - Operational Loading Limits are copied to the non-star end of the two-winding transformers.
  - Active and reactive powers at the terminal are copied to the non-star terminal of the two-winding transformer.
- Aliases:
  - Aliases for known CGMES identifiers (terminal, transformer end, ratio, and phase-tap changer) are copied to the right `TwoWindingsTransformer` after adjusting the alias type.
  - Aliases that are not mapped are recorded in the functional log.
- Properties:
  - Star bus voltage and angle are set to the bus created for the star node.
  - The names of the operational limits are copied to the right `TwoWindingsTransformer`.
  - The rest of the properties of the `ThreeWindingsTransformer` are transferred to all 3 `TwoWindingsTransformer`.
- Extensions:
  - Only IIDM extensions are copied: `TransformerFortescueData`, `PhaseAngleClock`, and `TransformerToBeEstimated`.
  - CGMES extensions can not be copied, as they cause circular dependencies.
  - Extensions that are not copied are recorded in the functional log.
- All the controllers using any of the `ThreeWindingsTransformer` terminals as regulated terminal are updated.
- New and removed equipment is recorded in the functional log.
- Internal connections are created to manage the replacement.

Class: `ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers`

#### Replace 3 two-winding transformers by 1 three-winding transformer
This modification is used to replace all or a given list of `TwoWindingsTransformer` by `ThreeWindingsTransformer`.

In the list of `TwoWindingsTransformer` if only one of a triplet of `TwoWindingsTransformer` is given then the 3 `TwoWindingsTransformer` will be transformed to a `ThreeWindingsTransformer`.

Conditions to detect a triplet of `TwoWindingsTransformer` to transform:
- `BusbarSections` and the three `TwoWindingsTransformer` are the only connectable equipment allowed in the voltage level associated with the star bus.
- The three `TwoWindingsTransformer` must be connected to the star bus.
- The star terminals of the two-winding transformers must not be regulated terminals for any controller.
- Each `TwoWindingsTransformer` is well oriented if the star bus is located at the end 2.

Then a `ThreeWindingsTransformer` is created to replace them:
- The following attributes are copied from each `TwoWindingsTransformer` to the new associated leg:
  - Electrical characteristics, ratio tap changers, and phase-tap changers. Adjustments are required if the `TwoWindingsTransformer` is not well oriented.
  - Only the operational loading limits defined at the non-star end are copied to the leg.
  - Active and reactive powers at the non-star terminal are copied to the leg terminal.
- Aliases:
  - Aliases for known CGMES identifiers (terminal, transformer end, ratio, and phase-tap changer) are copied to the `ThreeWindingsTransformer` after adjusting the alias type.
  - Aliases that are not mapped are recorded in the functional log.
- Properties:
  - Voltage and angle of the star bus are added as properties of the `ThreeWindingsTransformer`.
  - Only the names of the transferred operational limits are copied as properties of the `ThreeWindingsTransformer`.
  - All the properties of the first `TwoWindingsTransformer` are transferred to the `ThreeWindingsTransformer`, then those of the second that are not in the first, and finally, the properties of the third that are not in the first two.
  - Properties that are not mapped are recorded in the functional log.
- Extensions:
  - Only IIDM extensions are copied: `TransformerFortescueData`, `PhaseAngleClock`, and `TransformerToBeEstimated`.
  - CGMES extensions can not be copied, as they cause circular dependencies.
  - Extensions that are not copied are recorded in the functional log.
- All the controllers using any of the `TwoWindingsTransformer` terminals as regulated terminal are updated.
- New and removed equipment is recorded in the functional log.
- Internal connections are created to manage the replacement.

Class: `Replace3TwoWindingsTransformersByThreeWindingsTransformers`

### Tap changers

#### Phase-tap changer position
This modification is used to modify a phase-tap changers tap position of a given `PhaseTapChangerHolder` (for two or three-winding transformer).

The new tap position can be either the one given in parameter or a relative position added to the existing one.  
The `PhaseTapChangerHolder` can be from:
- A two-winding transformers
- A three-winding transformer with a single phase-tap changer
- A leg of a three-winding transformer

Class: `PhaseTapPositionModification`

#### Ratio tap changer position
This modification is used to modify a ratio tap changers tap position of a given `RatioTapChangerHolder` (for two or three-winding transformer).

The `RatioTapChangerHolder` can be from:
- A two-winding transformers
- A three-winding transformer with a single phase-tap changer
- A leg of a three-winding transformer

Class: `RatioTapPositionModification`

### VSC converter station
This modification is used to modify the voltage and reactive power setpoints of a VSC converter station, following a generator convention.

Class: `VscConverterStationModification`
