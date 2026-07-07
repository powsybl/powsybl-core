# Load flow validation
```{toctree}
---
hidden: true
maxdepth: 1
---

configuration.md
```
A load flow result is considered *acceptable* if it describes a feasible steady-state of a power system given its physics and its logics.
More practically, generations of practitioners have set quasi-standard ways to describe them that makes it possible to define precise rules.
They are described below for the different elements of the network.

Overall, in the PowSyBl loadflow validation, the tests are not made overly tight. In particular, leniency is preferred to tightness in case approximations are needed or
when expectations are unclear (typically when the input data is inconsistent). For example, there is a switch to test
only the main component because it is not clear what to expect from load flow results on small connected components.

Another important global setting available in the PowSyBl validation is the `ok-missing-values` parameter, which determines if is OK to have missing
values or `NaN`. Normally, it should be set to false, but it may be useful in the cases where the power flow results are
incomplete to go through the rest of the validation.

See the documentation [here](configuration.md) to configure correctly this feature.

(loadflow-validation-buses)=
## Buses

If all values are present, or if only one value is missing, the result is considered to be consistent.

Note that if the result contains only the voltages (phase and angle), the PowSyBl validation provides a load-flow results completion feature.
It can be used to compute the flows from the voltages to ensure the result consistency, with the run-computation option of the PowSyBl validation.

The first law of Kirchhoff must be satisfied for every bus for active and reactive power:

$$
\begin{aligned}
\left| \sum_{branches} P + \sum_{injections} P \right| \leq \epsilon \\
\left| \sum_{branches} Q + \sum_{injections} Q \right| \leq \epsilon
\end{aligned}
$$

Reworded with elements details:
- `P injections` and `Q injections` are the sums of connected injections (generators, batteries, shunts, SVCs, VSC, lines, dangling lines, and transformers)
- `P load` and `Q load` are the sums of connected loads.

(loadflow-validation-branches)=
## Branches

Lines and two-winding transformers are converted into classical PI models:

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

Thanks to Kirchhoff laws (see the [line](../../grid_model/network_subnetwork.md#line) and [2-winding transformer](../../grid_model/network_subnetwork.md#two-winding-transformer) documentation), estimations of powers are computed according to the voltages and the characteristics of the branch:

$(P_1^{calc}, Q_1^{calc}, P_2^{calc}, Q_2^{calc}) = f(\text{Voltages}, \text{Characteristics})$


The result on the branch is considered consistent if:

$$\max( \left| P_1^{calc} - P_1 \right|, \left| Q_1^{calc} - Q_1 \right|, \left| P_2^{calc} - P_2 \right|, \left| Q_2^{calc} - Q_2 \right| ) \leq \epsilon$$

For a branch that is disconnected on one end (for example, end 2), then $P_2 = Q_2 = 0$. As a result, it is
possible to recompute $(V_2, \theta_2)$ which are usually not returned by power flows and which are not stored in node-breaker
[network](../../grid_model/index.md) format. The quality checks are done when this is done.

In case of missing results (usually the powers $P_1$, $Q_1$, $P_2$, $Q_2$ which are not mandatory), the PowSyBl validation
will consider the results as inconsistent, unless `ok-missing-values` was set to `true` by the user on purpose to make the consistency
check more leniently.

In case the voltages are available but not the powers, the result completion feature of the PowSyBl validation
can be used to recompute them using the validation equations (meaning that the branch validation tests will always be OK, so that it allows performing the bus validation tests).

In case of disconnected branch, $P_i$ and $Q_i$ must be undefined or approximately equal to zero.

(loadflow-validation-three-winding-transformers)=
## Three-winding transformers

All network three-winding transformers are evaluated individually.

The validation consists in comparing the active and reactive powers on each leg with the recalculated powers (from the transformer parameters).

The configuration used during this validation is:
- [threshold](configuration.md#threshold) to determine the acceptable difference between the measured and calculated powers
- [apply-reactance-correction](configuration.md#apply-reactance-correction) to correct or not the very small reactances to `epsilon-x` value
- [epsilon-x](configuration.md#epsilon-x) for reactances considered too small then they can be set to epsilon-x value
- [ok-missing-values](configuration.md#ok-missing-values) to determine if missing values (NaN) are considered as valid or not
 
If the transformer has the `ThreeWindingsTransformerPhaseAngleClock` extension, the phase angles clocks 2 and 3 are extracted, if not they are considered as `0`.

The power comparisons are done via a `TwtData` [![Javadoc](https://img.shields.io/badge/-javadoc-blue.svg)](https://javadoc.io/doc/com.powsybl/powsybl-iidm-api/latest/com.powsybl.iidm.api/com/powsybl/iidm/network/util/TwtData.html) object.

A three-winding transformer is considered valid if its 3 legs are valid.

A leg is valid:
- if not connected or not in the main component
- or if the measured and calculated powers are within the configured threshold.

$$
\begin{aligned}
|P_{side\;measured} - P_{side\;computed}| > thresold \\
|Q_{side\;measured} - Q_{side\;computed}| > thresold
\end{aligned}
$$

(loadflow-validation-generators)=
## Generators

### Active power
There may be an imbalance between the sum of generator active power setpoints $\text{targetP}$ on one side and consumption
and losses on the other side, after the load flow optimization process. Note that if it is possible to modify the setpoints during the computation
(for example, if the results were computed by an Optimal Power Flow and not a Power Flow), there should be no imbalance left.

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

The load-flow validation of PowSyBl checks whether the adjustment of balances has been done consistently by the power flow.

The load-flow results do not include the adjustment mode used, nor the participation factors. They thus have to be inferred.
If deviations are perfect, the proportion factor $\hat{K}$ estimated for the right mode will
be the same for all the deviating units for which $P$ is strictly $P_{min}$ and $P_{max}$. Therefore, the inferred
deviation is the one for which the standard deviation of the estimated proportion factor is the lowest.

Once the mode is determined, the new target can be computed for each unit. The following check is done:

$$\left| \max(P_{min}, \min(P_{max}, (1+\hat{K} F(g)))) targetP - P \right| < \epsilon$$

### Voltage and reactive power

If the voltage regulation is deactivated, it is expected that:

$$\left| targetQ - Q \right| < \epsilon$$

If the voltage regulation is activated, the generator is modeled as a $PV$ node.
The voltage target should be reached, except if reactive bounds are hit. Then, the generator is switched to $PQ$ node and the reactive power should be equal to a limit.
Mathematically speaking, one of the following 3 conditions should be met:

$$
\begin{aligned}
|V - targetV| & \leq && \epsilon && \& && minQ & \leq & Q \leq maxQ \\
V - targetV & < & -& \epsilon && \& && |Q-maxQ| & \leq & \epsilon \\
targetV - V & < && \epsilon && \& && |Q-minQ| & \leq & \epsilon \\
\end{aligned}
$$


In the PowSyBl validation, there are a few tricks to handle special cases before applying the nominal active/reactive/voltage rules
- if `P` or `Q` is missing, validation fails if setpoints are defined and non-zero
- if $minQ > maxQ$, then the values are switched to recover a meaningful interval if `noRequirementIfReactiveBoundInversion = false`
- in case of a missing value, the corresponding test is OK
- $minQ$ and $maxQ$ are function of $P$. If $targetP$ is outside $[minP, maxP]$, and `noRequirementIfSetpointOutsidePowerBounds = true`, generator validation checks are skipped.

(loadflow-validation-loads)=
## Loads
To be implemented, with tests similar to generators with voltage regulation.

(loadflow-validation-shunts)=
## Shunts
A connected shunt is expected:
- Not to generate or absorb active power:
$$
\left| P \right| < \epsilon
$$

- To generate reactive power according to the number of activated sections and to the susceptance per section $B$:
$$
\left| Q + \#\text{sections} * B  V^2 \right| < \epsilon
$$

Additional condition for disconnected shunts:
- `Q` must be undefined or equal to `0`

Only linear shunts are supported for now.

(loadflow-validation-static-var-compensators)=
## Static VAR Compensators
Static VAR Compensators behave like generators producing zero active power except that their reactive bounds are expressed
in susceptance, so that they are voltage dependent.

$|P - targetP| <= \epsilon$, with $targetP = 0$ MW.

- If the regulation is disabled, then $targetQ$ is constant, $|Q| <= \epsilon$.
- If `P` or `Q` is missing, then reactive power setpoint must be undefined equal to `0`.
- If the regulation mode is `REACTIVE_POWER`, it behaves like a generator without voltage regulation: $|Q - reactivePowerSetpoint| <= \epsilon$.
- If the regulation mode is `VOLTAGE`, it behaves like a generator with voltage regulation with the following bounds (dependent on the voltage, which is not the case for generators):
  $minQ = - Bmax * V^2$ and $maxQ = - Bmin V^2$
  - If $V < voltageSetpoint$, then `Q` must match `maxQ`.
  - If $V > voltageSetpoint$, then `Q` must match `minQ`.
  - If $|V - voltageSetpoint| <= \epsilon$, then `Q` must be within `[minQ, maxQ]`.

(loadflow-validation-hvdc)=
## HVDC lines
To be done.

(loadflow-validation-vsc)=
## VSC
VSC converter stations behave like generators with the additional constraints that the sum of active power on converter
stations paired by a cable is equal to the losses on the converter stations plus the losses on the cable.

Same checks as a generator. Besides, for stations paired by a cable:

$$\sum_{\text{stations}}{P} = \sum_{\text{stations}}{Loss} + Loss_{cable}$$

(loadflow-validation-lcc)=
## LCC
To be done.

(loadflow-validation-transformers-ratio-tap-changer)=
## Transformers with a ratio tap changer

Transformers with a ratio tap changer have a tap with a finite discrete number of positions that allows to change their transformer ratio.
Let's assume that the logic is based on deadband: if the deviation between the measurement
and the setpoint is higher than the deadband width, the tap position is increased or decreased by one unit.

As a result, a state is a steady state only if the regulated value is within the deadband or if the tap position is at
minimum or maximum: this corresponds to a valid load flow result for the ratio tap changers tap positions.

To check a steady-state has been reached, an upper bound of the deadband value is needed. Generally, the value of the
deadband is not available in data models. Usual load flow solvers simply consider a continuous tap that is rounded
afterward. As a result, one should compute an upper bound of the effect of the rounding. Under the usual situation where
the low voltage (side one) is controlled, the maximum effect is expected if the high voltage is fixed (usually it decreases),
and if the network connected to the low voltage is an antenna. If the transformer is perfect, the equations are:

- With the current tap `tap`, and if the regulated side is side `TWO`:

$$V_2(tap) = \rho_{tap} V_1$$

- With the next tap, the new voltage would be:

$$V_2(tap+1) = \rho_{tap+1} V_1 = \frac{\rho_{tap+1}}{\rho_{tap}} V_2(tap)$$

We can therefore compute approximately the voltage increments corresponding to $tap-1$ and $tap+1$.

- We then assume the *deadband* of the regulation to be equal to the voltage increase/decrease that can be performed with
  taps $tap-1$ and $tap+1$:

$$
\begin{aligned}
& \text{up deadband} = - \min(V_2(tap+1) - V_2(tap), V_2(tap-1) - V_2(tap)) \\
& \text{down deadband} = \max(V_2(tap+1) - V_2(tap), V_2(tap-1) - V_2(tap))
\end{aligned}
$$

Finally, we check that the voltage deviation $\text{deviation} = V_2(tap) - targetV2$ stays inside the deadband.
- If $deviation < 0$, meaning that the voltage is too low, it should be checked if the deviation is smaller by
  increasing $V_2$, i.e., the following condition should be satisfied: $\left| deviation \right| < down deadband + threshold$
- If $deviation > 0$, meaning that the voltage is too high, it should be checked if the deviation is smaller by
  decreasing $V_2$, i.e., the following condition should be satisfied: $deviation < up deadband + threshold$

The test is done only if the regulated voltage is on one end of the transformer, and it always returns OK if the controlled voltage is remote.