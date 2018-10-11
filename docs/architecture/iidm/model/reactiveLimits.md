# IIDM - Reactive limits

The `com.powsybl.iidm.network.ReactiveLimits` interface is used to model limitations of the reactive power of
[generators](generator.md) and [VSC converter stations](vscConverterStation.md).

## MinMaxReactiveLimits

The `com.powsybl.iidm.network.MinMaxReactiveLimits` class is a `ReactiveLimits` implementation where the reactive power
does not depend on the active power. For any active power value, the reactive power value is in [minQ, maxQ] interval.

## ReactiveCapabilityCurve

The `com.powsybl.iidm.network.ReactiveCapabilityCurve` class is a `ReactiveLimits` implementation where the reactive power
depends on the active power. The curve is made of `Point` and each point defines the minimum and maximum reactive limits
for a given active power.

## Examples

This example shows how to use the `MinMaxReactiveLimits` and `ReactiveCapabilityCurve` classes:
```java
Generator generator = network.getGenerator("G");
if (generator.getReactiveLimits().getKind() == ReactiveLimitsKind.MIN_MAX) {
    MinMaxReactiveLimits limits = generator.getReactiveLimits(MinMaxReactiveLimits.class);
    System.out.println("MinMaxReactiveLimits: [" + limits.getMinQ() + ", " + limits.getMaxQ() + "]");
} else {
    ReactiveCapabilityCurve limits = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
    System.out.println("ReactiveCapabilityCurve:");
    limits.getPoints().forEach(p -> System.out.println("\t" + p.getP() + " -> [" + p.getMinQ() + ", " + p.getMaxQ() + "]"));
}
```

This example shows how to create a new `MinMaxReactiveLimits` object:
```java
Generator generator = network.getGenerator("G");
generator.newMinMaxReactiveLimits()
    .setMinQ(-100.0)
    .setMaxQ(100.0)
    .add();
```

This example shows how to create a new `ReactiveCapabilityCurve` object:
```java
Generator generator = network.getGenerator("G");
generator.newReactiveCapabilityCurve()
    .beginPoint()
        .setP(-10)
        .setMinQ(-10)
        .setMaxQ(10)
    .endPoint()
    .beginPoint()
        .setP(0)
        .setMinQ(-20)
        .setMaxQ(20)
    .endPoint()
    .beginPoint()
        .setP(10)
        .setMinQ(-15)
        .setMaxQ(-15)
    .endPoint()
    .add();
```

## References
See also:
- [Generator](generator.md)
- [VscConverterStation](hvdcline.md)
