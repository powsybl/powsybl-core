# CIM-CGMES

```{toctree}
:hidden:
format_specification.md
triple_store.md
import.md
post_processor.md
export.md
examples.md
```

The CGMES (**C**ommon **G**rid **M**odel **E**xchange **S**pecification) is an IEC technical specification (TS 61970-600-1, TS 61970-600-2) based on the IEC CIM (**C**ommon **I**nformation **M**odel) family of standards. It was developed to meet necessary requirements for TSO data exchanges in the areas of system development and system operation. In this scenario the agents (the Modelling Authorities) generate their Individual Grid Models (IGM) that can be assembled to build broader Common Grid Models (CGM). Boundaries between IGMs are well defined: the boundary data is shared between the modelling agents and contain all boundary points required for a given grid model exchange.

In CGMES an electric power system model is described by data grouped in different subsets (profiles) and exchanged as CIM/XML files, with each file associated to a given profile. The profiles considered in PowSyBl are:
- `EQ` Equipment. Contains data that describes the equipment present in the network and its physical characteristics.
- `SSH` Steady State Hypothesis. Required input parameters to perform power flow analysis; e.g., energy injections and consumptions and setpoint values for regulating controls.
- `TP` Topology. Describe how the equipment is electrically connected. Contains the definition of power flow buses.
- `SV` State Variables. Contains all the information required to describe a steady-state power flow solution over the network.
- `EQBD` Equipment Boundary. Contains definitions of the equipment in the boundary.
- `TPBD` Topology Boundary. Topology information associated to the boundary.
- `DL` Diagram Layout. Contains information about diagram positions.
- `GL` Geographical Layout. Contains information about geographical positions.

CGMES model connectivity can be defined at two different levels of detail:

`Node/breaker` This is the level of detail required for Operation. The `EQ` contains Connectivity Nodes where the conducting equipment are attached through its Terminals. All switching devices (breakers, disconnectors, ...) are modelled. The contents of the `TP` file must be the result of the topology processing over the graph defined by connectivity nodes and switching devices, taking into account its open/closed status.

`Bus/branch` No Connectivity Nodes are present in the `EQ` file. The association of every equipment to a bus is defined directly in the `TP` file, that must be provided.
