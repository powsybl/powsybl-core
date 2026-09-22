# Tripping
Tripping modifications are used to disconnect network elements by opening the appropriate switches.

## General principles
The tripping modifications rely on a topological traversal to identify which switches should be opened:
- In **Node/Breaker** topology, the traversal starts from the equipment terminal and searches for the nearest closed, non-fictitious `BREAKER`. 
If such a breaker is found, it is opened.
- In **Bus/Breaker** topology, the traversal stops at the terminal, and the terminal is simply disconnected.
- For **DC** equipment, the traversal searches for the nearest closed, non-fictitious DC switch.

Most tripping modifications for multi-terminal equipment (like lines or transformers) allow for side-specific tripping by 
providing a voltage level ID. If no voltage level ID is specified, all sides are tripped.

## Comparison with disconnection
Tripping differs from **Planned Disconnection** in the way it handles switches in **Node/Breaker** topology. While a planned 
disconnection aims to fully isolate a piece of equipment (e.g., for maintenance) by opening both breakers and disconnectors, 
tripping mimics a protection system by opening only the **breakers** necessary to interrupt the current. See [Disconnections](other_modifications.md#disconnections) for more details.

## Battery tripping
This modification trips a battery by opening the switches connected to its terminal.

It takes as input:
- The ID of the battery to trip.

Class: `BatteryTripping`

## Branch tripping
This modification trips a branch (line or two-winding transformer) by opening the switches connected to its terminals.

It takes as input:
- The ID of the branch to trip.
- Optionally, a voltage level ID to restrict the tripping to a single side.

Class: `BranchTripping`

## Busbar section tripping
This modification trips a busbar section by opening the switches connected to its terminal.

It takes as input:
- The ID of the busbar section to trip.

Class: `BusbarSectionTripping`

## Bus tripping
This modification trips a bus by opening the switches connected to all the terminals associated with the bus.

It takes as input:
- The ID of the bus to trip.

Class: `BusTripping`

## Boundary line tripping
This modification trips a boundary line by opening the switches connected to its terminal.

It takes as input:
- The ID of the boundary line to trip.

Class: `BoundaryLineTripping`

## DC ground tripping
This modification trips a DC ground by opening the switches connected to all its DC terminals.

It takes as input:
- The ID of the DC ground to trip.

Class: `DcGroundTripping`

## DC line tripping
This modification trips a DC line by opening the DC switches connected to its terminals.

It takes as input:
- The ID of the DC line to trip.
- Optionally, a DC node ID to restrict the tripping to a single side.

Class: `DcLineTripping`

## DC node tripping
This modification trips a DC node by opening the switches connected to all the DC terminals associated with the DC node.

It takes as input:
- The ID of the DC node to trip.

Class: `DcNodeTripping`

## Generator tripping
This modification trips a generator by opening the switches connected to its terminal.

It takes as input:
- The ID of the generator to trip.

Class: `GeneratorTripping`

## HVDC line tripping
This modification trips an HVDC line by opening the switches connected to its converter stations terminals.

It takes as input:
- The ID of the HVDC line to trip.
- Optionally, a voltage level ID to restrict the tripping to a single side.

Class: `HvdcLineTripping`

## Line tripping
This modification trips a line by opening the switches connected to its terminals.

It takes as input:
- The ID of the line to trip.
- Optionally, a voltage level ID to restrict the tripping to a single side.

Class: `LineTripping`

## Load tripping
This modification trips a load by opening the switches connected to its terminal.

It takes as input:
- The ID of the load to trip.

Class: `LoadTripping`

## Shunt compensator tripping
This modification trips a shunt compensator by opening the switches connected to its terminal.

It takes as input:
- The ID of the shunt compensator to trip.

Class: `ShuntCompensatorTripping`

## Static var compensator tripping
This modification trips a static var compensator by opening the switches connected to its terminal.

It takes as input:
- The ID of the static var compensator to trip.

Class: `StaticVarCompensatorTripping`

## Switch tripping
This modification trips a switch by directly opening it. Unlike other tripping modifications, it does not perform a topological traversal to find breakers.

It takes as input:
- The ID of the switch to trip.

Class: `SwitchTripping`

## Three-winding transformer tripping
This modification trips a three-winding transformer by opening the switches connected to the terminals of its three legs.

It takes as input:
- The ID of the three-winding transformer to trip.

Class: `ThreeWindingsTransformerTripping`

## Tie line tripping
This modification trips a tie line by opening the switches connected to its boundary lines terminals.

It takes as input:
- The ID of the tie line to trip.
- Optionally, a voltage level ID to restrict the tripping to a single side.

Class: `TieLineTripping`

## Two-winding transformer tripping
This modification trips a two-winding transformer by opening the switches connected to its terminals.

It takes as input:
- The ID of the two-winding transformer to trip.
- Optionally, a voltage level ID to restrict the tripping to a single side.

Class: `TwoWindingsTransformerTripping`

## Voltage source converter tripping
This modification trips a voltage source converter (VSC) by opening the switches connected to all its AC and DC terminals.

It takes as input:
- The ID of the converter to trip.

Class: `VoltageSourceConverterTripping`
