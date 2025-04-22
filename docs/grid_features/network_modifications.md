# Network modifications

The `powsybl-iidm-modification` module gathers classes and methods used to modify the network easily.  
Each modification must first be created with the right attributes or parameters and then applied on the network.  
A `NetworkModification` offers a method to check whether or not its application would have an impact on the given network.

## Scaling
<span style="color: red">TODO</span>

## Topology modifications
Powsybl provides classes that can be used to easily modify the topology of the network.
This includes: the creation of network elements with automatic creation of switches with respect to the topology of the
voltage level, the removal of network elements and their switches, the creation of T-pieces when connecting a line to
another line, and the connection of a voltage level to a line.
All these classes rely on a builder to create the modification and then apply it on the network.

### Network element creation

#### Create feeder bay
This class should be used to create any type of `Injection`. `Injections` are network elements with one terminal, such
as loads, generators...
It takes as input:
- The `InjectionAdder`, already created with the right attributes. These attributes depend on the type of `Injection`.
- The ID of the bus or busbar section (in `BUS_BREAKER` or `NODE_BREAKER` voltage levels respectively) to which the
  injection should be connected.
- The position order of the injection: when adding an injection to a `NODE_BREAKER` voltage level, this integer will be
  used to create the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension) that is
  used for visualization. It is optional for `BUS_BREAKER` voltage levels and will be ignored if specified.
- Optionally, a name for the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension).
  By default, the ID of the injection will be used.
- Optionally, the direction of the injection. It is also used to fill the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension).
  It indicates if the injection should be displayed at the top or at the bottom of the busbar section. By default, it is
  `BOTTOM`.

When applying this modification on the network, the injection is added to the voltage level associated with the bus or busbar
section.
If the voltage level topology kind is `BUS_BREAKER`, then the injection is added to the voltage level and connected to the
bus without any extension or switches.
If the voltage level topology kind is `NODE_BREAKER`, then the injection is added to the voltage level and connected to
the busbar section with a closed disconnector and a breaker. Additionally, open disconnectors will be created on every
parallel busbar section. To know which busbar sections are parallel, the [`BusbarSectionPosition` extension](../grid_model/extensions.md#busbar-section-position-extension)
is used. The [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension) will also be
created for the injection with the given data, unless there are no extensions yet in the voltage level.

#### Create Branch Feeder bays
This class allows the creation of lines and two-winding transformers.
It takes as input:
- The `BranchAdder`, which should be created beforehand with the electrotechnical characteristics of the branch.
- The ID of the bus or busbar section (in `BUS_BREAKER` or `NODE_BREAKER` voltage levels respectively) to which the side
  1 of the branch should be connected.
- The ID of the bus or busbar section (in `BUS_BREAKER` or `NODE_BREAKER` voltage levels respectively) to which the side
  2 of the branch should be connected.
- The position order of the branch on side 1. If the voltage level on side 1 of the branch is `NODE_BREAKER`, then
  this integer is used to create the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension)
  for the branch that is used for visualization and for positioning connectables relative to each other.  
  It is optional for `BUS_BREAKER` voltage levels and will be ignored if specified.
- The position order of the branch on side 2. It is the same but on the other side.
- Optionally, a name for the feeder that will be added in the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension)
  for side 1. This name is used for visualization. By default, it is the ID of the connectable.
- Optionally, a name for the feeder for side 2.
- Optionally, the direction of the feeder on side 1. This information will be used to fill the field in the
  [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension) and indicates the relative
  position of the branch with its busbar section on side 1. The default value is `TOP`.
- Optionally, the direction on side 2.

When the modification is applied on the network, the branch is added to both voltage levels and connected on the bus or
busbar section specified for both sides.
For each side, if the voltage level topology kind is `BUS_BREAKER`, then the branch is added to the voltage level and
connected to the bus without any extension or switches. If the voltage level topology kind is `NODE_BREAKER`, then the
branch is added to the voltage level and connected to the busbar section with a closed disconnector and a breaker.
Additionally, open disconnectors will be created on every parallel busbar section. To know which busbar sections are
parallel, the [`BusbarSectionPosition` extension](../grid_model/extensions.md#busbar-section-position-extension)
is used. The [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position-extension) will also be
created for the branch with the given data, unless no extensions are already available in the voltage level.

#### Create Coupling Device
This class allows the creation of coupling devices within a voltage level to couple some busbar sections.
It takes as input:
- The ID of one bus or busbar section (in `BUS_BREAKER` or `NODE_BREAKER` voltage levels respectively)
- The ID of another bus or busbar section
- Optionally, a prefix to be used when creating the switches of the coupling device.

Both buses or busbar sections must be within the same voltage level.
If the voltage level has a `BUS_BREAKER` topology, then a new breaker is created between both buses.

If the voltage level has a `NODE_BREAKER` topology, then the coupling device is created between the two given buses or
busbar sections as such:
A closed disconnector will be created on both busbar sections.
A closed breaker will be created between the two closed disconnectors.
An open disconnector will be created on every parallel busbar section. To find the parallel busbar sections, the
[`BusbarSectionPosition` extension](../grid_model/extensions.md#busbar-section-position-extension) is used.
The coupling device can be created between busbar sections that are parallel or not. If the two busbar sections are
parallel and there are exactly two parallel busbar sections, then no open disconnectors are created.

#### Create Voltage Level Topology
This class allows the creation of the topology inside a voltage level if it is meant to be symmetrical.
The voltage level must already have been created and does not have to be empty.
When applied to a network, it will create buses or busbar section in a matrix of aligned buses or busbar sections.
In `BUS_BREAKER` topology, the buses will be separated by `Breakers` and in `NODE_BREAKER`, the switch type between each
section must be specified.
It takes as input:
- The ID of the voltage level
- The aligned buses or busbar section count. This integer indicates the "row" number of the matrix of buses or
  busbar sections.
- The section count. This integer indicates the "column" number of the matrix of buses or busbar sections.
- A list of switch kinds, for `NODE_BREAKER` voltage levels. This list indicates the switch that should be created
  between each busbar section.
  In the end, `alignedBusesOrBusbarCount` * `sectionCount` buses or busbar sections will be created, and they will be
  connected by section either by `Breakers` in `BUS_BREAKER` topology or by the switch specified by the list in `NODE_BREAKER`
  topology. The length of this list must be equal to the section count - 1.

Additional input can be provided:
- The low-bus or busbar section index. This integer indicates the index of the first "row" of buses or busbar sections
  that should be created. If the voltage level is not empty, then the buses or busbar sections will be created starting
  from this index, so it can be below some already existing buses or busbar sections. By default, it is 1 (no bus or
  busbar section already in the voltage level).
- The low-section index. This integer indicates the index of the first section of buses or busbar sections that should
  be created. If the voltage level is not empty, it is possible to create buses or busbar sections next to already
  existing ones. By default, it is 1 (no bus or busbar section already in the voltage level).
- The bus or busbar section prefix ID is optional and used, if specified, as a prefix for the IDs of the created buses
  or busbar sections. This prefix is followed by the "row" index and the section number. If it is not specified, then the
  name of the voltage level is used as prefix.
- The switch prefix ID is also optional.

<span style="color: red">TODO: add single line diagrams</span>

### Network element removal

The classes `com.powsybl.iidm.modification.RemoveFeederBay`, `com.powsybl.iidm.modification.RemoveHvdcLine`,
`com.powsybl.iidm.modification.RemoveVoltageLevel` and `com.powsybl.iidm.modification.RemoveSubstation` allow to remove
all types of elements from a network.

#### RemoveFeederBay
This is the class to use to remove any Injection, Branch or Three-winding transformer.
The builder should be used to create any instance of this class. Only the ID of the connectable to remove should be given
as input.
When applied to the network, the connectable will be removed, as well as all the switches connecting it to busbar sections.
Note: Busbar sections are not allowed to be removed with this class.

#### RemoveHvdcLine
This class should be used to remove a HVDC line.
The input arguments are:
- The ID of the HVDC line
- If the HVDC line is an LCC, an optional list of IDs of the shunt compensators associated with this HVDC line that should also be removed.
  When applied to the network, the HVDC line is removed, as well as the two converter stations on each side and the
  switches connecting them to their voltage levels. If the list of shunt compensators is not empty, then they will also be
  removed along with their switches.

#### RemoveVoltageLevel
This class is used to remove an entire voltage level. All the connectables, busbar sections, coupling devices of the voltage level
are removed. The lines, two-winding transformers and three-winding transformers are also removed as well as their
switches in other voltage levels.
The builder to be used to initialize this class takes only the ID of the voltage level to be removed.

#### RemoveSubstation
This class should be used to remove an entire substation. All the voltage levels of the substation with all their
connectables are removed. The branches and three-winding transformers are also removed with their switches in the other
substations.
The builder takes the ID of the substation as input.

### Network element repositioning
The class `com.powsybl.iidm.modification.MoveFeederBay` allows to move feeder bays of connectables 
(except `BusOrBusBarSection` connectables) from one location to another within a network.

This class allows to move a feeder bay from one busbar section to another within the network.
The builder should be used to create any instance of this class. It takes as input:

- The ID of the connectable whose feeder bay will be moved (`connectableId`). Note that `BusOrBusBarSection` connectables are not accepted.
- The ID of the target bus or busbar section (`targetBusOrBusBarSectionId`) to which the feeder bay should be connected.
- The ID of the target voltage level (`targetVoltageLevelId`) where the feeder bay will be moved to.
- The terminal object that specifies which terminal of the connectable should be moved.

When the modification is applied on the network, the system identifies and updates all relevant switches and connections 
to move the feeder bay from its current position to the specified target location. This includes disconnecting 
from the original busbar section and reconnecting to the target busbar section.
If the target voltage level topology kind is `BUS_BREAKER`, the connectable is connected to the target bus without additional switches.
If the target voltage level topology kind is `NODE_BREAKER`, the appropriate disconnectors and breakers are created to connect
the feeder bay to the target busbar section, maintaining the correct topology.
This modification ensures that the connectivity of the network is preserved while relocating the feeder bay to its new position.

### Connect a line on a line or a voltage level on a line

#### ConnectVoltageLevelOnLine
<span style="color: red">TODO</span>

#### RevertConnectVoltageLevelOnLine
<span style="color: red">TODO</span>

#### CreateLineOnLine
<span style="color: red">TODO</span>

#### RevertCreateLineOnLine
<span style="color: red">TODO</span>

### ReplaceTeePointbyVoltageLevelOnLine
<span style="color: red">TODO</span>

### Naming strategy
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

### Two-winding transformer tripping
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

If `relativeValue` is set to true, then the new constant active power (`P0`) and reactive power (`Q0`) are set as the addition of the given values to the previous ones.  
If `relativeValue` is set to false, then the new constant active power (`P0`) and reactive power (`Q0`) are updated to the new given values.

Class: `LoadModification`

#### Percent modification
This modification is used to add or remove a percentage of the P and Q of the load. The percentage to add or remove for P and Q cannot be less than -100 (in percentage).

Class: `PercentChangeLoadModification`

### Phase shifters

#### Optimize tap modification
This modification is used to find the optimal phase tap changer position of a given two-winding transformer phase shifter id.

A phase shifter optimization load flow is run with the configured `load-flow-based-phase-shifter-optimizer` to determine the optimal tap position.

Class: `PhaseShifterOptimizeTap`

#### Fixed tap modification
This modification updates the phase tap changer of a given two-winding transformer phase shifter id.

It updates its `tapPosition` with the given value and set the phase tap changer as not regulating with a `FIXED_TAP` regulation mode.

Class: `PhaseShifterSetAsFixedTap`

#### Shift tap modification
This modification is used to update the phase tap changer of a given two-winding transformer phase shifter id.

It sets the phase tap changer as not regulating with a `FIXED_TAP` regulation mode and updates its `tapPosition` by adjusting it with the given `tapDelta` applied on the current tap position. The resulting tap position is bounded by the phase tap changer lowest and highest possible positions.

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
  - Electrical characteristics, ratio tap changers, and phase tap changers. No adjustments are required.
  - Operational Loading Limits are copied to the non-star end of the two-winding transformers.
  - Active and reactive powers at the terminal are copied to the non-star terminal of the two-winding transformer.
- Aliases:
  - Aliases for known CGMES identifiers (terminal, transformer end, ratio, and phase tap changer) are copied to the right `TwoWindingsTransformer` after adjusting the alias type.
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
  - Electrical characteristics, ratio tap changers, and phase tap changers. Adjustments are required if the `TwoWindingsTransformer` is not well oriented.
  - Only the operational loading limits defined at the non-star end are copied to the leg.
  - Active and reactive powers at the non-star terminal are copied to the leg terminal.
- Aliases:
  - Aliases for known CGMES identifiers (terminal, transformer end, ratio, and phase tap changer) are copied to the `ThreeWindingsTransformer` after adjusting the alias type.
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

#### Phase tap changer position
This modification is used to modify a phase tap changers tap position of a given `PhaseTapChangerHolder` (for two or three-winding transformer).

The new tap position can be either the one given in parameter or a relative position added to the existing one.  
The `PhaseTapChangerHolder` can be from:
- A two-winding transformers
- A three-winding transformer with a single phase tap changer
- A leg of a three-winding transformer

Class: `PhaseTapPositionModification`

#### Ratio tap changer position
This modification is used to modify a ratio tap changers tap position of a given `RatioTapChangerHolder` (for two or three-winding transformer).

The `RatioTapChangerHolder` can be from:
- A two-winding transformers
- A three-winding transformer with a single phase tap changer
- A leg of a three-winding transformer

Class: `RatioTapPositionModification`

### VSC converter station
This modification is used to modify the voltage and reactive power setpoints of a VSC converter station, following a generator convention.

Class: `VscConverterStationModification`
