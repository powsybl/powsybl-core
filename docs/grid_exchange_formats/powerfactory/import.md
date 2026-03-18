# Import
PowerFactory files are available through PowerFactory application. The internal format is `.pfd`. To use and generate PowerFactory files you need to own a licence. You can follow instructions on the [official PowerFactory website](https://www.digsilent.de/en/powerfactory.html).

## Handle PowerFactory exported format cases
PowerFactory files are encrypted (internal format `.pfd`), data are only readable by the PowerFactory application.
PowSyBl core only supports PowerFactory DGS V5 and only the ASCII format (`.dgs` files) which are PowerFactory formatted files. They are created from a PowerFactory application project export.

A basic DGS export configuration is provided with the PowerFactory application in <PowerFactory install dir>\Dgs\V5.X\DGS Export Definitions 5.pfd. This file has to be imported in your database. However, this basic configuration does not provide all necessary objects and attributes to PowSyBl converter to achieve a correct conversion of the initial data model (and get a calculated state when running a load flow close to the one calculated by PowerFactory).
So you need to complete the configuration with those following steps.

Two and three winding transformer taps definition attributes need to be added to existing configuration. Optionally, voltage magnitude and angle could be added to terminals to compare the calculated state between PowerFactory and PowSyBl. VSC and common impedance configuration are totally missing and need to be created. Optionally, in order to get the same slack, the attribute `ip_ctrl` can be provided.

```
- 2-Winding Transformer.IntMon (class ElmTr2)
    - e:mTaps

- 3-Winding Transformer.IntMon (class ElmTr3)
    - e:mTaps
    - e:iMeasTap

- Terminal.IntMon (class ElmTerm)
    - m:u
    - m:phiu

- PWM Converter/2 DC-Connections.IntMon (class ElmVsc)
    - e:loc_name
    - e:fold_id
    - e:psetp
    - e:qsetp
    - e:usetp
    - e:Pnold
    - e:Unom
    - e:P_max

- Common Impedance.IntMon (class ElmZpu)
    - e:loc_name
    - e:outserv
    - e:Sn
    - e:nphshift
    - e:ag
    - e:fold_id
    - e:r_pu
    - e:x_pu
    - e:r_pu_ji
    - e:x_pu_ji
    - e:gi_pu
    - e:bi_pu
    - e:gj_pu
    - e:bj_pu

- Synchronous Machine.IntMon (class ElmSym)
    - e:ip_ctrl
```

## Handling HVDC data

### Selecting reduced or detailed model

By default, the DGS file importer tries to import HVDC components as _reduced_ point-to-point representations. Parameter `powerfactory.import.dgs.HVDC-import-detailed` must be set to `true` to activate import of _detailed_ DC subnetworks with multi-terminal possibility.

### Additional attributes (_detailed_)
For _detailed_ network import, the following additional attributes are also requested in the DGS file for the VSCs (`ElmVsc`)

| Attribute     | Meaning                                                         |
|---------------|-----------------------------------------------------------------|
| i_acdc        | Control mode selector.<br/> Values 3, 4, 5 and 6 are supported. |
| usetpdc       | Control voltage in p.u. for DC voltage control.                 |
| Unomdc        | Nominal DC voltage for DC voltage control.                      |
| swtLossFactor | Swicthing loss factor (otherwise default to zero).              |
| resLossFactor | Resistive loss factor (otherwise default to zero).              |

Ground elements (`ElmGndswt`) are not exported by default by PowerFactory. Their export to the DGS file must be declared specifically if grounds are to be re-imported by PowSyBl. No additional attribute is mandatory for ground elements (`ElmGndswt`). If `on_off` is present and has value zero, the ground element is considered disconnected and it is not added to the network. `ciEarthed` is disregarded by the importer. The switch itself is not imported to PowSyBl. The ground resistance is assumed to be zero.

`ElmTerm`, `ElmLne` and `TypLne` are used by the importer, but require no additional data than the default attributes. 

### Control mode (_detailed_)

The following type of control mode is setup for VSCs, depending on the value of `i_acdc`:

| `i_acdc` | Behavior  | ControlMode | voltageRegulatorOn |
|----------|:---------:|:------------|--------------------|
| 3        |  Vdc - Q  | V_DC        | false              |
| 4        |  P - Vac  | P_PCC       | true               |
| 5        |   P - Q   | P_PCC       | false              |
| 6        | Vdc - Vac | V_DC        | true               |

Values 0, 1, 2, 7 and 8 are not supported and will raise a PowerFactoryException during the import.

### Limitations (_detailed_)
- The only supported AC-DC converters are VSCs in `ElmVsc`.
- For now PCC control is not taken into account and the VSC is connected to a single terminal.
- Attribute `ciEarthed` of `ElmTerm` is ignored by the importer.

## Import PowerFactory internal format

A [powsybl-powerfactory-db-native](https://github.com/powsybl/powsybl-powerfactory-db-native) repository has been created to import a proprietary PowerFactory file (`.pfd`) directly via a C++ API but the import can be slower and is not maintained for now.
