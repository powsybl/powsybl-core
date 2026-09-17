# Topology modifications
Powsybl provides classes that can be used to easily modify the topology of the network.
This includes: the creation of network elements with automatic creation of switches with respect to the topology of the
voltage level, the removal of network elements and their switches, the creation of T-pieces when connecting a line to
another line, and the connection of a voltage level to a line.
All these classes rely on a builder to create the modification and then apply it on the network.

## Naming strategy
The naming strategy aims at clarifying and facilitating the naming of the different network elements created via the different
`com.powsybl.iidm.modification.NetworkModification` classes. Based on the name of the network element the user wishes to create
(a VoltageLevel, a BranchFeederBay, etc.), all the other elements created during the NetworkModification will be given a name
using this name as baseline and prefixes/suffixes according to the naming strategy chosen by the user.
The naming strategy can be either the default one `com.powsybl.iidm.modification.topology.DefaultNamingStrategy`
or a new implementation of the `NamingStrategy` interface.

### Default naming strategy
Default naming strategy is used if no other naming strategy is specified.
The `DefaultNamingStrategy` implements a simple naming convention following the pattern:
base name + separator + element type + optional index.
The default implementation uses underscores as separators and appends element types and indices when necessary
to ensure unique naming.

### Custom naming strategies
Other Naming strategies can be implemented based on the `NamingStrategy` interface.
This allows for organization-specific naming conventions, different separator characters, or specialized formatting rules.

### Service loader for naming strategies
The `NamingStrategiesServiceLoader` enables dynamic discovery of available naming strategies through Java's ServiceLoader mechanism.

## Network element creation

### Create feeder bay
This class should be used to create any type of `Injection`. `Injections` are network elements with one terminal, such
as loads, generators...
It takes as input:
- The `InjectionAdder`, already created with the right attributes. These attributes depend on the type of `Injection`.
- The ID of the bus or busbar section (in `BUS_BREAKER` or `NODE_BREAKER` voltage levels respectively) to which the
  injection should be connected.
- The position order of the injection: when adding an injection to a `NODE_BREAKER` voltage level, this integer will be
  used to create the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position) that is
  used for visualization. It is optional for `BUS_BREAKER` voltage levels and will be ignored if specified.
- Optionally, a name for the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position).
  By default, the ID of the injection will be used.
- Optionally, the direction of the injection. It is also used to fill the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position).
  It indicates if the injection should be displayed at the top or at the bottom of the busbar section. By default, it is
  `BOTTOM`.
- Optionally, a boolean `logOrThrowIfIncorrectPositionOrder`, that indicates what should happen if the position order is
incorrect. This is mainly useful for voltage levels with `NODE_BREAKER` topology, since the `ConnectablePosition` extension
is not created otherwise. The order position may be incorrect if
  - it has already been taken on the busbar section,
  - if it is higher or lower than the maximum or minimum available order positions for the busbar section,
If the boolean is set to false, then the order position will be ignored and the `ConnectablePosition` extension will not be
created, but the `Injection` will be created. If the boolean is set to true, the `Injection` will not be created, and
either an exception will be thrown or a log will be returned, depending on the `throwException` boolean given when applying
the modification.


When applying this modification on the network, the injection is added to the voltage level associated with the bus or busbar
section.
If the voltage level topology kind is `BUS_BREAKER`, then the injection is added to the voltage level and connected to the
bus without any extension or switches.
If the voltage level topology kind is `NODE_BREAKER`, then the injection is added to the voltage level and connected to
the busbar section with a closed disconnector and a breaker. Additionally, open disconnectors will be created on every
parallel busbar section. To know which busbar sections are parallel, the [`BusbarSectionPosition` extension](../grid_model/extensions.md#busbar-section-position)
is used. The [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position) will also be
created for the injection with the given data, unless there are no extensions yet in the voltage level.

For instance, adding a `Load` to a beforehand empty `NODE_BREAKER` voltage level can be done with:
```java
LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
        .setId("newLoad")
        .setP0(100.0)
        .setQ0(50.0);
new CreateFeederBayBuilder()
        .withInjectionAdder(loadAdder)
        .withBusOrBusbarSectionId("bbs")
        .withInjectionPositionOrder(1)
        .build()
        .apply(network);
```
It will result in:
![Node/breaker network with new load](img/networkNodeBreakerWithLoad.svg){width="30%" align=center}

Class: `CreateFeederBay`

### Create branch feeder bays
This class allows the creation of lines and two-winding transformers.
It takes as input:
- The `BranchAdder`, which should be created beforehand with the electrotechnical characteristics of the branch.
- The ID of the bus or busbar section (in `BUS_BREAKER` or `NODE_BREAKER` voltage levels respectively) to which the side
  1 of the branch should be connected.
- The ID of the bus or busbar section (in `BUS_BREAKER` or `NODE_BREAKER` voltage levels respectively) to which the side
  2 of the branch should be connected.
- The position order of the branch on side 1. If the voltage level on side 1 of the branch is `NODE_BREAKER`, then
  this integer is used to create the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position)
  for the branch that is used for visualization and for positioning connectables relative to each other.
  It is optional for `BUS_BREAKER` voltage levels and will be ignored if specified.
- The position order of the branch on side 2. It is the same but on the other side.
- Optionally, a name for the feeder that will be added in the [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position)
  for side 1. This name is used for visualization. By default, it is the ID of the connectable.
- Optionally, a name for the feeder for side 2.
- Optionally, the direction of the feeder on side 1. This information will be used to fill the field in the
  [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position) and indicates the relative
  position of the branch with its busbar section on side 1. The default value is `TOP`.
- Optionally, the direction on side 2.
- Optionally, a boolean `logOrThrowIfIncorrectPositionOrder1`, that indicates what should happen if the position order is
incorrect on side 1 of the branch. This is mainly useful for voltage levels with `NODE_BREAKER` topology, since the
`ConnectablePosition` extension is not created otherwise. The order position may be incorrect if
  - it has already been taken on the busbar section,
  - if it is higher or lower than the maximum or minimum available order positions for the busbar section,
If the boolean is set to false, then the order position will be ignored and the `ConnectablePosition` extension will not be
created, but the `Branch` will. If the boolean is set to true, the `Branch` will not be created, and
either an exception will be thrown or a log will be returned, depending on the `throwException` boolean given when applying
the modification.
- Optionally, a boolean `logOrThrowIfIncorrectPositionOrder2`, which is the same but for the side 2 of the branch.


When the modification is applied on the network, the branch is added to both voltage levels and connected on the bus or
busbar section specified for both sides.
For each side, if the voltage level topology kind is `BUS_BREAKER`, then the branch is added to the voltage level and
connected to the bus without any extension or switches. If the voltage level topology kind is `NODE_BREAKER`, then the
branch is added to the voltage level and connected to the busbar section with a closed disconnector and a breaker.
Additionally, open disconnectors will be created on every parallel busbar section. To know which busbar sections are
parallel, the [`BusbarSectionPosition` extension](../grid_model/extensions.md#busbar-section-position)
is used. The [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position) will also be
created for the branch with the given data, unless no extensions are already available in the voltage level.

For instance, here is a `Substation`:
![Node/breaker network substation](img/networkNodeBreakerBeforeAddingTwoWindingTransformer.svg){width="100%" align=center}
Adding a `TwoWindingsTransformer` between both voltage levels of this substation can be done with:
```java
TwoWindingsTransformerAdder twtAdder = substation.newTwoWindingsTransformer()
        .setId("twt")
        .setR(1.0)
        .setX(10.0)
        .setG(0.0)
        .setB(0.0)
        .setRatedU1(225.0)
        .setRatedU2(400.0);
new CreateBranchFeederBaysBuilder()
        .withBranchAdder(twtAdder)
        .withBusOrBusbarSectionId1("bbs")
        .withPositionOrder1(1)
        .withBusOrBusbarSectionId2("bbs_1_1")
        .withPositionOrder2(1)
        .build()
        .apply(network);
```
It will result in:
![Node/breaker network with new two-winding transformer](img/networkNodeBreakerWithTwoWindingTransformer.svg){width="100%" align=center}

Class: `CreateBranchFeederBays`

### Create coupling device
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
[`BusbarSectionPosition` extension](../grid_model/extensions.md#busbar-section-position) is used.
The coupling device can be created between busbar sections that are parallel or not. If the two busbar sections are
parallel and there are exactly two parallel busbar sections, then no open disconnectors are created.

In the following single-line diagram, we can see three coupling devices added for each section created using:
```java
new CreateCouplingDeviceBuilder()
        .withBusOrBusbarSectionId1("bbs_1_1")
        .withBusOrBusbarSectionId2("bbs_2_1")
        .build()
        .apply(network);

new CreateCouplingDeviceBuilder()
        .withBusOrBusbarSectionId1("bbs_1_2")
        .withBusOrBusbarSectionId2("bbs_2_2")
        .build()
        .apply(network);

new CreateCouplingDeviceBuilder()
        .withBusOrBusbarSectionId1("bbs_1_3")
        .withBusOrBusbarSectionId2("bbs_2_3")
        .build()
        .apply(network);
```

The coupling device between the sections was created using:
```java
new CreateCouplingDeviceBuilder()
        .withBusOrBusbarSectionId1("bbs_1_1")
        .withBusOrBusbarSectionId2("bbs_1_2")
        .build()
        .apply(network);
```
![Emtpy node/breaker network with coupling devices](img/networkNodeBreakerEmptyWithCouplingDevice.svg){width="100%" align=center}

Class: `CreateCouplingDevice`

### Create voltage level topology
This class allows the creation of the topology inside a voltage level if it is meant to be symmetrical.
The voltage level must already exist and does not have to be empty.
When applied to a network, it will create buses or busbar sections in a matrix of aligned buses or busbar sections.
In `BUS_BREAKER` topology, the buses will be separated by `Breakers` and in `NODE_BREAKER`, the switch type between each
section must be specified.
It takes as input:
- The ID of the voltage level
- The count of aligned buses or busbar sections. This integer indicates the "row" number of the matrix of buses or
  busbar sections.
- The section count. This integer indicates the "column" number of the matrix of buses or busbar sections.
- A list of switch kinds, for `NODE_BREAKER` voltage levels. This list indicates the switches that should be created
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
  name of the voltage level is used as a prefix.
- The switch prefix ID is also optional.
- The boolean connectExistingConnectables indicates whether existing connectables should be connected to the new topology
if the busbar sections are created in a non-empty voltage level. If true, they will all be connected with
an open switch of the same kind as the first switch that connects the connectable to the other busbar sections.
If this boolean is true, the [`ConnectFeedersToBusbarSections`](#connect-feeders-to-busbar-sections) modification will be called on the network.

For instance, this single-line diagram shows the result of calling this modification on an empty voltage level in `NODE_BREAKER` topology with:
- two aligned busbar sections
- three sections
- `Breakers` between the first and the second sections and `Disconnector` between the second and the third sections.
It can be done with:
```java
new CreateVoltageLevelTopologyBuilder()
        .withVoltageLevelId("VL")
        .withAlignedBusesOrBusbarCount(2)
        .withSectionCount(3)
        .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR)
        .build()
        .apply(network);
```
![Node/breaker network with busbar sections created with the network modification](img/networkNodeBreakerWithBusbarSections.svg){width="80%" align=center}


Class: `CreateVoltageLevelTopology`

### Create voltage level sections
This class allows the creation of new busbar sections inside a voltage level in the NODE_BREAKER topology.
The voltage level must already have been created, must already contain some busbar sections, and these busbar sections 
must have the [`BusbarSectionPosition` extension](../grid_model/extensions.md#busbar-section-position),
which indicates their position in the voltage level busbar sections matrix (busbarIndex and sectionIndex).
When applied to a network, it will create new busbar sections before or after a reference busbar section.

It takes as input:
- The ID of the reference busbar section
- A boolean indicating if the new busbar section(s) must be created before(left) or after(right) the reference busbar section
- A boolean indicating if a new busbar section must be created on all busbars, or only on the busbar of the reference busbar section
- The switch kind of the new switch(es) that will be created left to the newly created busbar section :
  - DISCONNECTOR means that only a DISCONNECTOR switch will be created
  - BREAKER means that a BREAKER switch surrounded by two DISCONNECTOR switches will be created
- The switch kind of the new switch(es) that will be created right to the newly created busbar section :
  - DISCONNECTOR means that only a DISCONNECTOR switch will be created
  - BREAKER means that a BREAKER switch surrounded by two DISCONNECTOR switches will be created
- A boolean indicating if the new switches created left to the newly created busbar section(s) are fictitious
- A boolean indicating if the new switches created right to the newly created busbar section(s) are fictitious
- A boolean indicating if the new switches created left to the newly created busbar section(s) will be open
- A boolean indicating if the new switches created right to the newly created busbar section(s) will be open
- The switch prefix ID, used as a prefix for the IDs of the newly created switches.
- The busbar section prefix ID, used as a prefix for the IDs of the newly created busbar sections. This prefix
  is followed by the busbar index and the section index if the default naming strategy is used.

For instance, it is possible to add new busbar sections on the left of the voltage level with the following code:
```java
new CreateVoltageLevelSectionsBuilder()
    .withReferenceBusbarSectionId("bbs_1_1")
    .withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(false)
    .withCreateNewBusbarSectionOnAllBusbars(true)
    .withSwitchKindLeft(SwitchKind.BREAKER)
    .withLeftSwitchOpen(true)
    .build()
    .apply(network);
```
It will result in three new busbar sections:
![Voltage level with new busbar sections before first section](img/networkNodeBreakerCreateVlSectionsBeforeFirstSection.svg){width="80%" align=center}

It is also possible to add new busbar sections on the right of the voltage level:
```java
new CreateVoltageLevelSectionsBuilder()
    .withReferenceBusbarSectionId("bbs_1_3")
    .withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(true)
    .withCreateNewBusbarSectionOnAllBusbars(true)
    .withSwitchKindRight(SwitchKind.DISCONNECTOR)
    .withRightSwitchOpen(true)
    .build()
    .apply(network);
```
It will result in three new busbar sections:
![Voltage level with new busbar sections after last section](img/networkNodeBreakerCreateVlSectionsAfterLastSection.svg){width="80%" align=center}

Or also between existing sections:
```java
new CreateVoltageLevelSectionsBuilder()
    .withReferenceBusbarSectionId("bbs_1_1")
    .withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(true)
    .withCreateNewBusbarSectionOnAllBusbars(true)
    .withSwitchKindLeft(SwitchKind.DISCONNECTOR)
    .withSwitchKindRight(SwitchKind.BREAKER)
    .withLeftSwitchOpen(true)
    .withRightSwitchOpen(false)
    .build()
    .apply(network);
```
![Voltage level with new busbar sections between sections](img/networkNodeBreakerCreateVlSectionsBetweenSections.svg){width="80%" align=center}

In all of these single-line diagrams, the busbar sections that are added are the ones that are not connected to any feeders.

Class: `CreateVoltageLevelSections`

## Connect feeders to busbar sections
This class allows the connection of feeders to busbar sections in `NODE_BREAKER` topology.
The [`ConnectablePosition` extension](../grid_model/extensions.md#connectable-position) must be available for each busbar section in the voltage level.

It takes as input:
- A list of connectables that should be connected. None of them should be a `BusbarSection`.
- A list of busbar sections. The connectables will be connected to these busbar sections if they are not already.
- A boolean `connectCouplingDevices` indicating if the coupling devices of the voltage level should be connected to the busbar sections.
If the busbar sections are not already connected on each side of the coupling device breaker, an open switch will be created.
- A string `couplingDeviceSwitchPrefixId` that will be used, if the boolean `connectCouplingDevices` is true, in the naming strategy
to determine the IDs of the new disconnectors.

When applied to a network, the network modification will loop through all the parallel busbar sections of each input busbar section to gather the
switches that are connecting the feeders. Then, the feeders and coupling devices will be connected by a switch of the same kind
as the first switch that connects the feeder to the other busbar sections. If all the feeders are already connected to the busbar sections,
then the network modification will do nothing.

Let's take an example. This is the voltage level before applying the modification:
![Node/breaker network with coupling devices](img/networkNodeBreakerWithCouplingDevices.svg){width="100%" align=center}

If we apply the modification with both busbar sections, all the connectables and coupling devices:
```java
List<Connectable> connectables = network.getVoltageLevel("vl1")
        .getConnectableStream()
        .filter(c -> !(c instanceof BusbarSection))
        .toList();
List<BusbarSection> busbarSections = network.getVoltageLevel("vl1")
        .getConnectableStream()
        .filter(BusbarSection.class::isInstance)
        .map(BusbarSection.class::cast)
        .toList();
new ConnectFeedersToBusbarSectionsBuilder()
        .withConnectablesToConnect(connectables)
        .withBusbarSectionsToConnect(busbarSections)
        .withConnectCouplingDevices(true)
        .build()
        .apply(network);
```
We will get:
![Node/breaker network with coupling devices and connected feeder](img/networkNodeBreakerWithFeedersConnected.svg){width="100%" align=center}

If we want to only connect the two-winding transformers on busbar section bbs3:
```java
List<Connectable> transformers = network.getVoltageLevel("vl1")
        .getConnectableStream()
        .filter(TwoWindingsTransformer.class::isInstance)
        .toList();
BusbarSection bbs3 = network.getBusbarSection("bbs3");
new ConnectFeedersToBusbarSectionsBuilder()
        .withConnectablesToConnect(transformers)
        .withBusbarSectionsToConnect(List.of(bbs3))
        .withConnectCouplingDevices(false)
        .build()
        .apply(network);
```
We will get:
![Node/breaker network with connected transformers on bbs3](img/networkNodeBreakerWithTwtConnected.svg){width="100%" align=center}

Class: `ConnectFeedersToBusbarSections`

## Network element removal

The classes `com.powsybl.iidm.modification.RemoveFeederBay`, `com.powsybl.iidm.modification.RemoveHvdcLine`,
`com.powsybl.iidm.modification.RemoveVoltageLevel` and `com.powsybl.iidm.modification.RemoveSubstation` allow to remove
all types of elements from a network.

### Remove feeder bay
This is the class to use to remove any Injection, Branch or Three-winding transformer.
The builder should be used to create any instance of this class. Only the ID of the connectable to remove should be given
as input.
When applied to the network, the connectable will be removed, as well as all the switches connecting it to busbar sections.
Note: Busbar sections are not allowed to be removed with this class.

For instance, removing a load can be done with:
```java
new RemoveFeederBayBuilder()
        .withConnectableId("loadId")
        .build()
        .apply(network);
```

Class: `RemoveFeederBay`

### Remove HVDC line
This class should be used to remove an HVDC line.
The input arguments are:
- The ID of the HVDC line
- If the HVDC line is an LCC, an optional list of IDs of the shunt compensators associated with this HVDC line that should also be removed.
  When applied to the network, the HVDC line is removed, as well as the two converter stations on each side and the
  switches connecting them to their voltage levels. If the list of shunt compensators is not empty, then they will also be
  removed along with their switches.

For instance, removing an HVDC line and its associated shunt compensators can be done with:
```java
new RemoveHvdcLineBuilder()
        .withHvdcLineId("hvdcLineId")
        .withShuntCompensatorIds(List.of("shunt1", "shunt2"))
        .build()
        .apply(network);
```

Class: `RemoveHvdcLine`

### Remove voltage level
This class is used to remove an entire voltage level. All the connectables, busbar sections, coupling devices of the voltage level
are removed. The lines, two-winding transformers and three-winding transformers are also removed as well as their
switches in other voltage levels.
The builder to be used to initialize this class takes only the ID of the voltage level to be removed.

For instance, removing a voltage level can be done with:
```java
new RemoveVoltageLevelBuilder()
        .withVoltageLevelId("voltageLevelId")
        .build()
        .apply(network);
```

Class: `RemoveVoltageLevel`

### Remove substation
This class should be used to remove an entire substation. All the voltage levels of the substation with all their
connectables are removed. The branches and three-winding transformers are also removed with their switches in the other
substations.
The builder takes the ID of the substation as input.

For instance, removing a substation can be done with:
```java
new RemoveSubstationBuilder()
        .withSubstationId("substationId")
        .build()
        .apply(network);
```

Class: `RemoveSubstation`

## Move network elements

### Move feeder bay
This class is used to move feeder bays of connectables
(except `BusOrBusBarSection` connectables) from one place to another within a network.

This class allows moving a feeder bay from one busbar section to another within the network.
The builder should be used to create any instance of this class. It takes as input:

- The ID of the connectable whose feeder bay will be moved (`connectableId`). Note that `BusOrBusBarSection` connectables are not accepted.
- The ID of the target bus or busbar section (`targetBusOrBusBarSectionId`) to which the feeder bay should be connected.
- The ID of the target voltage level (`targetVoltageLevelId`) where the feeder bay will be moved to.
- The terminal object that specifies which terminal of the connectable should be moved.

When the modification is applied on the network, the system identifies and updates all relevant switches and connections
to move the feeder bay from its current position to the specified target place. This includes disconnecting
from the original busbar section and reconnecting to the target busbar section.
If the target voltage level topology kind is `BUS_BREAKER`, the connectable is connected to the target bus without additional switches.
If the target voltage level topology kind is `NODE_BREAKER`, the appropriate disconnectors and breakers are created to connect
the feeder bay to the target busbar section, maintaining the correct topology.
This modification ensures that the connectivity of the network is preserved while moving the feeder bay to its new position.

For instance, this is a voltage level: 
![Load1 in VL1.svg](img/networkNodeBreakerLoad1InVl1.svg)
With this modification, we can easily move load1 from bbs1 to bbs5, which is in another voltage level:
```java
new MoveFeederBayBuilder()
        .withConnectableId("load1")
        .withTargetBusOrBusBarSectionId("bbs5")
        .withTargetVoltageLevelId("vl2")
        .build()
        .apply(network);
```
![Load1 in VL2.svg](img/networkNodeBreakerLoad1InVl2.svg)
Class: `MoveFeederBay`

## Line splitting and tapping

### Connect voltage level on line
This class cuts an existing line into two new lines that are connected to an existing voltage level (the "switching voltage level").
The switching voltage level should be added to the network just before calling this method and should contain
at least a bus in `BUS_BREAKER` topology or a busbar section in `NODE_BREAKER` topology.

It takes as input:
- The original line to be cut.
- The percentage to split the electrical characteristics of the original line (R, X, B, etc.).
- The ID of the bus or busbar section in the switching voltage level where the lines will be connected.
- The IDs for the two new line segments.
- Optionally, names for the two new line segments.
- Optionally, position orders for the new lines to create [`ConnectablePosition` extensions](../grid_model/extensions.md#connectable-position) for visualization.

When applied, the original line is removed and two new lines are created. Switches are automatically created in the 
switching voltage level to connect the new lines to the specified bus or busbar section, according to the voltage level topology.

For instance, let's look at this network area diagram of a network with three voltage levels:
![Network area diagram](img/networkNodeBreakerAreaDiagram.svg)

We can connect a fictitious voltage level `VL_TEST` on the line between `VL3` and `VL2`:
```java
new ConnectVoltageLevelOnLineBuilder()
        .withLine(network.getLine("VL2_VL3_Line"))
        .withBusbarSectionOrBusId("bbs")
        .build()
        .apply(network);
```
The associated network area diagram is:
![Network area diagram connect voltage level on line](img/networkNodeBreakerAreaDiagramConnectVlOnLine.svg)
The line between `VL3` and `VL2` is now connected to the fictitious voltage level `VL_TEST` and cut in two lines.

Class: `ConnectVoltageLevelOnLine`

### Revert voltage level connection
This class reverses the action performed by `ConnectVoltageLevelOnLine`. It replaces two existing lines that share a 
common voltage level at one of their ends with a single new line.

It takes as input:
- The IDs of the two lines to be merged.
- The ID for the new merged line.
- Optionally, a name for the new merged line.

When applied, the two lines are removed and replaced by a single line connecting the two outer voltage levels. 
The common switching voltage level is removed if it no longer contains any equipment (except for buses or busbar sections).

For instance, merging two lines back into one can be done with:
```java
new RevertConnectVoltageLevelOnLineBuilder()
        .withLine1Id("line1Id")
        .withLine2Id("line2Id")
        .withLineId("newLineId")
        .build()
        .apply(network);
```

Class: `RevertConnectVoltageLevelOnLine`

### Create a line tee point
This class connects an existing voltage level to an existing line through a tee point.
It cuts the existing line in two, creating a fictitious voltage level (the tee point) between them, and then connects the 
existing voltage level to this tee point with a new line.

It takes as input:
- The percentage to split the original line at the tee point.
- The ID of the bus or busbar section in the existing voltage level to be connected.
- The ID for the fictitious voltage level.
- Optionally, the name for the fictitious voltage level.
- A boolean indicating whether to create a fictitious substation for the tee point.
- The IDs for the two line segments.
- Optionally, the name for the two line segments.
- The original line to be cut.
- The `LineAdder` for the new line connecting the existing voltage level to the tee point.
- Optionally, a position order for the new line connection to create [`ConnectablePosition` extensions](../grid_model/extensions.md#connectable-position) for visualization.

For instance, let's look at this network area diagram of a network with three voltage levels:
![Network area diagram](img/networkNodeBreakerAreaDiagram.svg)

We can connect a fictitious voltage level `VL_TEST` on the line between `VL3` and `VL2` through a tee point:
```java
Line line = network.getLine("VL2_VL3_Line");
LineAdder adder = network.newLine()
        .setId("newLine")
        .setR(1.0)
        .setX(10.0)
        .setG1(0.0).setG2(0.0).setB1(0.0).setB2(0.0);

new CreateLineOnLineBuilder()
        .withLine(line)
        .withLineAdder(adder)
        .withBusbarSectionOrBusId("bbs")
        .withFictitiousVoltageLevelId("VL_TEE_POINT")
        .build()
        .apply(network);
```
The associated network area diagram is:
![Network area tee point](img/networkNodeBreakerAreaDiagramTeePoint.svg)!
The line between `VL3` and `VL2` is now connected to the fictitious voltage level `VL_TEST` via a new line. 
A fictitious voltage level has been created to represent the tee point.

Class: `CreateLineOnLine`

### Revert line tee point
This class reverses the action performed by `CreateLineOnLine`. It replaces three existing lines that meet at a common tee point with a single new line.

It takes as input:
- The IDs of the two lines to be merged (those forming the original path).
- The ID of the line to be removed (the one connecting to the tapped voltage level).
- The ID for the new merged line.
- Optionally, a name for the new merged line.

When applied, the three lines are removed and replaced by a single line. The tee point voltage level and the tapped voltage level 
are removed if they become empty (except for buses or busbar sections).

For instance, removing a tee point and merging the path can be done with:
```java
new RevertCreateLineOnLineBuilder()
        .withLineToBeMerged1Id("line1Id")
        .withLineToBeMerged2Id("line2Id")
        .withLineToBeDeletedId("lineToBeDeletedId")
        .withMergedLineId("mergedLineId")
        .build()
        .apply(network);
```

Class: `RevertCreateLineOnLine`

### Replace tee point by voltage level
This class transforms a tee point configuration into a switching voltage level configuration. It replaces three existing 
lines meeting at a tee point with two new lines connected to the formerly tapped voltage level, which now acts as a switching voltage level.

It takes as input:
- The IDs of the three lines connected to the tee point.
- The ID of the existing bus or busbar section in the tapped voltage level where the new lines will be connected.
- The IDs for the two new lines.
- Optionally, names for the two new lines.

When applied, the three lines and the tee point are removed, and two new lines are created connecting the two original ends to the formerly tapped voltage level.

For instance, replacing a tee point configuration can be done with:
```java
new ReplaceTeePointByVoltageLevelOnLineBuilder()
        .withTeePointLine1("line1Id")
        .withTeePointLine2("line2Id")
        .withTeePointLineToRemove("lineToRemoveId")
        .withBbsOrBusId("bbsId")
        .withNewLine1Id("newLine1Id")
        .withNewLine2Id("newLine2Id")
        .build()
        .apply(network);
```

Class: `ReplaceTeePointByVoltageLevelOnLine`
