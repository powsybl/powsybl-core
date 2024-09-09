# Grid exchange formats

This section is dedicated to the description of the various grid formats available in PowSyBl. While IIDM is at the core
of it, it is possible to import and/or export data in a number of formats that may be dedicated to European data
exchanges
or network simulation via different tools: check them out below.

| Data format                                 | description                                                                                                  |                  import                   |                  export                   |
|---------------------------------------------|--------------------------------------------------------------------------------------------------------------|:-----------------------------------------:|:-----------------------------------------:|
| [CIM-CGMES](cgmes/index.md)                 | the standard format for European grid data exchange                                                          | <span style="color:green">&#x2714;</span> | <span style="color:green">&#x2714;</span> |
| [UCTE-DEF](ucte/index.md)                   | the legacy format for European grid data exchange                                                            | <span style="color:green">&#x2714;</span> | <span style="color:red">&#x2718;</span>*  |
| [IIDM](iidm/index.md) (XIIDM, JIIDM, BIIDM) | the internal data model of PowSyBl in a XML / JSON / binary format                                           | <span style="color:green">&#x2714;</span> | <span style="color:green">&#x2714;</span> |
| [IEEE-CDF](ieee/ieee.md)                    | a IEEE standard format                                                                                       | <span style="color:green">&#x2714;</span> |  <span style="color:red">&#x2718;</span>  |
| [PSS®E](psse/index.md)                      | the format for power flow analysis on Siemens PSS®E software                                                 | <span style="color:green">&#x2714;</span> | <span style="color:red">&#x2718;</span>*  |
| PowerFactory                                | the format for DIgSILENT PowerFactory software                                                               | <span style="color:green">&#x2714;</span> |  <span style="color:red">&#x2718;</span>  |
| [Matpower](matpower/index.md)               | the format for the free and open-source Matlab toolbox dedicated to power system simulation and optimization | <span style="color:green">&#x2714;</span> |  <span style="color:red">&#x2718;</span>  |
| [AMPL](ampl/index.md)                       | a data separated value format easy to parse with AMPL                                                        |  <span style="color:red">&#x2718;</span>  | <span style="color:green">&#x2714;</span> |

\* Note that updated export is available, that is, export is possible if the file was imported with the same format.
For instance, if you import a UCTE-DEF file in powsybl you can update some elements and then export it back to UCTE-DEF
format, but you cannot export to UCTE-DEF format a file imported from another format.

```{toctree}
---
maxdepth: 1
hidden: true
---

cgmes/index.md
ucte/index.md
iidm/index.md
ieee/ieee.md
matpower/index.md
psse/index.md
ampl/index.md
going_further/index
```
