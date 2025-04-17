# Export

Export of PowSyBl networks to Matpower is done by generating a binary file `(.mat)`.
This file will include all the bus view buses inside the main synchronous component and in all the parts of the main connected component linked 
with the main synchronous through HVDC lines with voltage source converters. These HVDC lines will be exported as DC line data.

All these bus view buses are in service and each one of them will represent a row inside the Matpower bus block data. 
Additionally, fictitious buses will be added for each three windings transformer (the `star bus`) and dangling line (the boundary side).

Bus numbers are preserved if the PowSyBl model was originally created by importing a Matpower case, 
otherwise buses are numbered sequentially starting from bus number `1`.

The `Pd` and `Qd` attributes of the exported buses are determined by the active and reactive power of all the loads, 
batteries, and LCC converters connected to them. Generators with voltage regulating control disabled are considered loads
and are also accumulated, with the right sign, to the previous attributes. Likewise, shunt compensators will define 
the `Gs` and `Bs` attributes.

The generation block data of the Matpower model is created in two steps. In the first step all the equipment candidate
to be exported as a generator is grouped by bus. The following equipment of the PowSybl model are considered: generators, static VAR compensators,
generators defined in the boundary bus of the dangling lines, and generators defined in the voltage source converters not exported as DC line data.
In this selection, `in service` generators connected to exported buses and `out of service` generators that
are connectable to the exported buses are included. 
In the second step, the following rules are applied to export them as generators or as a load accumulated into the bus:
- Disconnected generators with voltage regulation disabled are discarded as they are considered loads.
- Disconnected generators with voltage regulation enabled are exported as `out of service` generators.
- Connected generators with voltage regulation enabled are exported as `in service` generators.
- Connected generators with voltage regulation disabled are accumulated as load if the bus is defined as type `2` (The bus has some `in service` generator controlling voltage). When the type of the bus is `1` they are exported as generators with its voltage setpoint set to zero.

Each exported generator in the Matpower model is identified only by its bus. It is possible to have several generators (rows in the generation block data) with the same bus.

The next block data in Matpower model is the branch data. Branches are created from PowSyBl model lines, tie lines, two-winding transformers,
three-winding transformers and dangling lines. The associated branch is only created when all connected buses or connectable buses are exported buses.
Lines, tie lines and two-winding transformers are exported `in service` when both ends are connected. Otherwise, they are exported as `out of service`.
Three-winding transformers are exported as three branches connected to a fictitious bus, the star bus. Each disconnected leg is exported as an `out of service` branch.
When all three legs are disconnected but connectable to exported buses, they are exported as `out of service` branches with the star bus 
defined as type `4` (isolated bus). Dangling lines are only exported when they are connected to exported buses. To export a dangling line an 
additional bus is required to represent the boundary side.

Finally, each HVDC line with voltage source converters at its ends is exported as DC line data if the voltage control is `on` at both converters. If only one converter is regulating voltage, this converter is exported as a generator and the other, without voltage regulation, is accumulated as load. Both converters are accumulated as load when none of them are controlling the voltage.

