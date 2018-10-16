# IIDM - Current Limits

The `com.powsybl.iidm.network.CurrentLimits` interface is used to model current limits for [branches](branch.md), [dangling lines](danglingLine.md)
and [three windings transformers](threeWindingsTransformer.md).
Current limits are defined by:
- a permanent limit
- any number of temporary limits

## Permanent Limits
A permanent limit is modeled by a double.

## Temporary Limits
A temporary limit has an **acceptable duration**. The component on which the current limits are applied to can safely stayed
between the previous limit (could be another temporary limit or a permanent limit) and this limit during the acceptable duration.
A Nan value for a temporary limit means infinite.

## Examples
This example shows how to create a new CurrentLimits object:
```java
CurrentLimits currentLimits = network.getDanglingLine('DL').newCurrentLimits()
    .setPermanentLimit(100.0)
    .beginTemporaryLimit()
        .setName('TL1')
        .setValue(120.0)
        .setAcceptableDuration(20 * 60)
    .endTemporaryLimit()
    .beginTemporaryLimit()
        .setName('TL2')
        .setValue(140.0)
        .setAcceptableDuration(10 * 60)
    .endTemporaryLimit()
    .add();
```

## References
See also:
- [Branch](branch.md)
- [Dangling Line](danglingLine.md)
- [Three Windings Transformer](threeWindingsTransformer.md)