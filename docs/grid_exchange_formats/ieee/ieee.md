# IEEE CDF

The IEEE Common Data Format (CDF) is a standard text format defined by the IEEE Working Group on a Common Format for the Exchange of Solved Load Flow Data, to exchange power flow cases between programs. It is notably used to distribute the well-known IEEE test cases (14-bus, 30-bus, 57-bus, 118-bus, 300-bus...).

PowSyBl supports the **import** of IEEE CDF files into the IIDM model. The export to IEEE CDF is not supported.

## Format specification
An IEEE CDF file is a plain text file, by convention using the `.txt` extension. It starts with a title line holding the date, the originator's name, the base power (MVA), the year and the season (Summer or Winter), followed by several sections, each introduced by a keyword line and terminated by a `-999` line:
- the bus section;
- the branch section;
- the loss zones, interchange data and tie lines sections (not used by the import).

## Import
The IEEE CDF importer is selected automatically when the file matches the expected format. It reads the file and creates an IIDM network with the following mapping:

| IEEE CDF | IIDM |
|----------|------|
| Bus | A [substation](../../grid_model/network_subnetwork.md#substation), a [voltage level](../../grid_model/network_subnetwork.md#voltage-level) and a bus |
| Bus with a load | A [load](../../grid_model/network_subnetwork.md#load) |
| Bus with a generation | A [generator](../../grid_model/network_subnetwork.md#generator) |
| Bus with a shunt susceptance | A [shunt compensator](../../grid_model/network_subnetwork.md#shunt-compensator) |
| Branch (line) | A [line](../../grid_model/network_subnetwork.md#line) |
| Branch (transformer) | A [two-winding transformer](../../grid_model/network_subnetwork.md#two-winding-transformer), with a ratio or phase tap changer depending on the branch type |

The slack bus (a bus holding both voltage and angle) is flagged in the network through the [slack terminal](../../grid_model/extensions.md#slack-terminal) extension.

### Options
The IEEE CDF importer supports the following parameter:

| Parameter | Type | Description | Default value |
|-----------|------|-------------|---------------|
| `ignore-base-voltage` | boolean | Ignore the base voltage specified in the file, and use a nominal voltage of 1 instead | `false` |

This parameter can be set for all imports in the configuration file (see [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md)), or for a single import (for example with the `-I ignore-base-voltage=true` option of the [`convert-network`](../../user/itools/convert_network.md) command).
