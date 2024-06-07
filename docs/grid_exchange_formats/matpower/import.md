# Import
There is a lot of Matpower open source cases available all over the internet. Most of the time, those cases are provided  as a `.m` file. This is Matlab code and only Matlab can interpret this kind of data. PowSyBl converter can only import `.mat` file which is a binary serialization of Matlab case data structure created from `.m` file.

## Matpower cases conversions
To import a Matpower cases, they have to be converted to `.mat` files first. This can be done using Matlab of course, but it is also possible to use [Octave](https://www.gnu.org/software/octave/), an open source scientific programming language which is mostly compatible with Matlab. Matpower toolbox can be installed with Octave.

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
