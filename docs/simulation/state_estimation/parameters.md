# Parameters

## Available parameters

The parameters are defined in the `config.yml` file. For example:

```yaml
state-estimation-default-parameters:
  voltageInitMode: UNIFORM_VALUES
  zeroInjectionMode: HIGH_CONFIDENCE_MEASUREMENT
  busInjectionPolicy: REQUIRE_ALL_METERED
  duplicateMeasurementPolicy: MERGE_INVERSE_VARIANCE
  unobservablePolicy: ESTIMATE_OBSERVABLE_ISLANDS
  componentMode: MAIN
  relativeStandardDeviationFloor: 0.0
  residualFlaggingThreshold: 3.0
  writeResultsToNetwork: false
```

They are stored in `com.powsybl.stateestimation.StateEstimationParameters` and are all optional.

Only settings that change what the estimate means are here. A convergence tolerance, an iteration
cap or a choice of linear solver changes how an implementation reaches the answer rather than
which answer is correct, so it belongs in an extension that implementation contributes through
`StateEstimationProvider.getSpecificParametersClass()`.

(param-se-voltage-init-mode)=
### voltageInitMode

Where the iteration starts from:
- `UNIFORM_VALUES`: 1 pu and 0 radian at every bus.
- `PREVIOUS_VALUES`: the voltages the network already holds.
- `DC_VALUES`: angles from a DC load flow, magnitudes at 1 pu.

A well-conditioned estimation lands on the same answer from any of them, so the choice changes
the iteration count and not the result. It matters on a network where the measurement set
leaves the problem nearly singular.

The default value is `UNIFORM_VALUES`.

(param-se-zero-injection-mode)=
### zeroInjectionMode

How a bus with no device attached is handled. Such a bus injects exactly zero, which is known
without any meter, and there are usually many of them.

- `IGNORED`: treated like any other unmeasured bus.
- `HIGH_CONFIDENCE_MEASUREMENT`: added as a zero-valued measurement with a very small standard
  deviation.
- `EQUALITY_CONSTRAINT`: enforced exactly, so no weight is involved.

The three answers differ. A very small standard deviation makes the gain matrix badly conditioned
and a value known to be exact still moves under the fit, while an equality constraint holds it and
changes the system being solved.

The default value is `HIGH_CONFIDENCE_MEASUREMENT`.

(param-se-bus-injection-policy)=
### busInjectionPolicy

An estimator works on quantities at buses, while IIDM measurements are attached to devices. This
setting says how a bus injection is formed when only some of the devices on the bus are metered:
- `REQUIRE_ALL_METERED`: form a bus injection only where every connected device has a meter.
- `SUM_METERED`: sum the metered devices, treating the others as zero.

`SUM_METERED` gives the wrong injection whenever an unmetered device carries power. A bus with a
metered 100 MW generator and an unmetered 30 MW load injects 70 MW, and `SUM_METERED` reports
100 MW. It is there for networks where the unmetered devices are known to be negligible.

The setting also changes observability, since a bus with no injection measurement is one fewer
equation.

The default value is `REQUIRE_ALL_METERED`.

(param-se-duplicate-measurement-policy)=
### duplicateMeasurementPolicy

What happens when the same quantity at the same place is measured more than once:
- `MERGE_INVERSE_VARIANCE`: combined into one reading weighted by the inverse of each variance.
- `KEEP_ALL`: every reading contributes its own row.
- `THROW`.

`KEEP_ALL` and `MERGE_INVERSE_VARIANCE` give the same state when the readings are independent.
They differ once the readings share a source, and they differ in the residuals they report,
which is what a bad data test then works from.

The default value is `MERGE_INVERSE_VARIANCE`.

(param-se-unobservable-policy)=
### unobservablePolicy

What `estimate` does when the measurements leave part of the network undetermined:
- `THROW`.
- `SKIP_COMPONENT`: report the component as unobservable and estimate the others.
- `ESTIMATE_OBSERVABLE_ISLANDS`: estimate each observable island of the component separately.

A caller that runs `analyseObservability` first already knows the answer before calling
`estimate`, so this setting mostly covers the case where it does not. `THROW` suits an automated
pipeline where publishing a partial estimate is worse than failing.

The default value is `ESTIMATE_OBSERVABLE_ISLANDS`.

(param-se-component-mode)=
### componentMode

Whether to estimate the main connected component only (`MAIN`) or all of them (`ALL`).

The default value is `MAIN`.

(param-se-relative-standard-deviation-floor)=
### relativeStandardDeviationFloor

A lower bound on a measurement's standard deviation, as a fraction of the measured value. One
reading that claims near-perfect accuracy gets a weight large enough to pull the whole estimate
onto itself, and the floor stops that.

It is relative so that one number covers megawatts, kilovolts and amperes alike. The weakness is
that the floor shrinks with the reading and is zero at a zero reading, so a near-zero measurement
with an unrealistically small standard deviation is not caught.

`0.0`, the default, applies no floor. Negative values are rejected.

(param-se-residual-flagging-threshold)=
### residualFlaggingThreshold

A measurement is flagged once its normalized residual exceeds this multiple of its own standard
deviation. Flagging reports a suspicion. Nothing is removed or reweighted.

The default value is `3.0`, which is the usual threshold for a normalized residual test. Values
must be positive.

(param-se-write-results-to-network)=
### writeResultsToNetwork

Whether the estimated state is written back into the network. The result carries it either way.
Leaving this off also leaves the voltages already on the network untouched, which matters when
only part of a component was estimated: the buses that were not keep whatever was there before,
and only the result distinguishes them.

The default value is `false`.
