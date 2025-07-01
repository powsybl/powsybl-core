# Import

The import module reads and converts a PSS®E power flow data file to the PowSyBl grid model. The current implementation supports RAW format for versions 33 and 35 and RAWX format for version 35. The import process is performed in three steps:
- Read the input file.
- Validate input data.
- Convert input data into PowSyBl grid model.

First, input data is obtained by reading and parsing the input file, and as a result, a PSS®E model is created in memory. This model can be viewed as a set of Java classes where each data block of the PSS®E model is associated with a specific Java class that describes all their attributes or data items. Then, some inconsistency checks are performed on this model. If the validation succeeds, the PSS®E model is converted to a PowSyBl grid model.

(psse-import-options)=
## Options
Parameters for the import can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**psse.import.ignore-base-voltage**  
The `psse.import.ignore-base-voltage` property is an optional property that defines if the importer should ignore the base voltage information present in the PSS®E file. The default value is `false`.

**psse.import.ignore-node-breaker-topology**  
The `psse.import.ignore-node-breaker-topology` property is an optional property that defines if the importer should ignore the node breaker information present in the PSS®E file. The default value is `false`.

(psse-inconsistency-checks)=
## Inconsistency checks
-<span style="color: red">TODO</span>

## Conversion

A PSS®E file specifies a Bus/Branch network model where typically there is a bus for each voltage level inside a substation and where substation objects are not explicitly defined. Breakers and switches were traditionally modeled as zero impedance lines with special identifiers. Since version 35 PSS®E supports explicit definition of substation data and switching devices at both system and substation level. However, this information is optional and may not be present in the case.

The PowSyBl grid model establishes the substation as a required container of voltage levels and equipment. The first step in the conversion process assigns a substation for each PSS®E bus, ensuring that all buses at transformer ends are kept in the same substation.

The current conversion uses explicit PSS®E substation information to group buses, considering zero-impedance branches, transformers, and substation membership as connectors. A new substation is created for each identified component. Within the substation, a new voltage level is created for each group of buses connected by zero-impedance branches or sharing the same nominal voltage. The topology of each voltage level can be defined at either the bus-branch level or the node-breaker level. It is only defined at the node-breaker level when all the buses included in the voltage level belong to the same PSS®E substation. In this case, all the detailed connectivity defined in PSS®E is imported into the PowSyBl model.

In the PowSyBl grid model, all the network components are identified through a global and unique alphanumeric identifier (**Id**). Optionally, they may receive a name (**Name**).

For each substation, the following attributes are defined:
- **Id** following one of the two patterns: `S<n>`, when the substation corresponds to a PSS®E substation (where `n` is the identifier of the PSS®E substation), and `S<n1-n2 ... -ni>`, when the substation represents a bus or a group of buses, where `n1...ni` are the identifiers of the buses sorted in ascending order.

Every voltage level is assigned to its corresponding substation, with attributes:
- **Id** following the pattern `VL<n1-n2 ... -ni>` where `n1 .. ni` represents the PSS®E bus numbers included inside the voltage level sorted in ascending order.
- **NominalV** Nominal voltage of the voltage level. Equal to `1` if `psse.import.ignore-base-voltage` property is `true`. Otherwise, it is assigned to the base voltage of one representative bus inside the voltage level, read from PSS®E field `BASKV`.
- **TopologyKind** Topology level assigned to the network model, `NODE_BREAKER` or `BUS_BREAKER`.

The following sections describe in detail how each supported PSS®E data block is converted to PowSyBl network model objects.


(psse-bus-data-conversion)=
### _Bus Data_

There is a one-to-one correspondence between the records of the PSS®E _Bus Data_ block and the buses of the PowSyBl network model. For each record in the _Bus Data_ block a PowSyBl bus is created and assigned to its corresponding voltage level with the following attributes:
- **Id** according to the pattern `B<n>` where `n` represents the PSS®E bus number (field `I` in the _Bus Data_ record).
- **Name** is copied from PSS®E field `NAME`.
- **V** is obtained from the PSS®E bus voltage magnitude, `VM`, multiplied by the nominal voltage of the corresponding voltage level.
- **Angle** is copied from the PSS®E bus voltage phase angle, `VA`.


(psse-load-data-conversion)=
### _Load Data_

Every _Load Data_ record represents one load. Multiple loads are allowed at the same bus by specifying one _Load Data_ record with the same bus and different load identifiers. Each record defines a new load in the PowSyBl grid model associated with its corresponding voltage level and with the following attributes:
- **Id** according to the pattern `<n>-L<m>` where `n` represents the PSS®E bus number (field `I` in the _Load Data_ record) and `m` is the PSS®E alphanumeric load identifier (field `ID` in the _Load Data_ record).
- **ConnectableBus** PowSyBl bus identifier assigned to the PSS®E bus number (field `I` in the _Load Data_ record).
- **P0** Active power. It is copied from PSS®E field `PL`.
- **Q0** Reactive power. It is copied from PSS®E field `QL`.

The load is connected to the ConnectableBus if load status (field `STATUS` in the _Load Data_ record) is `1` (In-service).

PSS®E supports loads with three different characteristics: Constant Power, Constant Current and Constant Admittance. The current version only takes into consideration the Constant Power component, discarding the Constant Current component (fields `IP` and `IQ` in the _Load Data_ record) and the Constant Admittance component (fields `YP` and `YQ` in the _Load Data_ record).


(psse-fixed-bus-shunt-data-conversion)=
### _Fixed Bus Shunt Data_

Each _Fixed Bus Shunt Data_ record defines a PowSyBl shunt compensator with a linear model and a single section. It is possible to define multiple fixed shunts at the same bus. The PowSyBl shunt compensator is associated with its corresponding voltage level and has the following attributes:

- **Id** according to the pattern `<n>-SH<m>` where `n` represents the PSS®E bus number (field `I` in the _Fixed Bus Shunt Data_ record) and `m` is the PSS®E alphanumeric shunt identifier (field `ID` in the _Fixed Bus Shunt Data_ record).
- **ConnectableBus** PowSyBl bus identifier assigned to the PSS®E bus number (field `I` in the _Fixed Bus Shunt Data_ record).
- **SectionCount** Always `1`.
- **gPerSection** Positive sequence shunt (charging) conductance per section. It is defined as `GL` / (`vnom` *`vnom`), where `GL` is the active component of shunt admittance to ground, entered in MW at one per unit voltage (field `GL` in the _Fixed Bus Shunt Data_ record) and `vnom` is the nominal voltage of the corresponding voltage level.
- **bPerSection** Positive sequence shunt (charging) susceptance per section. It is defined as `BL` / (`vnom` *`vnom`), where `BL` is the reactive component of shunt admittance to ground, entered in MVAR at one per-unit voltage (field `BL` in the _Fixed Bus Shunt Data_ record).
- **MaximumSectionCount** Always `1`.

The shunt compensator is connected to the ConnectableBus if fixed shunt status (field `STATUS` in the _Fixed Bus Shunt Data_ record) is `1` (In-service).


(psse-switched-shunt-data-conversion)=
### _Switched Shunt Data_

In the PSS®E version 33, only one switched shunt element can be defined on each bus. Version 35 allows multiple switched shunts on the same bus, adding an alphanumeric switched shunt identifier.

A switched shunt device may be a mix of reactors and capacitors; it is divided in blocks and steps: a block contains `n` steps of the same admittance. The steps and blocks can be adjusted to regulate a given magnitude: a voltage at a bus, a reactive power output or an admittance. Only voltage regulation is considered when mapping this equipment to PowSyBl.

There are two methods for defining how a switched shunt steps and blocks should be adjusted to keep the controlled magnitude between limits; the method chosen is determined by the field `ADJM` in the _Switched Shunt Data_ record. If `ADJM` is `0` steps and blocks are switched on in input order, and switched off in reverse output order. If `ADJM` is `1`, steps and blocks are switched on and off such that the next highest (or lowest, as appropriate) total admittance is achieved.

Each switched shunt record defines a PowSyBl shunt compensator with a non-linear model with the following attributes:

- **Id** according to the pattern `<n>-SwSH<m>` where `n` represents the PSS®E bus number (field `I` in the _Switched Shunt Data_ record) and `m` is the PSS®E alphanumeric shunt identifier (field `ID` in version 35 of the _Switched Shunt Data_ record, forced to `"1"` when importing version 33 data).
- **ConnectableBus** PowSyBl bus identifier assigned to the PSS®E bus number (field `I` in the _Switched Shunt Data_ record).
- **SectionCount** Defined as the section count (section index + 1) where section `B` is closer to the initial switched shunt admittance (field `BINIT` in the _Switched Shunt Data_ record).
- **TargetV** Voltage setpoint defined as `0.5` * (`VSWLO` + `VSWHI`) * `vnom`, where `VSWLO` is the controlled voltage lower limit (field `VSWLO` in the _Switched Shunt Data_ record) and `VSWHI` is the controlled voltage upper limit (field `VSWHI` in the _Switched Shunt Data_ record).
- **TargetDeadband** defined as (`VSWHI` - `VSWLO`) * `vnom`.
- **RegulatingTerminal** Regulating terminal assigned to the bus where voltage is controlled by this switched shunt (field `SWREM` in version 33 or field `SWREG` in version 35, both in the _Switched Shunt Data_ record if they are not `0`. Otherwise, field `I` in the _Switched Shunt Data_ record).
- **VoltageRegulatorOn** defined as `true` if the control mode is not `0` (field `MODSW` in the _Switched Shunt Data_ record) and `TargetV` is greater than `0.0`.

The shunt compensator is connected to the ConnectableBus if switched shunt status (field `STAT` in the _Switched Shunt Data_ record) is `1` (In-service).

The sections of the PowSyBl shunt compensator non-linear model are defined according to the adjustment method of the PSS®E switched shunt. In the PowSyBl model, the susceptance at each section is the accumulated susceptance obtained if the section and all previous ones are connected.

The attributes of each section in the PowSyBl shunt compensator non-linear model are defined as:
- **Section G** Positive sequence shunt (charging) conductance of this section. Always `0.0`.
- **Section B** Positive sequence shunt (charging) susceptance of this section. It is defined as `B` / (`vnom` *`vnom`), where `B` is the reactive component of shunt admittance to ground, entered in MVAR at one per-unit voltage assigned to this section and `vnom` is the nominal voltage of the corresponding voltage level.

When the adjustment method `ADJM` is `0`, the behaviour of the switched shunt can be mapped directly to the shunt compensator non-linear model with sections based on the switched shunt blocks/steps and its order in the PSS@E input record. A section is assigned to each step of the reactor and capacitor shunt blocks by accumulating the admittance of the corresponding steps that are in-service. Only the in-service switched shunt blocks are considered (field `SI` in version 35 of the _Switched Shunt Data_ record, always in-service in version 33). A section with 0.0 susceptance is added between sections assigned to reactor and capacitor blocks.

If the adjustment method `ADJM` is `1`, the reactor and capacitor blocks can be specified at any order, and all the switching combinations are considered in PSS®E. Current conversion does not support building a separate section for each switching combination. To map the PSS@E shunt blocks/steps into PowSyBl sections, first the reactor and capacitor blocks are increasingly ordered by susceptance (field `BI` in the _Switched Shunt Data_ record) and then sections are created like in the previous adjustment considering that blocks are switched on following the sorted order.

(psse-generator-data-conversion)=
### _Generator Data_

Every _Generator Data_ single line record represents one generator. Multiple generators are allowed at a PSS®E bus by specifying the same bus and a different identifier. Each record defines a new generator in the PowSyBl grid model associated with its corresponding voltage level and with the following attributes:
- **Id** according to the pattern `<n>-G<m>` where `n` represents the PSS®E bus number (field `I` in the _Generator Data_ record) and `m` is the PSS®E alphanumeric load identifier (field `ID` in the _Generator Data_ record).
- **ConnectableBus** PowSyBl bus identifier assigned to the PSS®E bus number (field `I` in the _Generator Data_ record).
- **TargetP** Active power. It is copied from PSS®E field `PG`.
- **MinP** Minimum generator active power. It is copied from PSS®E field `PB`.
- **MaxP** Maximum generator active power. It is copied from PSS®E field `PT`.
- **TargetQ** Reactive power. It is copied from PSS®E field `QG`.
- **MinQ** Minimum generator reactive power. It is copied from PSS®E field `QB`.
- **MaxQ** Maximum generator reactive power. It is copied from PSS®E field `QT`.
- **TargetV** Voltage setpoint defined as `VS` * `vnom`, where `VS` is the regulated voltage (field `VS` in the _Generator Data_ record) and `vnom` is the nominal voltage of the corresponding voltage level.
- **RegulatingTerminal** Regulating terminal assigned to the bus where voltage is controlled by this generator (field `IREG` in the _Generator Data_ record if it is not `0`. Otherwise, field `I` in the _Generator Data_ record).
- **VoltageRegulatorOn** defined as `true` if the type code of the associated bus is `2` or `3` (field `IDE` in the _Bus Data_ record) and `TargetV` is greater than `0.0` and `MaxQ` is greater than `MinQ`.

The generator is connected to the ConnectableBus if generator status (field `STAT` in the _Generator Data_ record) is `1` (In-service).

(psse-branch-data-conversion)=
### _Non-Transformer Branch Data_

In PSS®E each AC transmission line is represented as a non-transformer branch record and defines a new line in the PowSyBl grid model with the following attributes:
- **Id** according to the pattern `L-<n>-<m>-<p>` where `n` represents the PSS®E bus `1` number (field `I` in the _Non-Transformer Branch Data_ record), `m` represents the bus `2` number (field `J` in the _Non-Transformer Branch Data_ record) and `p` is the circuit identifier (field `CKT` in the _Non-Transformer Branch Data_ record).
- **ConnectableBus1** PowSyBl bus identifier assigned to the PSS®E bus `1` number (field `I` in the _Non-Transformer Branch Data_ record).
- **VoltageLevel1** PowSyBl voltage level assigned to the bus `1`.
- **ConnectableBus2** PowSyBl bus identifier assigned to the PSS®E bus `2` number (field `J` in the _Non-Transformer Branch Data_ record).
- **VoltageLevel2** PowSyBl voltage level assigned to the bus `2`.
- **R** Resistance defined as `R` * `vnom2` * `vnom2` / `sbase` where `R` is the resistance of the branch (field `R` in the _Non-Transformer Branch Data_ record), `vnom2` is the nominal voltage of the voltage level assigned to the bus `2` and `sbase` is the system MVA base (field `SBASE` in the _Case Identification Data_ record).
- **X** Reactance defined as `X` * `vnom2` * `vnom2` / `sbase` where `X` is the reactance of the branch (field `X` in the _Non-Transformer Branch Data_ record).
- **G1** Conductance of the line shunt at the bus `1` defined as (`GI` * `sbase`) / ( `vnom2` * `vnom2`) where `GI` is the conductance at the bus `1` (field `GI` in the _Non-Transformer Branch Data_ record).
- **B1** Susceptance defined as (( `0.5` * `B` + `BI` ) * `sbase`) / ( `vnom2` * `vnom2`) where `B` is the total branch charging susceptance (field `B` in the _Non-Transformer Branch Data_ record) and `BI` is the susceptance at the bus `1` (field `BI` in the _Non-Transformer Branch Data_ record).
- **G2** Conductance of the line shunt at the bus `2` defined as (`GJ` * `sbase`) / ( `vnom2` * `vnom2`) where `GJ` is the conductance at the bus `2` (field `GJ` in the _Non-Transformer Branch Data_ record).
- **B2** Susceptance defined as (( `0.5` * `B` + `BJ` ) * `sbase`) / ( `vnom2` * `vnom2`) where `B` is the total branch charging susceptance (field `B` in the _Non-Transformer Branch Data_ record) and `BJ` is the susceptance at the bus `2` (field `BJ` in the _Non-Transformer Branch Data_ record).

The line is connected at both ends if the branch status (field `ST` in the _Non-Transformer Branch Data_ record) is `1` (In-service).

A set of current permanent limits is defined as `1000.0` * `rateMva` / (`sqrt(3.0)` * `vnom1`) at the end `1` and `1000.0` * `rateMva` / (`sqrt(3.0)` * `vnom2`) at the end `2` where `rateMva` is the first rating of the branch (field `RATEA` in version 33 of the _Non-Transformer Branch Data_ record and field `RATE1` in version 35) and `vnom1` and `vnom2` are the nominal voltages of the associated voltage levels.

(psse-transformer-data-conversion)=
### _Transformer Data_

The _Transformer Data_ block defines two- and three-winding transformers. Two-winding transformers have four line records, while three-winding transformers have five line records. A `0` value in the field `K` of the _Transformer Data_ record first line is used to indicate that is a two-winding transformer, otherwise is considered a three-winding transformer.

PSS@E two-winding transformer records are mapped to two-winding transformers in the PowSyBl grid model. They are associated with corresponding voltage levels inside the same substation and defined with the following attributes:

- **Id** according to the pattern `T-<n>-<m>-<p>` where `n` represents the PSS®E bus `1` number (field `I` in the _Transformer Data_ record), `m` represents the bus `2` number (field `J` in the _Transformer Data_ record) and `p` is the circuit identifier (field `CKT` in the _Transformer Data_ record).
- **ConnectableBus1** PowSyBl bus identifier assigned to the PSS®E bus `1` number (field `I` in the _Transformer Data_ record).
- **VoltageLevel1** PowSyBl voltage level assigned to the bus `1`.
- **ConnectableBus2** PowSyBl bus identifier assigned to the PSS®E bus `2` number (field `J` in the _Transformer Data_ record).
- **VoltageLevel2** PowSyBl voltage level assigned to the bus `2`.
- **RatedU1** Rated voltage at the end `1`. The nominal voltage of the associated `voltageLevel` is assigned.
- **RatedU2** Rated voltage at the end `2`. The nominal voltage of the associated `voltageLevel` is assigned.
- **R** Transmission resistance.
- **X** Transmission reactance.
- **G** Shunt conductance.
- **B** Shunt susceptance.
- **TapChanger** Could be a ratio tap changer or a phase tap changer.
- **OperationalLimits** Current limits for both ends.

The transformer is connected at both ends if the branch status (field `STAT` in the _Transformer Data_ record) is `1` (In-service)

In PSS®E the transformer model allows to define a ratio and angle at the end `1` and only a fixed ratio at the end `2`. The transformer magnetizing admittance is modeled between the bus and the ratio of the end `1`. The PowSyBl grid model supports a ratioTapChanger and a phaseTapChanger at the end `1` and the magnetizing admittance is between the ratio and the transmission impedance.

![TwoWindingsTransformerModels](img/two-winding-transformer-model.svg){width="100%" align=center class="only-light"}
![TwoWindingsTransformerModels](img/dark_mode/two-winding-transformer-model.svg){width="100%" align=center class="only-dark"}

To express the PSS®E electric attributes of the transformer in the PowSyBl grid model, the following conversions are performed:

- The first step is to define the complex impedance between windings (`Z`) by using the resistance and reactance (fields `R1-2` and  `X1-2` in the _Transformer Data_ record), the winding base MVA (field `SBASE1-2` in the _Transformer Data_ record) and the system MVA base (field `SBASE` in the _Case Identification Data_ record) according to the code that defines the units in which the winding impedances `R1-2`, `X1-2` are specified (field `CZ` in the _Transformer Data_ record). Then the complex impedance (`Z`) is converted to engineering units using the nominal voltage of the voltage level at end `2` and the system MVA base. Finally, it should be adjusted after fixing an ideal ratio at end `2` and moving the configured ratio to the end `1`. The obtained result is assigned to the transmission resistance and reactance of the PowSyBl transformer.

- The complex shunt admittance `Ysh` is calculated using the transformer magnetizing admittance connected to ground at bus `1` (fields `MAG1` and  `MAG2` in the _Transformer Data_ record), the winding base MVA (field `SBASE1-2` in the _Transformer Data_ record), the system MVA base, the bus base voltage (field `BASKV` in the _Bus Data_ record) of the transformer bus `I` and the nominal (rated) winding `1` voltage base (field `NOMV1` in the _Transformer Data_ record) according to the magnetizing admittance code that defines the units in which `MAG1` and `MAG2` are specified (filed `CM` in the _Transformer Data_ record). The next step is to convert the complex `Ysh` to engineering units using the nominal voltage of the voltage level at end `2` and the system MVA base and finally the obtained value is assigned to the shunt conductance and susceptance of the PowSyBl transformer. The shunt conductance in the PowSyBl grid model is located after the ratio, so is necessary to add a step correction by each different ratio in the tabular `tapChanger`.

To define the `tapChanger` the first step is to calculate the complex ratio at end `1` and the ratio at end `2`. The ratio at end `1` is calculated using the winding ratio (field `WINDV1` in the _Transformer Data_ record), the nominal (rated) winding voltage base (field `NOMV1` in the _Transformer Data_ record) and the bus base voltage (field `BASKV` in the _Bus Data_ record) according to the code that defines the units in which the turns ratios are specified (field `CZ` in the _Transformer Data_ record). The angle at end `1` is copied from the winding phase shift angle (field `ANG1` in the _Transformer Data_ record). The ratio at end `2` is calculated in the same way as at end `1` but using the following fields (fields `WINDV2`, `NOMV1` in the _Transformer Data_ record) and the corresponding bus base voltage at bus `J`  (field `BASKV` in the _Bus Data_ record). <br>
Then a `tapChanger` at end `1` is defined by fixing one of the components of the complex ratio at end `1` and moving the other in each step using the number of tap positions available (field `NTP1` in the _Transformer Data_ record) and the upper and lower limits (fields `RMA1`, `RMI1` in the _Transformer Data_ record). <br>
Finally, the `tapChanger` is adjusted twice, after fixing an ideal ratio at end `2` by moving the current ratio to end `1` and after moving the shunt admittance adding in this last case a step correction for each step of the `tapChanger`. The tap for which the (ratio, angle) is closer to the complex ratio at end `1` is assigned as `tapPosition`. <br>
This current version does not consider the impedance correction table if this transformer impedance is to be a function of either off-nominal turns ratio or phase shift angle.

If the `tapChanger` is a `ratioTapChanger` and the transformer control mode (field `COD` in the _Transformer Data_ record) is `1` a voltage control with the following attributes is defined:
- **TargetV** Voltage setpoint defined as `0.5` * (`VMI` + `VMA`) * `vnom`, where `VMA` and `VMI` are the voltage upper and lower limits (fields `VMA1`, `VMA1` in the _Transformer Data_ record) and `vnom` is the nominal voltage of the voltageLevel at end `1`.
- **TargetDeadband** defined as (`VMA` - `VMI`) * `vnom`.
- **RegulatingTerminal** Regulating terminal assigned to the bus where voltage is controlled (field `CONT1` in the _Transformer Data_ record).
- **RegulatingOn** defined as `true` if `TargetV` and `TargetDeadBand` are greater than `0.0`.

If the PSS®E transformer is controlling the reactive power (field `COD` = `2` in the _Transformer Data_ record) the control is discarded as the current version of PowSyBl does not support reactive control for transformers.

When the `tapChanger` is a `phaseTapChanger` and the transformer control mode (field `COD` in the _Transformer Data_ record) is `3` an active power control with the following attributes is defined:
- **TargetValue** Active power flow setpoint defined as `0.5` * (`APFI` + `APFA`), where `APFA` and `APFI` are the active power flow upper and lower limits (fields `VMA1`, `VMA1` in the _Transformer Data_ record, same fields as voltage control).
- **TargetDeadband** defined as (`APLA` - `APFI`).
- **RegulatingTerminal** Regulating terminal assigned to the bus where active power flow is controlled (field `CONT1` in the _Transformer Data_ record).
- **RegulatingOn** defined as `true` if `TargetV` is greater than `0.0`.

A set of current operational limits is defined for the two-winding transformer as `1000.0` * `rateMva` / (`sqrt(3.0)` * `vnom1`) at the end `1` and `1000.0` * `rateMva` / (`sqrt(3.0)` * `vnom2`) at the end `2` where `rateMva` is the first rating of the transformer (field `RATA1` in version 33 of the _Transformer Data_ record and field `RATE11` in version 35) and `vnom1` and `vnom2` are the nominal voltage of the associated voltage levels.

When a three-winding transformer is modeled, the two-winding transformer steps should be followed for each leg of the transformer taking into account the following considerations:
- The **Id** is defined according to the pattern `T-<n>-<m>-<o>-<p>` where `n` represents the PSS®E bus `1` number (field `I` in the _Transformer Data_ record), `m` represents the bus `2` number (field `J` in the _Transformer Data_ record), `o` represents the bus `3` number (field `K` in the _Transformer Data_ record) and `p` is the circuit identifier (field `CKT` in the _Transformer Data_ record).
- The three-winding transformers are modeled in PowSyBl as three two-winding transformers connected to a fictitious bus defined with a nominal base voltage and rated voltage of `1.0 kV` (star configuration). <br>
- In PSS®E the between windings transmission impedances `Z1-2`, `Z2-3` and `Z3-1` are specified in the input file. These impedances are generally supplied on a transformer data sheet or test report. The transmission impedances `Z1`, `Z2` and `Z3` of the star network equivalent model are related to them according to the following expressions (see [Modeling of Three-Winding Voltage Regulating Transformers for
  Positive Sequence Load Flow Analysis in PSS®E](https://static.dc.siemens.com/datapool/us/SmartGrid/docs/pti/2010July/PDFS/Modeling%20of%20Three%20Winding%20Voltage%20Regulating%20Transformers.pdf)):

  `Z1-2 = Z1 + Z2`

  `Z2-3 = Z2 + Z3`

  `Z3-1 = Z3 + Z1`

  So:

  `Z1 = 0.5 * (Z1-2 + Z3-1 - Z2-3)`

  `Z2 = 0.5 * (Z1-2 + Z2-3 - Z3-1)`

  `Z3 = 0.5 * (Z2-3 + Z3-1 - Z1-2)`

- All the shunt admittances of the three-winding transformers in the PSS®E model are assigned to the winding `1`. There is no shunt admittance at windings `2` and `3`.
- Each winding can have a complex ratio and a `ratioTapChanger` or `phaseTapChanger` with its corresponding control, always at end `1`. The current PowSyBl version only supports one enabled control by three-winding transformers so if there is more than one enabled only the first (winding `1`, winding `2`, winding `3`) is kept enabled, the rest are automatically disabled.
- In three-winding transformers the status attribute (field `STAT` in the _Transformer Data_ record) could be `0` that means all the windings disconnected, `1` for all windings connected, `2` for only the second winding disconnected, `3` for the third winding disconnected and `4` for the first winding disconnected.

![ThreeWindingsTransformerModels](img/three-winding-transformer-model.svg){width="100%" align=center class="only-light"}
![ThreeWindingsTransformerModels](img/dark_mode/three-winding-transformer-model.svg){width="100%" align=center class="only-dark"}

(psse-slack-bus-conversion)=
### Slack bus

The buses defined as slack terminal are the buses with type code  `3` (field `IDE` in the _Bus Data_ record).


### _Area data_

Every _Area interchange_ single line record represents one area in PowSyBl grid model with the following attributes:

- **AreaType** - always set to "ControlArea"
- **InterchangeTarget** - target active power interchange. It is copied from PSS®E field `PDES`
- **Name** - area name. It is copied from PSS®E field `ARNAME`
- **VoltageLevels** - a set of voltage levels of the area. The set is created from `VoltageLevels` objects that
  correspond
  to PSS®E buses within the given area.
- **AreaBoundaries** – a list of area boundaries. Boundary terminals are determined from the boundary (AC) lines, 
  HVDC lines and transformers. The boundary lines or boundary transformers are always identified such that one of
  their terminals belongs to the area. This  terminal is marked as a boundary terminal and included to area boundaries.
  Similarly, three-winding transformers are considered boundary transformers only if some, but not all, of their
  terminals belong to the area.
