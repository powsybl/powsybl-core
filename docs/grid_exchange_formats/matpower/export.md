# Export

Export of PowSyBl networks to Matpower is done by generating a binary file `(.mat)`, that only Matlab can interpret.
This file will include all the busView buses inside the mainSynchronousComponent and in all the parts of the main connected component linked 
with the main synchronous through Hvdc Lines with voltage source converters exported as DC Line Data.

All these busView buses are in service and each one of them will represent a row inside the Matpower bus block data. 
Additionally, will be necessary to include a fictitious bus, the `star bus`,  for each three windings transformer and also 
a bus to define the boundary side of the connected danglingLines.

Bus numbers are preserved if the PowSyBl model was originally created by importing a Matpower case, 
otherwise buses are numbered sequentially starting from bus number `1`.

The `Pd` and `Qd` attributes of the exported buses are determined by the active and reactive power of all the loads, 
batteries, and LCC converters connected to them. Generators, with voltage regulating control `off`, are considered loads
and are also accumulated, with the right sign, to the previous attributes. Likewise, shunt compensators will define 
the `Gs` and `Bs` attributes.

The gen block data of the Matpower model is created in two steps. In the first step all the equipment candidate
to be exported as a generator is grouped by bus. The following equipment of the PowSybl model are considered: generators, staticVarCompensators,
generators defined in the boundary bus of the danglingLines, and generators defined in the voltage source converters not exported as DC Line data.
In this selection, `In service` generators connected to exported buses and `out of service` generators that
are connectable to the exported buses are included. 
In the second step, the following rules are applied to export them as generators o as an accumulated load into the bus:
- All disconnected generators with voltage regulation off are discarded as they are considered loads.
- All disconnected generators with voltage regulation on are exported as `out of service` generators.
- All connected generators with voltage regulation on are exported as `in service` generators.
- All connected generators with voltage regulation off are accumulated as load if the bus is defined as type `2` (The bus has some `in service` generator controlling voltage). When the type of the bus is `1` they are exported as generators with zero voltage setpoint.

Each exported generator, in the Matpower model, is only identified bye the bus. It is possible to have several generators (rows in the gen block data) with the same bus.

The next block data in Matpower model is the branch data. Branches are created from Lines, TieLines, TwoWindingTransformers,
ThreeWindingsTransformers and DanglingLines defined in the PowSyBl model. The associated branch is only created when both connected buses or connectable buses (from and to) are exported buses.
Lines, TieLines and TwoWindingsTransformers are exported `in service` when both ends are connected. Otherwise, they are exported as `out of service`.
ThreeWindingsTransformers are exported as three branches connected to a fictitious bus, the star bus. Each disconnected leg is exported as an `out of service` branch.
When all three legs are disconnected but connectable to exported buses, they are exported as `out of service` branches with the star bus 
is defined as type `4` (isolated bus). DanglingLines are only exported when they are connected to exported buses. To export a DanglingLine an 
additional bus is required for the boundary side.

Finally, each Hvdc line with voltage source converters is exported as a DC Line data if the voltage control is `on` at both converters. If only one converter is regulating 
voltage, this converter is exported as a generator and the other, without voltage regulation, is accumulated as load. Both converters are accumulated as load when none of them
are controlling the voltage.



