# Inputs

## A network

Every operation takes a network, and nothing else is required. The measurements are already on
it.

Readings are read from the [measurements](../../grid_model/extensions.md#measurements) and
[discrete measurements](../../grid_model/extensions.md#discrete-measurements) extensions attached
to the equipment they were taken on. Each `Measurement` gives a type, a value, a standard
deviation, a validity flag, and a side when the equipment has more than one. Each
`DiscreteMeasurement` gives a type, such as a tap or a switch position, a value and a validity
flag. The topology an operation works from is still the one the variant holds, and what an
implementation does with a discrete reading is its own choice.

```java
line.newExtension(MeasurementsAdder.class).add();
line.getExtension(Measurements.class)
        .newMeasurement()
        .setId("MEAS_LINE_P_1")
        .setType(Measurement.Type.ACTIVE_POWER)
        .setSide(ThreeSides.ONE)
        .setValue(42.1)
        .setStandardDeviation(0.8)
        .setValid(true)
        .add();
```

The standard deviation is the only input the estimator has about how far a meter can be trusted,
so a measurement left with `NaN` there has to be weighted by the implementation's own rule.

Every entry point names the variant it runs against, because the topology and the voltages an
estimation works from are variant dependent:

```java
network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "snapshot");
// open a breaker, or set a different starting voltage, in the "snapshot" variant
StateEstimationResult result = StateEstimation.estimate(network, "snapshot",
        StateEstimationRunParameters.getDefault());
```

The measurement extensions are not variant dependent, so writing a second set of readings
replaces the first in every variant. Estimating two measurement snapshots side by side takes two
networks.

## Parameters

Optional. `StateEstimationParameters` holds the settings that are shared between implementations,
and an implementation adds its own as an extension. See [parameters](parameters.md).

```java
StateEstimationParameters parameters = new StateEstimationParameters()
        .setZeroInjectionMode(StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT)
        .setResidualThreshold(2.5);

StateEstimationResult result = StateEstimation.estimate(network, parameters);
```
