# Load flow

```{toctree}
---
hidden: true
maxdepth: 1
---
configuration.md
```

A load flow is a numerical analysis of the flow of electric power in an interconnected system, where that system is
considered to be in normal steady-state operation. Load flow studies are important for planning future expansion of
power systems as well as in determining the best operation of existing systems. The principal outputs obtained from
the load flow study are the magnitude and phase angle of the voltage at each bus, and the active and reactive power
flowing in each line; the current is computed from both of them. In this page we will go into some details about what
are the inputs and outputs of a load flow simulation, what is expected from a load flow result, how the load flow validation
feature works, what load flow implementations are compatible with the generic interface, and how to configure it for
the different implementations.

## Inputs

The only input for a load flow simulation is a network and optionally a set of parameters. The parameters could be generic
(use `LoadFlowParameters`), meaning that there are shared between all implementations. They also could be specific to an
implementation.

## Outputs

The load flow simulation outputs consists of:
- A network, which has been modified based on the simulation results. The modified variables are the active and reactive
power at the terminals, the voltage magnitude and voltage angle at all buses, the solved tap changers positions, the
solved shunt compensator sections.
- A global status indicating if the simulation succeeded for all synchronous components (`Fully Converged` status), or for 
only some of them (`Partially Converged` status), or for none of them (`Failed` status).
- Detailed results per synchronous component: a convergence status, the number of iterations (could be equal to `-1` if
not relevant for a specific implementation), the selected reference bus (voltage angle reference), the selected slack buses
(the buses at which the power balance has been done), active power mismatch at slack buses, and amount of distributed
active power (zero MW if slack distribution is disabled).
- Metrics regarding the computation. Depending on the load flow implementation, the content of these metrics may vary.
- Logs in a simulator specific format.

## Implementations

The following power flow implementations are supported:
- [PowSyBl OpenLoadFlow](TODO)
- [Dynaflow](TODO)

## Going further
- [Run a power flow through an iTools command](../../user/itools/loadflow.md): Learn how to perform a power flow calculation from the command line
- [Load flow tutorial](../../developer/tutorials/loadflow.md)