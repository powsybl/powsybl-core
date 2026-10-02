# Export

The export of an IIDM grid model to a UCTE-DEF file is a direct conversion: every supported network element is converted
into its UCTE-DEF equivalent. Further information about the format can be found in the UCTE-DEF
[format specification](format_specification.md).

## Limitations

**Supported equipments**: The export fails with a `UcteException` if the network contains any of the following elements,
which have no UCTE-DEF equivalent: shunt compensators, static VAR compensators, batteries, LCC or VSC converter
stations, HVDC lines, or three-winding transformers.

**Follows UCTE-DEF import**: Export is possible if the file was imported with the same format. For instance, if you
import a UCTE-DEF file in PowSyBl, you can update some elements and then export it back to UCTE-DEF format. However,
exporting to UCTE-DEF format a file imported from another format most often leads at best to incorrect file content, at
worst to an exporter failure. Some examples are listed here after.

**Transformers Tap Changers**: UCTE-DEF format assumes neutral tap position to be centered at position zero. If your
IIDM model contains tap changers not fitting this requirement, the export will not fail but the resulting UCTE file will
be incorrect without warning.

## Options

These properties can be defined in the configuration file in
the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value)
module.

**ucte.export.naming-strategy**<br>
The `ucte.export.naming-strategy` property is an optional property that defines the naming strategy to be used for UCTE
export.

Default naming strategy (`Default`) expects the network elements' ID to be totally compatible with UCTE-DEF norm (e.g.,
a network initially imported from a UCTE-DEF file), and throws an exception if any network element does not respect the
norm. It does not do any ID modification.

**ucte.export.combine-phase-angle-regulation**<br>
The `ucte.export.combine-phase-angle-regulation` property is an optional property that defines, for a two-winding
transformer that has both a ratio and a phase tap changer, whether the ratio tap changer's current step should be folded
into the exported angle regulation δu. This only applies to `ASYM` angle regulations. See
[angle regulation](#angle-regulation) below.

Its default value is `false`.

## From IIDM to UCTE

### Node conversion

Every bus of the network's [bus/breaker view](../../grid_model/network_subnetwork.md#voltage-level) is converted into a
UCTE node, using the naming strategy to compute its UCTE node code. If several loads or generators are connected to the
bus, their injections are aggregated
(see [Aggregation of several generators and loads](#aggregation-of-several-generators-and-loads)).

The table below maps every UCTE-DEF node attribute to its source in IIDM. Unless stated otherwise, an attribute with no
IIDM source is left undefined in the exported node.

| UCTE-DEF attribute                                   | Source in IIDM                                                                                   | Computation                                                                                                                                                                                                                                               |
|------------------------------------------------------|--------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Node code                                            | Bus                                                                                              | Computed by the [naming strategy](#options).                                                                                                                                                                                                              |
| Geographical name                                    | Bus `geographicalName` property                                                                  | Used as-is.                                                                                                                                                                                                                                               |
| Node status                                          | Bus fictitiousness                                                                               | `0` (real node) if the bus is not fictitious, `1` (equivalent node) otherwise, following the [UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf)'s node status convention. |
| Node type                                            | Connected generators' voltage regulation; whether the bus is the network's slack bus             | Derived from the connected generators' voltage regulation and whether the bus is the network's slack bus. Further information in [Node type](#node-type).                                                                                                 |
| Voltage reference (kV)                               | Connected generators' target voltage                                                             | Selected among the generators' targets; left undefined if there is no usable target. See [Voltage reference selection](#voltage-reference-selection).                                                                                                     |
| Active load (MW)                                     | Connected loads' `P0`                                                                            | Sum of the `P0` of the connected loads; `0` if there is no load.                                                                                                                                                                                          |
| Reactive load (MVar)                                 | Connected loads' `Q0`                                                                            | Sum of the `Q0` of the connected loads; `0` if there is no load.                                                                                                                                                                                          |
| Active power generation (MW)                         | Connected generators' target active power (`targetP`)                                            | Opposite of the sum of the `targetP` of the connected generators (UCTE-DEF convention: generation is negative); undefined values are ignored; `0` if there is no generator.                                                                               |
| Reactive power generation (MVar)                     | Connected generators' target reactive power                                                      | Opposite of the sum of the target reactive power of the connected generators; undefined values are ignored; `0` if there is no generator.                                                                                                                 |
| Minimum permissible active power generation (MW)     | Connected generators' `minP`                                                                     | Opposite of the sum of the `minP`; left undefined if the limit is out of bounds. See [Active and reactive power generation limits](#active-and-reactive-power-generation-limits).                                                                         |
| Maximum permissible active power generation (MW)     | Connected generators' `maxP`                                                                     | Opposite of the sum of the `maxP`; left undefined if the limit is out of bounds. See [Active and reactive power generation limits](#active-and-reactive-power-generation-limits).                                                                         |
| Minimum permissible reactive power generation (MVar) | Connected generators' reactive limits, each evaluated at the generator's own target active power | Opposite of the sum of the minimum reactive limits; left undefined if the limit is out of bounds. See [Active and reactive power generation limits](#active-and-reactive-power-generation-limits).                                                        |
| Maximum permissible reactive power generation (MVar) | Connected generators' reactive limits, each evaluated at the generator's own target active power | Opposite of the sum of the maximum reactive limits; left undefined if the limit is out of bounds. See [Active and reactive power generation limits](#active-and-reactive-power-generation-limits).                                                        |
| Static of primary control (%)                        | *(none)*                                                                                         | Always left undefined.                                                                                                                                                                                                                                    |
| Nominal power for primary control (MW)               | *(none)*                                                                                         | Always left undefined.                                                                                                                                                                                                                                    |
| Three-phase short-circuit power (MVA)                | *(none)*                                                                                         | Always left undefined.                                                                                                                                                                                                                                    |
| X/R ratio                                            | *(none)*                                                                                         | Always left undefined.                                                                                                                                                                                                                                    |
| Power plant type                                     | Connected generators' `powerPlantType` property, or their energy source                          | Derived from the generators' `powerPlantType` property or energy source; `F` if they differ. Further information in [Power plant type](#power-plant-type).                                                                                                |

#### Node type

The node type is `0` (PQ node) by default. It is set to `2` (PU node) if the bus has at least one connected generator
with voltage regulation on. It is set to `3` (global slack node) if the bus is the network's
[slack bus](../../grid_model/extensions.md#slack-terminal), which takes precedence over the PU case. `1` (Q and θ
constant) is never produced by the export. See the
[UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf)
for the full node type convention.

#### Power plant type

The `powerPlantType` property of each connected generator (typically set at import time) is used if present; otherwise,
the energy source of the generator is converted to a UCTE power plant type according to the following table:

| IIDM Energy source | UCTE Power plant type |
|:------------------:|:---------------------:|
|       Hydro        |      `H` (hydro)      |
|      Nuclear       |     `N` (nuclear)     |
|      Thermal       |      `C` (Coal)       |
|        Wind        |      `W` (Wind)       |
|   Other sources    |     `F` (further)     |

If the generators of a bus do not all have the same power plant type, type `F` is used.

See the convention for power plant types in
[UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf):

#### Aggregation of several generators and loads

Several loads or generators can be connected to the same bus. They are exported as a single UCTE-DEF node, with the
rules described in [Node conversion](#node-conversion): loads, active and reactive generation are summed, the limits are
aggregated as described in [Active and reactive power generation limits](#active-and-reactive-power-generation-limits),
the power plant type is `F` if the generators' types differ, and the node is a PU node if at least one generator
regulates voltage.

#### Active and reactive power generation limits

In a UCTE-DEF file, the minimum and maximum permissible generation of a node can be left empty, meaning that the limit
is not defined. When such a file is imported, PowSyBl sets the missing limits to `-9999` (minimum) and `9999`
(maximum). CGMES conversion implements a similar behavior, using a very large value to signify "no limit" (actually, the
largest encodable: `Double.MAX_VALUE`).

On export, a limit is left empty if it is outside of bounds `-9999` and `9999` (included):

- a minimum limit lower than or equal to `-9999` is left undefined,
- a maximum limit greater than or equal to `9999` is left undefined.

If a bus has several generators, each of the four limits (`minP`, `maxP`, minimum and maximum reactive limits) is
aggregated independently. The reactive limits of each generator are evaluated at its own target active power.

- If the limit of any generator is out of bounds ("no limit"), the aggregated limit is left undefined. This is checked
  before summing: a `9999` value never takes part in a sum.
- Otherwise, the aggregated limit is the sum of the generators' limits. If this sum is itself out of bounds, the limit
  is left undefined and a warning is reported.

##### Voltage reference selection

A UCTE-DEF node has a single voltage reference. When several generators are connected to a bus, the target voltage of
one of them has to be selected for the exported node. Each generator provides at most one candidate target voltage, then
one candidate is kept.

A voltage-regulating generator provides:

- its local target voltage (`localTargetV`), if defined (case *local*);
- otherwise, the target value of its voltage regulation, if defined:
  - used as-is if its regulating terminal is on the exported bus (case *local*),
  - rescaled if its regulating terminal is on another bus (case *remote*):

$$
V_{ref} = V_{target} \times \dfrac{V_{nom,local}}{V_{nom,remote}}
$$

where $V_{target}$ is the target value of the generator's voltage regulation (in kV, at the regulated bus),
$V_{nom,local}$ the nominal voltage of the exported bus and $V_{nom,remote}$ the nominal voltage of the regulated bus.
This keeps the per-unit target, and is an approximation which ignores transformer ratios and voltage drops. A warning is
[reported](#reporting) if the selected candidate is rescaled.

A generator that does not regulate voltage provides its local target voltage, if defined (case *non-regulating*). This
keeps the voltage reference of a PQ node imported from a UCTE-DEF file.

The candidates of the first non-empty case are considered, in this order: local, remote, non-regulating. Among them,
the one of the generator with the largest target active power is kept (ties are broken by the smallest generator id). A
warning is reported if the candidates have different values. If there is no candidate, the voltage reference is left
undefined; if the node is a PU node, a warning is reported.

### Line conversion

Every [switch](../../grid_model/network_subnetwork.md#breakerswitch) (as a busbar
coupler), [line](../../grid_model/network_subnetwork.md#line),
unpaired [boundary line](../../grid_model/network_subnetwork.md#boundary-line),
and [tie line](../../grid_model/network_subnetwork.md#tie-line)
of the network is converted into one or more UCTE lines, using the naming strategy to compute each line's node codes and
order code. A boundary line or a tie line additionally creates an [X-node](#x-nodes).

The table below maps every UCTE-DEF line attribute to its source in IIDM. Unless stated otherwise, an attribute with no
IIDM source is left undefined in the exported line.

| UCTE-DEF attribute               | Source in IIDM                                                                                                         | Computation                                                                                                                         |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| Node codes (1, 2) and order code | Id of the switch, line, or boundary line (each side's boundary line id, for a tie line)                                | Computed by the [naming strategy](#options) from the element's id.                                                                  |
| Status                           | Switch open/closed state; branch/boundary line fictitiousness and connection state; boundary line `isCoupler` property | See [Line status](#line-status) below.                                                                                              |
| Resistance R (Ω)                 | Line's/boundary line's `r`                                                                                             | Used as-is; `0` for a busbar coupler (switch).                                                                                      |
| Reactance X (Ω)                  | Line's/boundary line's `x`                                                                                             | Used as-is; `0` for a busbar coupler (switch).                                                                                      |
| Susceptance B (μS)               | Line's `b1`/`b2`; boundary line's `b`                                                                                  | `0` for a busbar coupler (switch); sum of `b1` and `b2` for a line; the boundary line's `b` for a boundary line or a tie line side. |
| Current limit I (A)              | Switch's `currentLimit` property; line's/transformer's/boundary line's permanent current limit(s)                      | See [Current limit](#current-limit) below.                                                                                          |
| Element name                     | `elementName` property                                                                                                 | Used as-is; left undefined if the property is absent.                                                                               |

#### Line status

The status follows the
[UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf)
's convention for element status:

- `0`: real element in operation
- `8`: real element out of operation
- `1`: equivalent element in operation
- `9`: equivalent element out of operation
- `2`: busbar coupler in operation
- `7`: busbar coupler out of operation

It is derived as follows:

- **Switch**: `2` if closed, `7` if open.
- **Line**, **two-winding transformer**, and each side of a **tie line**: `0`/`8` (real) if not fictitious, `1`/`9`
  (equivalent) if fictitious; the `_IN_OPERATION` variant if the branch's two terminals (or the tie line side's single
  terminal) are connected, the `_OUT_OF_OPERATION` variant otherwise.
- **Boundary line**: if its `isCoupler` property is `true`, it is exported as a busbar coupler, `2` if its terminal is
  connected, `7` otherwise; if not, it follows the same real/equivalent convention as a line, based on its
  fictitiousness and terminal connection state.

#### Current limit

- **Switch**: taken from the `currentLimit` property if it can be parsed as an integer; otherwise left undefined, and a
  warning is [reported](#reporting).
- **Line** and **two-winding transformer**: if a permanent current limit is defined on both sides, the smaller of the
  two is used; otherwise, the one that is defined is used, if any; left undefined if neither side has one.
- **Boundary line** (and each side of a tie line): the boundary line's own permanent current limit, if defined; left
  undefined otherwise.

#### X-nodes

An unpaired boundary line is converted into an X-node and a UCTE line connecting it to the corresponding real node. A
tie line is converted into an X-node and two UCTE lines, one for each side, using the same rules as for a standalone
boundary line.

The X-node is a UCTE node; the table below maps every UCTE-DEF node attribute to its source for an X-node, following the
same conventions as [node conversion](#node-conversion) above.

| UCTE-DEF attribute                                     | Source in IIDM                                                            | Computation                                                                                                                                                                                                             |
|--------------------------------------------------------|---------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Node code                                              | Boundary line's pairing key, or id if it has none (tie line: pairing key) | Computed by the [naming strategy](#options).                                                                                                                                                                            |
| Geographical name                                      | Boundary line's `geographicalName` property                               | Used as-is; for a tie line, merged from both boundary lines, see [Tie line properties](#tie-line-properties).                                                                                                           |
| Node status                                            | Boundary line's `status_XNode` property                                   | `EQUIVALENT` if set to `EQUIVALENT`, `REAL` otherwise; for a tie line, merged from both boundary lines, see [Tie line properties](#tie-line-properties).                                                                |
| Node type                                              | Unpaired boundary line's generation part voltage regulation               | `PU` if the generation part regulates voltage, `PQ` otherwise; always `PQ` for a tie line's X-node.                                                                                                                     |
| Voltage reference (kV)                                 | Unpaired boundary line's generation part target voltage                   | Used as-is, only if the generation part regulates voltage; left undefined otherwise, and always for a tie line's X-node.                                                                                                |
| Active load (MW)                                       | Unpaired boundary line's `P0`                                             | Used as-is; `0` for a tie line's X-node.                                                                                                                                                                                |
| Reactive load (MVar)                                   | Unpaired boundary line's `Q0`                                             | Used as-is; `0` for a tie line's X-node.                                                                                                                                                                                |
| Active power generation (MW)                           | Unpaired boundary line's generation part target active power              | Opposite sign; `0` if undefined, or for a tie line's X-node.                                                                                                                                                            |
| Reactive power generation (MVar)                       | Unpaired boundary line's generation part target reactive power            | Opposite sign; `0` if undefined, or for a tie line's X-node.                                                                                                                                                            |
| Min./max. permissible active/reactive power generation | Unpaired boundary line's generation part min/max limits                   | Same rules as for a node, see [Active and reactive power generation limits](#active-and-reactive-power-generation-limits); only set if the generation part regulates voltage; always undefined for a tie line's X-node. |
| Static of primary control (%)                          | *(none)*                                                                  | Always undefined.                                                                                                                                                                                                       |
| Nominal power for primary control (MW)                 | *(none)*                                                                  | Always undefined.                                                                                                                                                                                                       |
| Three-phase short-circuit power (MVA)                  | *(none)*                                                                  | Always undefined.                                                                                                                                                                                                       |
| X/R ratio                                              | *(none)*                                                                  | Always undefined.                                                                                                                                                                                                       |
| Power plant type                                       | *(none)*                                                                  | Always undefined.                                                                                                                                                                                                       |

#### Tie line properties

A tie line originates from 2 boundary lines. _Geographical name_ and _element name_ are values carried in IIDM by custom
properties (further information on how they are sourced in [UCTE import](./import.md#line-conversion)). Although we
expect both sides of the line to carry the same value for the same property, there can be discrepancies.

- If both boundary lines have the same value, it is used in the exported tie line.
- If one of the boundary lines has an empty value, the other boundary line's value is used.
- If both boundary lines have different values, the exported tie line field remains empty.

### Two-winding transformer conversion

Every [two-winding transformer](../../grid_model/network_subnetwork.md#two-winding-transformer) of the network is
converted into a UCTE transformer.

The table below maps every UCTE-DEF transformer attribute to its source in IIDM.

| UCTE-DEF attribute                          | Source in IIDM                                      | Computation                                                                                            |
|---------------------------------------------|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| Node codes (1, 2) and order code            | Transformer id                                      | Computed by the [naming strategy](#options) from the transformer's id.                                 |
| Status                                      | Transformer fictitiousness and connection state     | See [Line status](#line-status).                                                                       |
| Resistance R (Ω)                            | Transformer's `r`                                   | Used as-is.                                                                                            |
| Reactance X (Ω)                             | Transformer's `x`                                   | Used as-is.                                                                                            |
| Susceptance B (μS)                          | Transformer's `b`                                   | Used as-is.                                                                                            |
| Current limit I (A)                         | Transformer's permanent current limits (both sides) | See [Current limit](#current-limit).                                                                   |
| Element name                                | `elementName` property                              | Used as-is; left undefined if the property is absent.                                                  |
| Rated voltage 1 (kV, non-regulated winding) | Transformer's `ratedU2`                             | Used as-is; swapped with rated voltage 2, see [Rated voltages](#rated-voltages).                       |
| Rated voltage 2 (kV, regulated winding)     | Transformer's `ratedU1`                             | Used as-is; swapped with rated voltage 1, see [Rated voltages](#rated-voltages).                       |
| Nominal power (MVA)                         | `nomimalPower` property                             | Parsed as a double; left undefined if the property is absent, and a warning is [reported](#reporting). |
| Conductance G (μS)                          | Transformer's `g`                                   | Used as-is.                                                                                            |

#### Rated voltages

Because UCTE-DEF has the regulated winding on side 2, while IIDM has it on side 1, the rated voltages are swapped on
export: the UCTE transformer's rated voltage 1 (non-regulated winding) is the transformer's `ratedU2`, and its rated
voltage 2 (regulated winding) is its `ratedU1`.

If the transformer has a ratio and/or a phase tap changer, a regulation is exported.

#### Phase regulation

If the transformer has a [ratio tap changer](../../grid_model/additional.md#ratio-tap-changer), it is converted into a
phase regulation.

The table below maps every UCTE-DEF phase regulation attribute to its source in IIDM.

| UCTE-DEF attribute       | Source in IIDM                                 | Computation                                                                                                                       |
|--------------------------|------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| δu (%)                   | Ratio tap changer's tap steps' ρ               | Computed from the ρ of the two extreme taps, see formula in [δu formula](#δu-formula).                                            |
| n (number of taps)       | Ratio tap changer's high tap position          | Used as-is. This assumes the tap changer's neutral position is `0`, per the [Transformers Tap Changers](#limitations) limitation. |
| n' (tap position)        | Ratio tap changer's current tap position       | Used as-is. Same assumption as above.                                                                                             |
| Voltage set point U (kV) | Ratio tap changer's target voltage (`targetV`) | Used as-is; left undefined if the ratio tap changer has no target voltage.                                                        |

##### δu formula

$$
\delta u = 100 \times \left (\dfrac{1}{\rho_{max}} - \dfrac{1}{\rho_{min}}\right) / (n - 1)
$$

where $n$ is the number of taps, $\rho_{min}$ the ρ of the lowest tap position and $\rho_{max}$ the ρ of the highest tap
position.

#### Angle regulation

If the transformer has a [phase tap changer](../../grid_model/additional.md#phase-tap-changer), it is converted into an
angle regulation.

The table below maps every UCTE-DEF angle regulation attribute to its source in IIDM.

| UCTE-DEF attribute      | Source in IIDM                            | Computation                                                                                                                                  |
|-------------------------|-------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Regulation type         | Phase tap changer's tap steps' ρ          | `SYMM` if the ρ of every tap is `1`, `ASYM` otherwise.                                                                                       |
| δu (%)                  | Phase tap changer's tap steps' α and/or ρ | See the formulas in [SYMM regulation δu formula](#symm-regulation-δu-formula) and [ASYM regulation δu formula](#asym-regulation-δu-formula). |
| Angle θ (°)             | Phase tap changer's tap steps' α and/or ρ | Fixed at `90°` for a `SYMM` regulation; see the [ASYM regulation δu formula](#asym-regulation-δu-formula) otherwise.                         |
| n (number of taps)      | Phase tap changer's high tap position     | Used as-is. This assumes the tap changer's neutral position is `0`, per the [Transformers Tap Changers](#limitations) limitation.            |
| n' (tap position)       | Phase tap changer's current tap position  | Used as-is. Same assumption as above.                                                                                                        |
| Regulation power P (MW) | Phase tap changer's `regulationValue`     | Opposite of `regulationValue`.                                                                                                               |

##### SYMM regulation δu formula

For a `SYMM` regulation, the angle is fixed at `90°` and the δu (%) is computed from the α of the two extreme taps:

$$
\delta u = 100 \times 2 \times \left (\tan\left (\dfrac{\alpha_{max}}{2}\right) - \tan\left (\dfrac{\alpha_{min}}{2}\right)\right) / (n - 1)
$$

##### ASYM regulation δu formula

For an `ASYM` regulation, the δu (%) and the angle are computed from the distance, in the complex plane, between the
points $\frac{1}{\rho} e^{-i\alpha}$ of the two extreme taps. If the [
`ucte.export.combine-phase-angle-regulation`](#options)
option is enabled and the transformer also has a ratio tap changer, the computed δu (%) is divided by the ρ of the ratio
tap changer's current step.

**Note:** the sign of α is inverted in both cases, because the phase tap changer is on side 2 in the
[UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf),
and on side 1 in IIDM.

## Reporting

When a [ReportNode](../../user/functional_logs/index.md) is provided to the export, one report node is created per
conversion step (buses and switches, boundary lines, lines, tie lines, transformers) and per exported file. The
following situations are reported:

| Situation                                                                                                    | Severity | See                                                                                         |
|--------------------------------------------------------------------------------------------------------------|----------|---------------------------------------------------------------------------------------------|
| A switch has no usable `currentLimit` property                                                               | `WARN`   | [Current limit](#current-limit)                                                             |
| A two-winding transformer has no usable nominal power                                                        | `WARN`   | [Two-winding transformer conversion](#two-winding-transformer-conversion)                   |
| A node regulates voltage but none of its generators provides a voltage target                                | `WARN`   | [Voltage reference selection](#voltage-reference-selection)                                 |
| The generators of a bus have different candidate voltage targets; the kept value and its generator are given | `WARN`   | [Voltage reference selection](#voltage-reference-selection)                                 |
| The kept voltage target comes from a remote regulation and is rescaled                                       | `WARN`   | [Voltage reference selection](#voltage-reference-selection)                                 |
| The sum of finite generator power limits reaches or exceeds `9999` in absolute value and is left undefined   | `WARN`   | [Active and reactive power generation limits](#active-and-reactive-power-generation-limits) |
| The generators of a bus have different power plant types and type `F` (further) is used                      | `INFO`   | [Power plant type](#power-plant-type)                                                       |
