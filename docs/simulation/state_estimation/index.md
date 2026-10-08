# State estimation

A state estimation finds the network state that best explains a set of meter readings. The
readings carry noise and there are normally more of them than there are unknowns, so the
estimator fits one state to all of them at once rather than solving any single reading exactly.
What comes out is a voltage magnitude and angle at every bus, together with how far each reading
is from what that state implies.

A load flow needs a complete and consistent input, which a set of real measurements is not. The
estimate provides one, and it is what a security analysis and the other operational applications
are run against.

## The three operations

```java
ObservabilityResult observability = StateEstimation.analyseObservability(network);
StateEstimationResult estimate = StateEstimation.estimate(network);
BadDataResult badData = StateEstimation.detectBadData(network, estimate, observability);
```

`analyseObservability` reports which parts of the network the measurement set determines.
`estimate` computes the state. `detectBadData` tests an estimate for readings that disagree
with the rest.

They are separate calls because the sequence between them is an operating decision. An operator
who finds part of the network unobservable might add distribution system measurements, fall back
on pseudo-measurements from a forecast, or reduce the area being estimated. Which of those
to try, and in what order, differs between operators, so the API gives the steps and the caller
writes the sequence.

`detectBadData` takes an estimate it did not compute, so a caller that loops between estimating
and testing pays for the estimate once per iteration. It also takes an observability result,
because a measurement that nothing else determines has a residual of zero whatever its value, so
no test can tell whether it is correct.

All three come from one provider, so choosing an implementation by name chooses all three
together, and one estimator's observability analysis cannot be combined with another's solver.

## Implementations

For the moment, there is no available implementation of this API.

```{toctree}
---
caption: State estimation
maxdepth: 2
hidden: true
---

limitations.md
```
