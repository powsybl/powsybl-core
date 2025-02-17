# Import
PowerFactory files are available through PowerFactory application. The internal format is `.pfd`. To use and generate PowerFactory files you need to own a licence. You can follow instructions on the [official PowerFactory website](https://www.digsilent.de/en/powerfactory.html).

## Handle PowerFactory exported format cases
PowerFactory files are encrypted (internal format `.pfd`), data are only readable by the PowerFactory application.
PowSyBl core only supports PowerFactory DGS V5 and only the ASCII format (`.dgs` files) which are PowerFactory formatted files. They are created from a PowerFactory application project export.  

A basic DGS export configuration is provided with the PowerFactory application in <PowerFactory install dir>\Dgs\V5.X\DGS Export Definitions 5.pfd. This file has to be imported in your database. However, this basic configuration does not provide all necessary objects and attributes to PowSyBl converter to achieve a correct conversion of the initial data model (and get a calculated state when running a load flow close to the one calculated by PowerFactory).  
So you need to complete the configuration with those following steps.

Two and three windings transformer taps definition attributes need to be added to existing configuration. Optionally, voltage magnitude and angle could be added to terminals to compare the calculated state between PowerFactory and PowSyBl. VSC and common impedance configuration are totally missing and need to be created. Optionally, in order to get the same slack, the attribute ip_ctrl can be provided.

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


## Import PowerFactory internal format

A [powsybl-powerfactory-db-native](https://github.com/powsybl/powsybl-powerfactory-db-native) repository has been created to import a proprietary PowerFactory file (`.pfd`) directly via a C++ API but the import can be slower and is not maintained for now.
