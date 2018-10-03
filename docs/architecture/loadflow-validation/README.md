# Loadflow validation

The load-flow validation aims at ensuring the consistency of load-flow results with respect to a set of rules that describes what is a "correct" load-flow result. On the most abstract level, a load-flow result is "correct" if it describes a feasible steady-state of a power system given its physics and its logics. More practically, generations of practitioners have set quasi-standard ways to describe them that allows to define precise rules.

Overall, tests should never be too tight and leniency is preferred to tightness in case approximations are needed or in case expectations are unclear (typically when the input data is inconsistent). For example, there is a switch to test only the main component because it is not clear what to expect from load flow results on small connected components.

Another important global setting is the ok-missing-values parameter, which determines which is OK to have missing values or NaN. Normally, it should be set to false but it may be useful in cases the power flow results are incomplete.

This documentation explains the tests done. The documentation of the load-flow validation command, including all its parameters can be found [here](../../tools/loadflow-validation.md).

# Buses

The first law of Kirchhoff must be satisfied for every bus for active and reactive power:

    | sum of P over branches + sum  active  injections | < threshold
    | sum of Q over branches + sum reactive injections | < threshold

If one value is missing, the test is OK.

If only the voltages are given, com.powsybl.loadflow.ResultsCompletionLoadFlow can be used to compute the flows from the voltages in order to validate the rule.

# Branches

All branches are converted into an universal branch:

    V1*exp(j*theta1)     rho1*exp(j*alpha1)             r+j*x              rho1*exp(j*alpha1)   V2*exp(j*theta2)
        (P1,Q1)->      ____O/O__________________________-----__________________________O/O_____     <-(P2,Q2)
                                            |           -----           |
                                  g1+j*b1  |_|                         |_| g2+j*b2
                                            |                           |
                                           _|_                         _|_
                                            _                           _
                                            .                           .

* Power-flow results:
    * (V1, theta1) and (V2, theta2): Magnitude (kV) and angle (°) of the voltage at the connection buses 1 and 2 
respectively.
    * (P1,Q1) and (P2,Q2): Active power (MW) and reactive power (MVAr) injected in the branch on each side.
* Characteristics:
    * (rho1, alpha1) and (rho2, theta2): Magnitude (no unit) and angle (°) of the ideal transformers on each side.
    * (g1,b1) and (g2,b2): Complex shunt impedance (Ohm) on each side.
    * (r,x): Complex serial impedance (Ohm).

Thanks to Kirchhoff laws, estimations of powers are computed according to the voltages and the characteristics of the branch:

    (P1calc, Q1calc, P2calc, Q2calc) = f(Voltages, Characteristics)

The test of the branch is OK if:

    max( |P1calc-P1|, |Q1calc-Q1|, |P2calc-P2|, |Q2calc-Q2| ) <= threshold

In the case of branches that are disconnected on one end (for example end 2), then `P2=Q2=0`. As a result, it is possible to recompute (V2, theta2) which are usually not returned by power-flows and which are not stored in node-breaker iIDM format. Then, the same tests are done.

In case of missing results (usually the powers P1, Q1, P2, Q2 which are not mandatory), the test is always OK if ok-missing-values=true and NOK if false. In case the voltages are available but not the powers, the com.powsybl.loadflow.ResultsCompletionLoadFlow recomputes them using the validation equations (meaning that the branch validation tests will always be OK but it allows to perform the bus validation tests).

# Three-winding transformers

To be implemented, based on a conversion into 3 two-winding transformers.

# Generators

## Active power

As there is no standard way to balance generation and consumption in power flow, the validation assumes that the power-flow results are balanced, meaning that, for all generators including those of the slack node:

    |Active Power Set Point (targetP) - Active power (P)| < threshold

## Voltage and reactive power

### Voltage regulation deactivated

If the voltage regulation is deactivated, it is expected that:

    |Reactive Power Set Point (targetQ) - Reactive power (Q)| < threshold

### Voltage regulation activated

If the voltage regulation is activated, the generator is modelled as a PV/PQ node: the voltage target should be reached except if reactive bounds are hit (PV mode). If the reactive bounds are hit, the reactive power should be equal to a limit. Mathematically speaking, one of the following 3 conditions should be met:

    |v-targetV|<= threshold and minQ <= Q <= maxQ
     v-targetV < -threshold and |Q-maxQ| <= threshold
     targetV-V <  threshold and |Q-minQ| <= threshold

There are a few tricks to handle special cases:
- if minQ>maxQ, then the values are switched to recover a meaningfull interval if noRequirementIfReactiveBoundInversion=false
- in case of a missing value, the corresponding test is OK
- minQ and maxQ are function of P. If targetP is outside [minP, maxP], no test is done.

# Loads

To be implemented, with tests similar to generators with voltage regulation.

# Shunts

A shunt is expected not to generate or absorb active power:

    | P | < threshold

A shunt is expected to generate reactive power according to the number of actived section and to the susceptance per section:

    | Q + B * #sections * V² | < threshold

# Static VAr Compensator

Static VAr Compensator behave like generators producing 0 active power except that their reactive bounds are expressed in susceptance, so that they are voltage dependent.

    targetP = 0 MW

* If the regulation mode is OFF, then `targetQ = 0 MW`
* If the regulation mode is REACTIVE_POWER, it behaves like a generator without voltage regulation
* If the regulation mode is VOLTAGE, it behaves like a generator with voltage regulation with the following bounds: `minQ = - bMax * V²` and `maxQ = - bMin * V²`

# HVDC lines

To be done.

## VSC

VSC converter stations behave like generators with the additional constraints that the sum of active power on converter stations paired by a cable is equal to the losses on the converter stations plus the losses on the cable.

## LCC

To be done.

# Ratio tap transformers

Ratio tap transformers have a tap with a finite discrete number of position that allows to change its characteristics, especially the transformer ratio. Let assume that the logic is based on dead band: if the deviation between the measurement and the set point is higher than the dead band width, the tap position is increased or decreased of one unit.

As a result, a state is a steady state only if the regulated value is within the dead band or if the tap position is at minimum or maximum. To check this assertion, an upper bound of the dead-band value is needed. Generally, the value of the dead-band is not know available in data models. Usual load flow solvers simply consider a continuous tap that is rounded afterwards. As a result, one should compute an upper bound of the effect of the rounding. Under the usual situation where the low voltage (side one) is controlled, the maximum effect is expected if the high voltage is fixed (usually it decreases) and if the network connected to the low voltage is an antenna. If the transformer is perfect, the equations are:

With the current tap `tap`, and if regulated side is side TWO:

    V2(tap) = rho(t)*V1

With the next tap, the new voltage would be:

    V2(tap+1) = rho(tap+1)*V1 = rho(tap+1)/rho(tap)*V2(t)

We can therefore compute approximately the voltage increments corresponding to `tap+1` and `tap-1`.

We then assume the "deadband" of the regulation to be equal to the voltage increase/decrease that can be performed with taps `tap+1` and `tap-1`:

    updeadband = -min(V2(tap+1)-V2(tap), V2(tap-1)-V2(tap))

    downdeadband = max(V2(tap+1)-V2(tap), V2(tap-1)-V2(tap))

Finally, we check that the voltage deviation `deviation = V2(tap) - targetV2` stays inside the deadband.
* If `deviation < 0`, meaning that the voltage is too low, it should be checked if the deviation would be smaller by increasing V2, i.e. the following condition should be satisfied: `|deviation| < downdeadband + threshold`
* If `deviation > 0`, meaning that the voltage is too high, it should be checked if the deviation would be smaller by decreasing V2, i.e. the following condition should be satisfied: `deviation < updeadband  + threshold`

The test is done only if the regulated voltage is on one end of the transformer and it always returns OK if the controlled voltage is remote.


## References:

* 2018 iPST-day: [Steady-state validation](http://www.itesla-pst.org/pdf/iPST-PowSyBl-day-2018/04%20-%20iPST-PowSyBl%20day%20-%20Open-source%20steady-state%20validation.pdf)