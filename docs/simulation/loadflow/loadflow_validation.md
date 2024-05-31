# Load flow validation

A load flow result is considered *acceptable* if it describes a feasible steady-state of a power system given its physics and its logics.
More practically, generations of practitioners have set quasi-standard ways to describe them that makes it possible to define precise rules.
They are described below for the different elements of the network.

### Buses

The first law of Kirchhoff must be satisfied for every bus for active and reactive power:

$$\begin{equation}
\left| \sum_{branches} P + \sum_{injections} P \right| \leq \epsilon \\
\left| \sum_{branches} Q + \sum_{injections} Q \right| \leq \epsilon \\
\end{equation}$$

### Branches
Lines and two windings transformers are converted into classical PI models:

```
    V1*exp(j*theta1)     rho1*exp(j*alpha1)             r+j*x              rho2*exp(j*alpha2)   V2*exp(j*theta2)
        (P1,Q1)->      ____O/O__________________________-----__________________________O/O_____     <-(P2,Q2)
                                            |           -----           |
                                  g1+j*b1  |_|                         |_| g2+j*b2
                                            |                           |
                                           _|_                         _|_
                                            _                           _
                                            .                           .
```

- Power flow results:
    - $(\|V_1\|, \theta_1)$ and $(\|V_2\|, \theta_2)$: Magnitude (kV) and angle ($°$) of the voltage at the sides 1 and 2, respectively.
    - $(P_1, Q_1)$ and $(P_2, Q_2)$: Active power (MW) and reactive power (MVAr) injected in the branch on each side.
- Characteristics:
    - $(\rho_1, \alpha_1)$ and $(\rho_2, \alpha_2)$: Magnitude (no unit) and angle ($°$) of the ideal transformers
      ratios on each side.
    - $(g_1, b_1)$ and $(g_2, b_2)$: Complex shunt impedance on each side (S).
    - $(r, x)$: Complex series impedance $(\Omega)$.

Thanks to Kirchhoff laws (see the [line](../../grid_model/network_subnetwork.md#line) and [2-winding transformer](../../grid_model/network_subnetwork.md#two-windings-transformer) documentation), estimations of powers are computed according to the voltages and the characteristics of the branch:

$(P_1^{calc}, Q_1^{calc}, P_2^{calc}, Q_2^{calc}) = f(\text{Voltages}, \text{Characteristics})$

#### Three-windings transformers
To be implemented, based on a conversion into 3 two-windings transformers.

#### Generators

##### Active power
There may be an imbalance between the sum of generator active power setpoints $\text{targetP}$ on one side and consumption
and losses on the other side, after the load flow optimization process. Note that, if it is possible to modify the setpoints during the computation
(for example if the results were computed by an Optimal Power Flow and not a Power Flow), there should be no imbalance left.

In case of an imbalance between the sum of generator active power setpoints $\text{targetP}$ on one side and consumption
and losses on the other side, the generation $P$ of some units has to be adjusted.
The adjustment is done by modifying the generation of the generators connected to the slack node of the network.
It may also be done by modifying the loads connected to the slack node.
The slack node is a computation point designated to be the place where adjustments are done.

This way of performing the adjustment is the simplest solution from a mathematical point of view, but it presents several drawbacks.
In particular, it may not be enough in case of a large imbalance.
This is why other schemes have been developed, called "distributed slack nodes".

Generators or loads are usually adjusted proportionally to a shift function to be defined.
Three keys have been retained for the validation ($g$ is a generator):
Usual ways of defining this function, for each equipment that may be involved in the compensation (generator or load), read:
- proportional to $P_{max}$: $F = f \times P_{max}$
- proportional to ${targetP}$: $F = f \times targetP$
- proportional to $P_{diff}$: $F = f (P_{max} - targetP)$

$f$ is a participation factor, per unit. For example, a usual definition is: $f\in\{0,1\}$: either the unit
participates or not. The adjustment is then done by doing:
$P <- P \times \hat{K} \times F$
where $\hat{K}$ is a proportionality factor, usually defined for each unit by $\dfrac{P_{max}}{\sum{F}}$, $\dfrac{targetP}{\sum{F}}$ or $\dfrac{P_{diff}}{\sum{F}}$
depending on the adjustment mode (the sums run over all the units participating in the compensation).

##### Voltage and reactive power

If the voltage regulation is deactivated, it is expected that:

$\left| targetQ - Q \right| < \epsilon$

If the voltage regulation is activated, the generator is modeled as a $PV$ node.
The voltage target should be reached, except if reactive bounds are hit. Then, the generator is switched to $PQ$ node and the reactive power should be equal to a limit.
Mathematically speaking, one of the following 3 conditions should be met:

\begin{align*}
|V - targetV| & \leq && \epsilon && \& && minQ & \leq & Q \leq maxQ \\
V - targetV & < & -& \epsilon && \& && |Q-maxQ| & \leq & \epsilon \\
targetV - V & < && \epsilon && \& && |Q-minQ| & \leq & \epsilon \\
\end{align*}
$$

#### Loads
To be implemented, with tests similar to generators with voltage regulation.

#### Shunts
A shunt is expected not to generate or absorb active power:

$\left| P \right| < \epsilon$

A shunt is expected to generate reactive power according to the number of activated sections and to the susceptance per section $B$:
$\left| Q + \text{#sections} * B  V^2 \right| < \epsilon$

#### Static VAR Compensators
Static VAR Compensators behave like generators producing 0 active power except that their reactive bounds are expressed
in susceptance, so that they are voltage dependent.

$targetP = 0$ MW

- If the regulation mode is `OFF`, then $targetQ$ is constant
- If the regulation mode is `REACTIVE_POWER`, it behaves like a generator without voltage regulation
- If the regulation mode is `VOLTAGE`, it behaves like a generator with voltage regulation with the following bounds (dependent on the voltage, which is not the case for generators):
  $minQ = - Bmax * V^2$ and $maxQ = - Bmin V^2$

#### HVDC lines
To be done.

##### VSC
VSC converter stations behave like generators with the additional constraints that the sum of active power on converter
stations paired by a cable is equal to the losses on the converter stations plus the losses on the cable.

##### LCC
To be done.

#### Transformers with a ratio tap changer

Transformers with a ratio tap changer have a tap with a finite discrete number of position that allows to change their transformer ratio.
Let's assume that the logic is based on deadband: if the deviation between the measurement
and the setpoint is higher than the deadband width, the tap position is increased or decreased by one unit.

As a result, a state is a steady state only if the regulated value is within the deadband or if the tap position is at
minimum or maximum: this corresponds to a valid load flow result for the ratio tap changers tap positions.