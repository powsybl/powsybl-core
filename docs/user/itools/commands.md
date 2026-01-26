# Commands

```{toctree}
---
maxdepth: 1
hidden: true
---
action-simulator.md
cim_anonymizer.md
compare-security-analysis-results.md
convert_network.md
dynamic-security-analysis.md
dynamic-simulation.md
list-dynamic-simulation-models.md
loadflow.md
loadflow-validation.md
plugins-info.md
run-script.md
security-analysis.md
sensitivity-computation.md
```
(itools-available-commands)=
## Available commands
The `iTools` script relies on a plugin mechanism: the commands are discovered at runtime and depend on the jars present in the `share/java` folder.

| Command                                                                  | Theme           | Description                                                            |
|--------------------------------------------------------------------------|-----------------|------------------------------------------------------------------------|
| [action-simulator](action-simulator.md)                                  | Computation     | Run a security analysis with remedial actions                          |
| [cim-anonymizer](cim_anonymizer.md)                                      | Data conversion | Anonymize CIM files                                                    |
| [compare-security-analysis-results](compare-security-analysis-results.md) | Computation     | Compare security analysis results                                      |
| [convert-network](convert_network.md)                                    | Data conversion | Convert a grid file from a format to another                           |
| [dynamic-security-analysis](dynamic-security-analysis.md)                | Computation     | Run a dynamic security analysis                                        |
| [dynamic-simulation](dynamic-simulation.md)                              | Computation     | Run a dynamic simulation                                               |
| [list-dynamic-simulation-models](list-dynamic-simulation-models.md)      | Computation     | List all models used by the time domain simulation                     |
| [loadflow](loadflow.md)                                                  | Computation     | Run a power flow simulation                                            |
| [loadflow-validation](loadflow-validation.md)                            | Computation     | Validate load flow results on a network                                |
| [plugins-info](plugins-info.md)                                            | Script          | Print the currently available implementations for each kind of plugin  |
| [run-script](run-script.md)                                              | Script          | Run a script on top of PowSyBl                                         |
| [security-analysis](security-analysis.md)                                | Computation     | Run a security analysis                                                |
| [sensitivity-computation](sensitivity-computation.md)                    | Computation     | Run a sensitivity analysis                                             |

