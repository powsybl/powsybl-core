# Simulation

One major aim of the PowSyBl project is to make it easy to plug it with different solvers for grid simulation.
At the moment, four types of simulations are supported: power flow, security analysis, time-domain simulation and sensitivity analysis.
For each of them, one or several simulators is officially supported, but it is also possible to plug your own simulator within the framework.
Below comes a description of the various types of simulation and supported solvers.

```{toctree}
---
maxdepth: 2
---

loadflow/loadflow.md
security/security.md
sensitivity/sensitivity.md
shortcircuit/index.md
```