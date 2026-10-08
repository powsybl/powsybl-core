# Limitations

Six things this API leaves out. Each has a way forward that does not mean redesigning what is
here.

## Bad data removal

`detectBadData` reports and flags. It never removes a measurement or reweights one.

Removing a suspect reading and estimating again is a loop, and choosing which reading to drop is
the same kind of operating policy as the observability fallbacks. A caller drives it with the
operations already here:

```java
ObservabilityResult observability = StateEstimation.analyseObservability(network);
StateEstimationResult estimate = StateEstimation.estimate(network);
BadDataResult badData = StateEstimation.detectBadData(network, estimate, observability);

while (badData.getStatus() == BadDataResult.Status.BAD_DATA_DETECTED) {
    // the caller decides what to do with the worst suspect, then estimates again
    invalidate(network, badData.getSuspectMeasurements().get(0).getMeasurementId());
    estimate = StateEstimation.estimate(network);
    badData = StateEstimation.detectBadData(network, estimate, observability);
}
```

A convenience method that runs this loop with one fixed policy could be added later. It would
belong next to the three operations rather than inside any of them.

## Only measurement errors

`detectBadData` covers errors in the measurements themselves. Two other error classes exist and
are not reported here:

- Topology errors, where a recorded switch state is wrong.
- Parameter errors, where a line impedance does not match reality.

Both are detected by different methods and produce different outputs, so one call covering three
kinds of error would return a result that is hard to read. They would arrive as their own
operations on the provider. `BadDataResult` is extendable in the meantime, so an implementation
that already detects them can attach what it found.

## Numerical observability

Determining observability numerically does not force `analyseObservability` and `estimate` to
become one call. Both ways of determining it fit behind the interface as it stands.

`analyseObservability` does not say which way an implementation uses. A topological analysis walks
the measurement graph. A numerical one builds the gain matrix and examines its rank, which means
doing most of an estimation before answering.

What the numerical way costs is that the same factorization is built twice, once to answer the
observability question and again when `estimate` runs. That is a performance question inside an
implementation, and it does not reach the interface. Caching the factorization against the variant
it was built from would avoid it, though nothing here has been tried against a working
implementation. It is worth knowing before anyone writes the numerical variant.

## Estimators with a time dimension

A tracking or Kalman-filter estimator carries state and covariance from one run into the next, and
nothing here passes anything between calls. Of the six, this is a genuine gap in the interface.

The way forward does not need the network to hold hidden carry-over state.
`StateEstimationResult` is extendable, so an implementation can attach whatever it needs to carry,
typed as its own extension and serialized through its own serializer. What is missing is the other
direction, a way to hand a previous result back in, which is one field on
`StateEstimationRunParameters` and no change to anything else.

It is left out because adding an input that nothing reads would be guessing at what a tracking
estimator actually needs. It should be designed alongside the first implementation that wants it.

## Measurement preprocessing is not shared

An estimator works on quantities at buses, while IIDM measurements are attached to devices. Every
implementation has to move one to the other, and this module gives it no code for that. There is
no bus-level measurement type in core, and the IIDM extensions stay the only measurement model.

The one choice in that step that changes the answer is shared, as `busInjectionPolicy` in
`StateEstimationParameters`: whether a bus injection is formed at all when some of the devices on
the bus have no meter. Sharing the aggregation itself would mean publishing a bus-level
measurement type, which is a larger change than this API.

## Voltage angle is not an observability quantity

`ObservabilityResult.Quantity` has `ACTIVE_POWER`, `REACTIVE_POWER` and `VOLTAGE`, copied from
what `InjectionObservability` and `BranchObservability` already model. Voltage angle is absent
from those extensions, and is absent here for the same reason.

Angle is half of what a state estimation computes, and the `isObservable()` boolean on the same
extensions is documented as covering it, so the boolean answers for four quantities while the
qualities describe three. Adding a fourth value here would put this API out of step with the
extensions a provider publishes to, so the two should change together if they change.

## Also absent

No JSON serialization. Every sibling API has it and this one should too.

No command-line tool. A tool needs an implementation to invoke, and there is none yet.
