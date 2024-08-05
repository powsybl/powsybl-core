# Network modifications

The `powsybl-iidm-modification` module gathers classes and methods used to modify the network easily.
Each modification must first be created with the right attributes or parameters and then applied on the network.

## Scaling
<span style="color: red">TODO</span>


## Topology modifications

Powsybl provides classes that can be used to easily modify the topology of voltage levels.
This includes: the creation of network elements with automatic creation of switches with respect to the topology of the 
voltage level, the removal of network elements and their switches, the creation of T-pieces when connecting a line to 
another line, and the connection of a voltage level to a line. 
All these classes rely on a builder to create the modification and then apply it on the network.

### Network element creation

#### Create feeder bay
This class should be used to create any type of `Injection`. `Injections` are network elements with one terminal, such 
as loads, generators... 
It takes as input:
- The `InjectionAdder`, already created with the right parameters
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
created for the injection with the given data, unless no extensions are already available in the voltage level.

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
- The low bus or busbar section index. This integer indicates the index of the first "row" of buses or busbar sections 
that should be created. If the voltage level is not empty, then the buses or busbar sections will be created starting 
from this index, so it can be below some already existing buses or busbar sections. By default, it is 1 (no bus or 
busbar section already in the voltage level).
- The low section index. This integer indicates the index of the first section of buses or busbar sections that should
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
This class should be used to remove an entire substations. All the voltage levels of the substation with all their 
connectables are removed. The branches and three-winding transformers are also removed with their switches in the other
substations. 
The builder takes the ID of the substation as input.


### Connect a line on a line or a voltage level on a line
<span style="color: red">TODO</span>


### Naming strategy
<span style="color: red">TODO</span>

