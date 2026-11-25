# Limitations

The PowSyBl State Estimation API is still under active development. This section lists its current
limitations and how future releases are expected to address them.

## Tooling and serialization

No JSON serialization. Every sibling API has it and this one should too.

No command-line tool. A tool needs an implementation to invoke, and there is none yet.

## Bad data removal

`detectBadData` reports suspects. It never removes a measurement or reweights one.

Removing a suspect reading and estimating again is a loop, and choosing which reading to drop is
an operating policy. A caller drives it with the operations already here:

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

A convenience method that runs this loop with one fixed policy could be added later, next to the
three operations.

## Only measurement errors

`detectBadData` covers errors in the measurements themselves. Two other error classes exist and
are not reported here:

- Topology errors, where a recorded switch state is wrong.
- Parameter errors, where a line impedance does not match reality.

Each is detected by a different method, so they would arrive as their own operations on the
provider. `BadDataResult` is extendable in the meantime, so an implementation that already
detects them can attach what it found.

## Numerical observability

`analyseObservability` does not say which way an implementation determines observability, and
both ways fit behind the interface as it stands. A topological analysis walks the measurement
graph. A numerical one builds the gain matrix and examines its rank, which means doing most of an
estimation before answering.

`StateEstimationParameters` has no setting for the method either. An implementation that offers a
choice defines one in its own parameters extension, contributed through
`StateEstimationProvider.getSpecificParametersClass()`.

The topological approach comes from Krumpholz, Clements and Davis, *Power System Observability: A
Practical Algorithm Using Network Topology* (IEEE Trans. PAS-99(4), 1534-1542, 1980). The numerical
one, and the observable islands it reports, come from a pair of companion papers published
together five years later: Wu and Monticelli, *Network Observability: Theory*, and Monticelli and
Wu, *Network Observability: Identification of Observable Islands and Measurement Placement* (IEEE
Trans. PAS-104(5), 1042-1048 and 1035-1041, 1985).

A numerical analysis builds the same factorization twice, once to answer the observability
question and again when `estimate` runs. An implementation could avoid that by caching the
factorization against the variant it was built from, with no change to the interface, though
this has not been tried against a working implementation.

## Estimators with a time dimension

A tracking or Kalman-filter estimator carries state and covariance from one run into the next, and
nothing here passes anything between calls.

`StateEstimationResult` is extendable, so an implementation can attach whatever it needs to carry
as its own extension. What is missing is a way to hand a previous result back in, which would be
one field on `StateEstimationRunParameters`. It is left out until a tracking estimator exists to
design that input against.

## Measurement preprocessing is not shared

An estimator works on quantities at buses, while IIDM measurements are attached to devices. Every
implementation has to move one to the other, and this module gives it no code for that. There is
no bus-level measurement type in core, and the IIDM extensions stay the only measurement model.

The one choice in that step that changes the answer is shared, as `busInjectionPolicy` in
`StateEstimationParameters`: whether a bus injection is formed at all when some of the devices on
the bus have no meter. Sharing the aggregation itself would mean publishing a bus-level
measurement type, which is a larger change than this API.

## Voltage angle has no redundancy or accuracy entry

`Quantity` has `ACTIVE_POWER`, `REACTIVE_POWER` and `VOLTAGE`, copied from
what `InjectionObservability` and `BranchObservability` already model. Voltage angle is absent
from those extensions, and is absent here for the same reason. A `QuantityRedundancy` and a
`QuantityAccuracy` both name what they describe by a `Quantity`, so the standard deviation of an
estimated angle has no entry to go in.

Angle is half of what a state estimation computes, and the `isObservable()` boolean on the same
extensions is documented as covering it, so the boolean answers for four quantities while the
entries describe three. Adding a fourth value here would put this API out of step with the
extensions a provider publishes to, so the two should change together if they change.

## Observability recovery

`analyseObservability` reports which parts of the network the measurements leave undetermined, and
`UnobservablePolicy` says what `estimate` does about it. Neither substitutes a value for the
missing data.

Which substitution is appropriate differs between operators, so it stays in the caller's code,
next to the bad data loop above. A per voltage level IIDM extension has been proposed for
recording which method applies where.

## Observability is not written to the network

`writeResultsToNetwork` puts the estimated voltages onto the buses and the flows onto the
terminals. Nothing puts an `ObservabilityResult` onto the IIDM observability extensions.

The two models line up for it. `QuantityRedundancy` and `QuantityAccuracy` carry between them
what `ObservabilityQuality` holds, `IslandStatus` has the same three values as
`ObservabilityArea.ObservabilityStatus`, and `ObservabilityAreaAdder` takes a bus view bus id,
which is what `ObservableIsland` holds. So a `writeObservabilityToNetwork` would be a mapping
rather than a computation. It would take the estimate along with the observability result, since
the extensions set a redundancy flag only together with a standard deviation.

Adding it is a default method, plus a `run` that calls `analyseObservability`, `estimate` and the
two writes, and a dependency on `iidm-extensions`. Nothing already in the API would change.
