# Import
The import of a UCTE file into an IIDM grid model is done in three steps.
First, the file is parsed and a UCTE grid model is created in memory.
Then, some inconsistency checks are performed on this model.
Finally, the UCTE grid model is converted into an IIDM grid model.

The UCTE parser provided by PowSyBl does not support the blocks `##TT` and `##E` providing respectively a special
description of the two-winding transformers and the scheduled active power exchange between countries.
Those blocks are ignored during the parsing step.

(ucte-import-options)=
## Options
Parameters for the import can be defined in the configuration file in the
[import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

### `ucte.import.ucte.import.combine-phase-angle-regulation`
The `ucte.import.ucte.import.combine-phase-angle-regulation` property is an optional property that defines if
the ratio and phase tap changers of transformers should, when both are present, be combined.

The default value is `false`.

### `ucte.import.create-areas`
The `ucte.import.create-areas` property is an optional property that defines if
[areas](../../grid_model/network_subnetwork.md#area) should be created in the imported IIDM grid model.

The default value is `true`. For more details see further below about [area conversion](#area-conversion).

### `ucte.import.areas-dc-xnodes`
The `ucte.import.areas-dc-xnodes` property is an optional property that defines the list of X-nodes that should be
considered as [area](../../grid_model/network_subnetwork.md#area) DC boundaries.

The default value is an empty list. For more details see further below about [area conversion](#area-conversion).

(ucte-inconsistency-checks)=
## Inconsistency checks

Once the UCTE grid model is created in memory, a consistency check is performed on the different elements (nodes, lines, two-winding transformers and regulations).

In the tables below, we summarize the inconsistency checks performed on the network for each type of equipment.
For the sake of clarity, we use notations that are made explicit before each table.

### Nodes with active or reactive power generation

Notations:
- $P$: active power generation, in MW
- $Q$: reactive power generation, in MVar
- $minP$: minimum permissible active power generation in MW
- $maxP$: maximum permissible active power generation in MW
- $minQ$: minimum permissible reactive power generation in MVar
- $maxQ$: maximum permissible reactive power generation in MVar
- $Vreg$: voltage regulation, which can be enabled or disabled
- $Vref$: voltage reference, in V

|                          Check                           |           Consequence            |
|:--------------------------------------------------------:|:--------------------------------:|
|             $Vreg$ enabled and $Q$ undefined             |             $Q = 0$              |
|                      $P$ undefined                       |             $P = 0$              |
|                      $Q$ undefined                       |             $Q = 0$              |
| $Vreg$ enabled and $Vref$ undefined or $\in [0, 0.0001[$ |          Node type = PQ          |
|                     $minP$ undefined                     |          $minP = -9999$          |
|                     $maxP$ undefined                     |          $maxP = 9999$           |
|                     $minQ$ undefined                     |          $minQ = -9999$          |
|                     $maxQ$ undefined                     |          $maxQ = 9999$           |
|                      $minP > maxP$                       |   $minP$ switched with $maxP$    |
|                      $minQ > maxQ$                       |   $minQ$ switched with $maxQ$    |
|                        $P > maxP$                        |            $maxP = P$            |
|                        $P < minP$                        |            $minP = P$            |
|                        $Q > maxQ$                        |            $maxQ = Q$            |
|                        $Q < minQ$                        |            $minQ = Q$            |
|                      $minP = maxP$                       | $maxP = 9999$ and $minP = -9999$ |
|                      $minQ = maxQ$                       | $maxQ = 9999$ and $minQ = -9999$ |
|                      $maxP > 9999$                       |          $maxP = 9999$           |
|                      $minP < -9999$                      |          $minP = -9999$          |
|                      $maxQ > 9999$                       |          $maxQ = 9999$           |
|                      $minQ < -9999$                      |          $minQ = -9999$          |

### Lines or two-winding transformers

Notations:
- $X$: reactance in $\Omega$

|         Check         |         Consequence         |
|:---------------------:|:---------------------------:|
| $X \in [-0.05, 0.05]$ |       $X = \pm 0.05$        | 
|  Current limit $<0$   | Current limit value ignored |

### Regulations

#### Phase regulation

|                                 Check                                  |       Consequence        |
|:----------------------------------------------------------------------:|:------------------------:|
|                          Voltage value $<=0$                           | Voltage value set to NaN |
| Invalid ($n = 0$) or undefined ($n$, $n'$ or $\delta U$) data provided |    Regulation ignored    |

#### Angle regulation

|                                 Check                                  |    Consequence     |
|:----------------------------------------------------------------------:|:------------------:|
| Invalid ($n = 0$) or undefined ($n$, $n'$ or $\delta U$) data provided | Regulation ignored |
|                             Undefined type                             | type set to `ASYM` |

## From UCTE to IIDM
The UCTE file name is parsed to extract metadata required to initialize the IIDM network, such as its ID, the case date and the forecast distance.

(ucte-node-conversion)=
### Node conversion
There is no equivalent [voltage level](../../grid_model/network_subnetwork.md#voltage-level) or [substation](../../grid_model/network_subnetwork.md#substation) concept in the UCTE-DEF format,
so we have to create substations and voltage levels from the node description and the topology.
Two nodes are in the same substation if they have the same geographical spot (the 1st-6th character of the node code)
or are connected by a two-winding transformer, a coupler or a low-impedance line.
Two nodes are in the same voltage level if their code only differs by the eighth character (busbars identifier).  
**Note:** We do not create a substation, a voltage level or a bus for X-nodes. They are converted to [dangling lines](../../grid_model/network_subnetwork.md#dangling-line).

For nodes with a valid active or reactive load, a [load](../../grid_model/network_subnetwork.md#load) is created.
Its ID is equal to the ID of the bus post-fixed by `_load`.
The `P0` and `Q0` are equal to the active load and the reactive load of the UCTE node.
For those with a valid generator, a [generator](../../grid_model/network_subnetwork.md#generator) is created. Its ID is equal to the ID of the bus post-fixed by `_generator`.
The power plant type is converted to an [energy source]() value (see the mapping table below for the matching).

**Mapping table for power plant types:**

| UCTE Power plant type | IIDM Energy source |
|:---------------------:|:------------------:|
|       Hydro (H)       |       Hydro        |
|      Nuclear (N)      |      Nuclear       |
|      Lignite (L)      |      Thermal       |
|     Hard coal (C)     |      Thermal       |
|        Gas (G)        |      Thermal       | 
|        Oil (O)        |      Thermal       |
|       Wind (W)        |        Wind        |
|      Further (F)      |       Other        |

The list of the power plant types is more detailed than the list of available [energy source]() types in IIDM,
so we add the `powerPlantType` property to each generator to keep the initial value.

(ucte-line-conversion)=
### Line conversion
The busbar couplers connecting two real nodes are converted into a [switch](../../grid_model/network_subnetwork.md#breakerswitch). This switch is open or closed depending
on the status of the coupler. In the UCTE-DEF format, the coupler can have extra information not supported in IIDM we keep
as properties:
- the current limits is stored in the `currentLimit` property
- the order code is stored in the `orderCode` property
- the element name is stored in the `elementName` property

The lines connected between two real nodes are converted into an [AC line](../../grid_model/network_subnetwork.md#line), except if its impedance is too small (e.g. smaller than `0.05`).
In that particular case, the line is considered as a busbar coupler, and a [switch](../../grid_model/network_subnetwork.md#breakerswitch) is created.
The susceptance of the UCTE line is split in two, to initialize `B1` and `B2` with equal values.
If the current limits are defined, a permanent limit is created for both ends of the line.
The element name of the UCTE line is stored in the `elementName` property.

The lines connected between a read node and an X-node are converted into a [dangling line](../../grid_model/network_subnetwork.md#dangling-line).
In IIDM, a dangling line is a line segment connected to a constant load.
The sum of the active load and generation (rep. reactive) is computed to initialize the `P0` (resp. `Q0`) of the dangling line.
The element name of the UCTE line is stored in the `elementName` property and the geographical name of the X-node is stored in the `geographicalName` property.

(ucte-two-winding-transformer-conversion)=
### Two-winding transformer conversion
The two-winding transformers connected between two real nodes are converted into a [two-winding transformer](../../grid_model/network_subnetwork.md#two-winding-transformer).
If the current limits are defined, a permanent limit is created only for the second side.
The element name of the transformer is stored in the `elementName` property and the nominal power is stored in the `nominalPower` property.

If a two-winding transformer is connected between a real node and an X-node, a fictitious intermediate voltage level is created,
with a single bus called a Y-node. This new voltage level is created in the same substation as the real node.
The transformer is created between the real node and the new Y-node, and the X-node is converted into a dangling line.
The only difference with a classic X-node conversion, is that the electrical characteristic are hold by the transformer and set to `0` for the dangling line,
except for the reactance that is set to $0.05\space\Omega$.

**TODO**: insert a schema

(ucte-phase-regulation-conversion)=
#### Phase regulation
If a phase regulation is defined for a transformer, it is converted into a [ratio tap changer](../../grid_model/additional.md#ratio-tap-changer).
If the voltage setpoint is defined, the ratio tap changer will regulate the voltage to this setpoint.
The regulating terminal is assigned to the first side of the transformer.
The &rho; of each step is calculated according to the following formula: $\rho = 1 / (1 + i * \delta U / 100)$.

(ucte-angle-regulation-conversion)=
#### Angle regulation
If an angle regulation is defined for a transformer, it is converted into a [phase tap changer](../../grid_model/additional.md#phase-tap-changer), with a `CURRENT_LIMITER` regulation mode with `regulating=false`.
&rho; and &alpha; of each step are calculated according to the following formulas:
- for an asymmetrical regulation (e.g. $\Theta \ne 90°$)

$$
\begin{array}{ccl}
dx & = & step_i \times \dfrac{\delta U}{100} \times \cos(\Theta) \\[1em]
dy & = & step_i \times \dfrac{\delta U}{100} \times \sin(\Theta) \\[1em]
\rho & = & \dfrac{1}{\sqrt{dy^2 + (1 + dx)^2}} \\[1em]
\alpha & = & - \text{atan2}(dy, 1 + dx) \\[1em]
\end{array}
$$

- for a symmetrical regulation (e.g. $\Theta = 90°$)

$$
\begin{array}{ccl}
dx & = & step_i \times \dfrac{\delta U}{100} \times \cos(\Theta) \\[1em]
dy & = & step_i \times \dfrac{\delta U}{100} \times \sin(\Theta) \\[1em]
\rho & = & 1 \\[1em]
\alpha & = & - 2 \times \text{atan2}(dy, 2 \times (1 + dx)) \\[1em]
\end{array}
$$

**Note:** The sign of $\alpha$ is changed because the phase tap changer is on side 2 in UCTE-DEF, and on side 1 in IIDM.

(ucte-area-conversion)=
### Area conversion

When the [`ucte.import.create-areas` option](#ucteimportcreate-areas) is set to `true` (default), the importer will create
[areas](../../grid_model/network_subnetwork.md#area) in the IIDM Network.

The areas are created on the basis of the countries present in the input UCTE-DEF file.
Each country present results in the creation of an Area in the IIDM model with:
- Area **ID**: set to the country alpha-2 code
- Area **Type**: hardcoded to `ControlArea`
- Area **VoltageLevels**: all VoltageLevels created in the Country (see [Node conversion](#node-conversion))
- Area **Boundaries**: all DanglingLines in the Country

By default, all Area Boundaries are flagged as AC, because the UCTE-DEF format does not have any HVDC explicit description.
To specify which boundaries should be considered as DC in the conversion, you may supply a list of X-nodes IDs in the 
[`ucte.import.areas-dc-xnodes` option](#ucteimportareas-dc-xnodes).

**Note:** Creating areas for German subregions / TSOs is not supported today (D2, D4, D7, D8).
Let us know if you have this use case by entering an issue in our GitHub. We also welcome contributions.
