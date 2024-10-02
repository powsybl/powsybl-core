# Import
There are a lot of Matpower open source cases available all over the internet. Most of the time, those cases are provided as a `.m` file. This is Matlab code, and only Matlab can interpret this kind of data. PowSyBl converter can only import `.mat` file which is a binary serialization of Matlab case data structure created from `.m` file.

## Matpower cases conversions
To import a Matpower cases, they have to be converted to `.mat` files first. This can be done using Matlab, but it is also possible to use [Octave](https://www.gnu.org/software/octave/), an open source scientific programming language which is mostly compatible with Matlab. Matpower toolbox can be installed with Octave.

### Octave and Matpower installation
To install Octave, please follow the installation guide for your operating system, available on their [wiki](https://wiki.octave.org/Category:Installation).

Then, you can download and install Matpower toolbox, following the instructions of the [getting started](https://matpower.org/about/get-started/).

### Matpower case conversion
In this example, we'll use the `case6515rte.m` case file found on [Matpower GitHub](https://github.com/MATPOWER/matpower/blob/master/data/case6515rte.m).

Run Octable in the `traditional` mode, meaning the mode with the maximal compatibility with Matlab especially for binary serializations.
```shell
$> octave --traditional
```

To convert the `case6515rte.m` to `case6515rte.mat`, execute the two following lines:
```matlab
mpc = loadcase('case6515rte.m');
savecase("case6515rte.mat", mpc);
```
Note that the `loadcase` and `savecase` are functions provided by the Matpower toolbox.
 
### Format Specifications

In Matpower the grid is described in Bus/Branch topology where the fields of the struct are `baseMVA, bus, branch, gen,`
and since version 4.1 a simple model for DC transmission lines is also included. The field `baseMVA` is a scalar and the 
rest are all matrices where each row defines a single equipment and each column an attribute of the equipment.

A constant power load can be defined at each bus by specifying the active and reactive power expressed in p.u. Also, the shunt
elements (capacitors or inductors) are modeled as a constant impedance accumulated to the bus `Gs` and `Bs` attributes.

All the transmission lines, transformers and phase shifters are modeled as branches by defining the transmission impedance,
the total charging susceptance, the ratio and the phase shift angle. 

A generator is modeled as a power injection at the bus where it is connected by defining a row in the gen data block. Finally, 
DC line transmissions are modeled as two linked generators, where the generator located at the rectifier bus is extracting power
from the AC network and the generator at the inverter bus is injecting the power to the AC network.

### Conversion

There is no equivalent voltage level or substation concept in the Matpower format, so substations and voltage levels are created 
from the buses description and the topology. Two buses are in the same substation if they are connected by a transformer or a zero impedance line. 
Two buses of the same substation are in the same voltage level if they are connected by a zero impedance line, or they have the same nominal voltage.

The conversion process creates a BusBreaker bus for each bus row, a load if the attributes `Pd` and `Qd` are both not zero and, a shunt compensator
with a linear model and only one block count when `Gs` and `Bs` are not zero.

A generator with a voltage control is created for each row of the gen block data where the regulating control will be on if the 
target voltage is valid.

Each branch row is converted into a line or a two windings transformer based on the `ratio` and `angle` attributes. If the `ratio`
is not zero it will be incorporated in the structural ratio by defining properly the ratedU of both ends. When the `angle` has a
non-zero value a phaseTapChanger with only one step is needed. 

Finally, each row of the DC line block data is converted into one Hvdc Line with voltage source converters. 

