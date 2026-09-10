# Export

The export of an IIDM grid model to a UCTE-DEF file is a direct conversion: every supported network element is converted
into its UCTE-DEF equivalent and written to a file using the [`SECOND`](format_specification.md) format version.

The export fails with a `UcteException` if the network contains any of the following elements, which have no UCTE-DEF
equivalent: shunt compensators, static VAR compensators, batteries, LCC or VSC converter stations, HVDC lines, or
three-winding transformers.

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

Other naming strategies can be defined, as long as they are provided as services implementing `NamingStrategy`. A
working example is the class `CounterNamingStrategy`.

**ucte.export.combine-phase-angle-regulation**<br>
The `ucte.export.combine-phase-angle-regulation` property is an optional property that defines, for a two-winding
transformer that has both a ratio and a phase tap changer, whether the ratio tap changer's current step should be folded
into the exported angle regulation δu. See [angle regulation](#angle-regulation) below.

Its default value is `false`.

## From IIDM to UCTE

### Node conversion

Every bus of the network's [bus/breaker view](../../grid_model/network_subnetwork.md#voltage-level) is converted into a
UCTE node, using the naming strategy to compute its UCTE node code.

**Node status**: The node status is `EQUIVALENT` if the bus is fictitious, `REAL` otherwise.

**Node type**: The node type is `UT` if the bus is the network's
[slack bus](../../grid_model/extensions.md#slack-terminal), `PU` if a connected generator regulates voltage, and `PQ`
otherwise.

**Note:** these are mnemonic names, not the single-digit codes actually written to the file. The complete node status
and node type code tables are documented in the [official UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf).

If the bus has one or several [loads](../../grid_model/network_subnetwork.md#load), their active and reactive powers are
summed to initialize the node's active and reactive load. If the bus has one or several
[generators](../../grid_model/network_subnetwork.md#generator), their active and reactive target powers are summed to
initialize the node's active and reactive power generation, and their minimum/maximum active and reactive power limits
are used to initialize the node's permissible power generation range. The energy source of the generator is converted to
a UCTE power plant type according to the following table, unless the generator has a `powerPlantType` property
(typically set at import time), in which case this property's value is used directly:

| IIDM Energy source | UCTE Power plant type |
|:------------------:|:---------------------:|
|       Hydro        |       Hydro (H)       |
|      Nuclear       |      Nuclear (N)      |
|      Thermal       |     Hard coal (C)     |
|        Wind        |       Wind (W)        |
|   Other sources    |      Further (F)      |

The letter in parentheses is the actual mnemonic written to the file. The full list of power plant type mnemonics
(including `L` for lignite, `G` for gas and `O` for oil, none of which is produced by this mapping) is documented in the
[official UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf).

### Line conversion

#### Busbar coupler conversion

Every [switch](../../grid_model/network_subnetwork.md#breakerswitch) of the network's bus/breaker view is converted into
a UCTE busbar coupler (a UCTE line with resistance, reactance and susceptance set to `0`). Its status is
`BUSBAR_COUPLER_IN_OPERATION` or `BUSBAR_COUPLER_OUT_OF_OPERATION` depending on whether the switch is closed or open
(see the [official UCTE-DEF specification](https://eepublicdownloads.entsoe.eu/clean-documents/pre2015/publications/ce/otherreports/UCTE-format.pdf)
for the corresponding element status codes). If the switch has a `currentLimit` property that can be parsed as an
integer, it is used as the current limit of the coupler. Otherwise, no current limit is set, and a warning is
[reported](#reporting).

#### Boundary line conversion

Every unpaired [boundary line](../../grid_model/network_subnetwork.md#boundary-line) of the network is converted into an
X-node and a UCTE line connecting it to the corresponding real node. The status of the X-node is `EQUIVALENT` if the
boundary line's `status_XNode` property is set to `EQUIVALENT`, `REAL` otherwise. The X-node's active and reactive load
are set from the boundary line's `P0` and `Q0`. If the boundary line's generation part regulates voltage, the X-node
type is set to `PU` and its voltage reference and permissible power generation range are set from the generation part,
otherwise its active and reactive power generation are set from the generation part's target powers. See
[node conversion](#node-conversion) above for the corresponding raw codes.

The UCTE line is created with the boundary line's resistance, reactance, susceptance and permanent current limit (if
defined).

#### Line conversion

Every [line](../../grid_model/network_subnetwork.md#line) of the network is converted into a UCTE line, with the same
resistance and reactance, and a susceptance equal to the sum of the line's `B1` and `B2`. If a permanent current limit
is defined on both sides of the line, the smaller of the two is used; otherwise, the one that is defined is used, if
any.

#### Tie line conversion

Every [tie line](../../grid_model/network_subnetwork.md#tie-line) of the network is converted into an X-node and two
UCTE lines, one for each side, using the same rules as for a standalone boundary line above. The X-node's geographical
name and status are derived from the corresponding property of the two boundary lines composing the tie line: if both
sides agree, that value is used; if only one side has a value, that value is used; if the two sides disagree, the
property is left empty on the X-node.

### Two-winding transformer conversion

Every [two-winding transformer](../../grid_model/network_subnetwork.md#two-winding-transformer) of the network is
converted into a UCTE transformer.

The nominal power is taken from the transformer's (otherwise deprecated) `nomimalPower` property.

If a permanent current limit is defined on both sides of the transformer, the smaller of the two is used; otherwise, the
one that is defined is used, if any.

If the transformer has a ratio and/or a phase tap changer, a regulation is exported.

#### Phase regulation

If the transformer has a [ratio tap changer](../../grid_model/additional.md#ratio-tap-changer), it is converted into a
phase regulation. If the ratio tap changer has a target voltage, it is exported as the regulation's voltage set point.
The δu (%) of the regulation is computed from the ρ of the two extreme taps:

$$
\delta u = 100 \times \left (\dfrac{1}{\rho_{max}} - \dfrac{1}{\rho_{min}}\right) / (n - 1)
$$

where $n$ is the number of taps, $\rho_{min}$ the ρ of the lowest tap position and $\rho_{max}$ the ρ of the highest tap
position.

#### Angle regulation

If the transformer has a [phase tap changer](../../grid_model/additional.md#phase-tap-changer), it is converted into an
angle regulation, with a regulation power $P = -RegulationValue$. The regulation type is `SYMM` if the ρ of every tap is
`1`, `ASYM` otherwise.

- For a `SYMM` regulation, the angle is fixed at `90°` and the δu (%) is computed from the α of the two extreme taps:

$$
\delta u = 100 \times 2 \times \left (\tan\left (\dfrac{\alpha_{max}}{2}\right) - \tan\left (\dfrac{\alpha_{min}}{2}\right)\right) / (n - 1)
$$

- For an `ASYM` regulation, the δu (%) and the angle are computed from the distance, in the complex plane, between the
  points $\frac{1}{\rho} e^{-i\alpha}$ of the two extreme taps. If the [`ucte.export.combine-phase-angle-regulation`](#options)
  option is enabled and the transformer also has a ratio tap changer, the computed δu (%) is divided by the ρ of the 
  ratio tap changer's current step.

**Note:** the sign of α is inverted in both cases, because the phase tap changer is on side 2 in UCTE-DEF, and on side 1
in IIDM.

## Reporting

When a [ReportNode](../../user/functional_logs/index.md) is provided to the export, one report node is created per
conversion step (buses and switches, boundary lines, lines, tie lines, transformers) and per exported file, and the
following situations are reported with a `WARN` severity:

- a switch has no usable `currentLimit` property (see [busbar coupler conversion](#busbar-coupler-conversion)),
- a two-winding transformer has no usable nominal power
  (see [two-winding transformer conversion](#two-winding-transformer-conversion)).
