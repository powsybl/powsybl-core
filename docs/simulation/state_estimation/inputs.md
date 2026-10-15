# Inputs

## A network

Every operation takes a network, and nothing else is required. The measurements are already on
it.

## Measurements

Readings are read from the [measurements extension](../../grid_model/extensions.md#measurements)
attached to the equipment they were taken on. Each `Measurement` gives a type, a value, a
standard deviation, a validity flag, and a side when the equipment has more than one.

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

The standard deviation is what makes a reading count for more or less than its neighbors. It is
the only input the estimator has about how far a meter can be trusted, so a measurement left with
`NaN` there has to be weighted by the implementation's own rule.

Two things follow from reading measurements off the network rather than passing them in.

A measurement set is tied to the variant it sits in, so a second snapshot is a second variant.
Every entry point names the variant it runs against:

```java
network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "snapshot");
// write the readings of that snapshot into the "snapshot" variant
StateEstimationResult result = StateEstimation.estimate(network, "snapshot",
        StateEstimationRunParameters.getDefault());
```

Where the readings came from is outside this API. A SCADA export, a CGMES file and a message
stream all end at the same place, which is a `Measurement` on a piece of equipment.

## Parameters

Optional. `StateEstimationParameters` holds the settings that are shared between implementations,
and an implementation adds its own as an extension. See [parameters](parameters.md).

```java
StateEstimationParameters parameters = new StateEstimationParameters()
        .setZeroInjectionMode(StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT)
        .setResidualFlaggingThreshold(2.5);

StateEstimationResult result = StateEstimation.estimate(network, parameters);
```
