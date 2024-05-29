# Additional network models

In this section, the additional models available in IIDM are described: reactive limits, current limits, voltage regulation, phase and ratio tap changers.
They can be used by various equipment models.

## Reactive limits
[![Javadoc](https://img.shields.io/badge/-javadoc-blue.svg)](https://javadoc.io/doc/com.powsybl/powsybl-core/latest/com/powsybl/iidm/network/ReactiveLimits.html)

The reactive limits may be used to model limitations of the reactive power of
[generators](#generator), [VSC converter stations](#vsc-converter-station) and [batteries](#battery).

### Min-Max reactive limits
With the min-max reactive limits, the reactive power does not depend on the active power. For any active power value, the reactive power value is in the [minQ, maxQ] interval.

### Reactive capability curve
With the reactive capability curve limits, the reactive power limitation depends on the active power value. This dependency is based on a curve provided by the user.
The curve is defined as a set of points that associate, to each active power value, a minimum and maximum reactive power value.
In between the defined points of the curve, the reactive power limits are computed through a linear interpolation.

## Loading Limits
[![Javadoc](https://img.shields.io/badge/-javadoc-blue.svg)](https://javadoc.io/doc/com.powsybl/powsybl-core/latest/com/powsybl/iidm/network/LoadingLimits.html)

Some equipment have operational limits regarding the current, active power or apparent power value, corresponding to the equipment's physical limitations (related to heating).

Loading limits can be declined into active power limits (in MW), apparent power limits (in kVA) and current limits (in A).
They may be set for [lines](#line),
[dangling lines](#dangling-line), [two windings transformers](#two-windings-transformer) and [three windings transformers](#three-windings-transformer). The active power limits are in absolute value.

Loading limits are defined by one permanent limit and any number of temporary limits (zero or more).
The permanent limit sets the current, active power or apparent power absolute value under which the equipment can safely
be operated for any duration.
The temporary limits can be used to define higher current, active power or apparent power limitations corresponding
to specific operational durations.
A temporary limit thus has an **acceptable duration**.

The component on which the current limits are applied can safely remain
between the preceding limit (it could be another temporary limit or a permanent limit) and this limit for a duration up to the acceptable duration.
Please look at this scheme to fully understand the modelling (the following example shows current limits but this modelling is valid for all loading limits):

![Loading limits model](img/currentLimits.svg){: width="50%" .center-image}

Note that, following this modelling, in general the last temporary limit (the higher one in value) should be infinite with an acceptable duration different from zero, except for tripping current modeling where the last temporary limit is infinite with an acceptable duration equal to zero. If temporary limits are modeled, the permanent limit becomes mandatory.

### Limit group collection
In network development studies or in an operational context (CGMES), we can have a set of operational limits according to the season (winter vs summer for example), the time of the day (day vs night) etc.
In PowSyBl, users can store a collection of limits:
- Active power limits, apparent power limits and current limits are gathered into an `OperationalLimitsGroup` object. This group has an `id`.
- Lines and transformers are associated with a collection of `OperationalLimitsGroup` (one collection per side/leg).
  Users can then choose the active set according to their needs.

## Phase tap changer
[![Javadoc](https://img.shields.io/badge/-javadoc-blue.svg)](https://javadoc.io/doc/com.powsybl/powsybl-core/latest/com/powsybl/iidm/network/PhaseTapChanger.html)

A phase tap changer can be added to either [two windings transformers](#two-windings-transformer) or [three windings transformers' legs](#three-windings-transformer-leg).

**Specifications**

A phase tap changer is described by a set of tap positions (or steps) within which the transformer or transformer leg can operate. Additionally to that set of steps, it is necessary to specify:
- the lowest tap position
- the highest tap position
- the position index of the current tap (which has to be within the highest and lowest tap position bounds)
- whether the tap changer is regulating or not
- the regulation mode, which can be `CURRENT_LIMITER`, `ACTIVE_POWER_CONTROL` or `FIXED_TAP`: the tap changer either regulates the current or the active power.
- the regulation value (either a current value in `A` or an active power value in `MW`)
- the regulating terminal, which can be local or remote: it is the specific connection point on the network where the setpoint is measured.
- the target deadband, which defines a margin on the regulation so as to avoid an excessive update of controls

The phase tap changer can always switch tap positions while loaded, which is not the case of the ratio tap changer described below.

<!---
<span style="color:red"> TODO: check what happens when setting `isRegulating` to true and `FIXED_TAP` as regulating mode</span>
-->

Each step of a phase tap changer has the following attributes:

| Attribute | Unit | Description |
| --------- | ---- | ----------- |
| $r_{\phi, tap}$ | % | The resistance deviation in percent of nominal value |
| $x_{\phi, tap}$ | % | The reactance deviation in percent of nominal value |
| $g_{\phi, tap}$ | % | The conductance deviation in percent of nominal value |
| $b_{\phi, tap}$ | % | The susceptance deviation in percent of nominal value |
| $\rho_{\phi, tap}$ | p.u. | The voltage ratio in per unit of the rated voltages |
| $\alpha_{\phi, tap}$ | $^{\circ}$ | Angle difference |

## Ratio tap changer
[![Javadoc](https://img.shields.io/badge/-javadoc-blue.svg)](https://javadoc.io/doc/com.powsybl/powsybl-core/latest/com/powsybl/iidm/network/RatioTapChanger.html)

A ratio tap changer can be added to either [two windings transformers](#two-windings-transformer) or [three windings transformers' legs](#three-windings-transformer-leg).

**Specifications**

A ratio tap changer is described by a set of tap positions (or steps) within which the transformer or transformer leg can operate (or be operated offload). Additionally to that set of steps, it is necessary to specify:
- the lowest tap position
- the highest tap position
- the position index of the current tap (which has to be within the highest and lowest tap position bounds)
- whether the tap changer is regulating or not
- the regulation mode, which can be `VOLTAGE` or `REACTIVE_POWER`: the tap changer either regulates the voltage or the reactive power
- the regulation value (either a voltage value in `kV` or a reactive power value in `MVar`)
- the regulating terminal, which can be local or remote: it is the specific connection point on the network where the setpoint is measured.
- the target deadband, which defines a margin on the regulation so as to avoid an excessive update of controls
- whether the ratio tap changer can change tap positions onload or only offload


Each step of a ratio tap changer has the following attributes:

| Attribute | Unit | Description |
| --------- | ---- | ----------- |
| $r_{r, tap}$ | % | The resistance deviation in percent of nominal value |
| $x_{r, tap}$ | % | The reactance deviation in percent of nominal value |
| $g_{r, tap}$ | % | The conductance deviation in percent of nominal value |
| $b_{r, tap}$ | % | The susceptance deviation in percent of nominal value |
| $\rho_{r, tap}$ | p.u. | The voltage ratio in per unit of the rated voltages |