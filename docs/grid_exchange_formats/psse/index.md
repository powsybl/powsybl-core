# PSS®E

```{toctree}
:hidden:
import.md
export.md
examples.md
```

[PSS®E software](https://new.siemens.com/global/en/products/energy/energy-automation-and-smart-grid/pss-software/pss-e.html) from Siemens provides analysis functions for power system networks in steady-state and dynamic conditions.
PSS®E uses different types of files to exchange data about the network. One of them is the RAW file (power flow data file). A PSS®E RAW file contains a collection of unprocessed data that specifies a Bus/Branch network model for the establishment of a power flow working case.

The RAW file has multiple groups of records (data blocks), with each group containing a particular type of data needed in power flow. The last record of each data block is a record specifying a value of zero to indicate the end of the category.

Each record in a data block contains a set of data items separated by a comma or one or more blanks where alphanumeric attributes must be enclosed in single quotes. As many of the data items specified in the RAW file have a default value, only the specific information needed should be defined in the record.

In PSS®E version 35, a new RAWX file format (Extensible Power Flow Data File) based on JSON has been introduced. It will be the standard text-based data format for PSS®E power flow data exchange. The RAWX files contain two types of data objects: Parameter Sets and Data Tables. A Parameter Set has an array with field names and a single array with field values. A Data Table has an array with field names an and array of records, each record being an array of field values. The field names array indicates the order and subset of fields for which data is provided in the data arrays.
