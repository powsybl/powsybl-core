# Adders by copy

Adders by copy allow users to quickly and easily create objects by taking existing ones as "models" and then changing what needs to be changed before adding the object to the network.

At the moment, adders by copy are available for the objects listed below.

## Lines

You can create a [`Line`](network_subnetwork.md#line) object from another existing [`Line`](network_subnetwork.md#line) object.
Some characteristics of the new [line](network_subnetwork.md#line) are pre-filled with the characteristics of the "model" [line](network_subnetwork.md#line):
- R
- X
- G1 and G2
- B1 and B2
- The [voltage levels](network_subnetwork.md#voltage-level) at each side of the [line](network_subnetwork.md#line)

Operational limits are also copied from the model line.

Other attributes of the new [line](network_subnetwork.md#line) may (or should, for mandatory ones) be filled up by the user.

NB: it is mandatory for the user to fill up the `id` attribute before calling the `add()` method or else the code will throw an exception.

In the following example, the id, the buses and the connectable buses are set by the user before the new object is added to the network through the `add()` method.

```java
network.newLine(network.getLine("existingLineId"))
        .setId("newLineId")
        .setBus1(newLineBus1)
        .setBus2(newLineBus2)
        .setConnectableBus1(newLineBus1)
        .setConnectableBus2(newLineBus2)
        .add();
```

NB : the pre-filled characteristics could also be modified if needed.

## Two-winding transformers

You can create a [`TwoWindingsTransformer`](network_subnetwork.md#two-winding-transformer) object from another existing [`TwoWindingsTransformer`](network_subnetwork.md#two-winding-transformer) object.
Some characteristics of the new [two-winding transformer](network_subnetwork.md#two-winding-transformer) are pre-filled with the characteristics of the "model" [two-winding transformer](network_subnetwork.md#two-winding-transformer):
- R
- X
- G
- B
- ratedU1
- ratedU2

Operational limits are also copied from the existing transformer.


Other attributes may (or should, for mandatory ones) be filled up by the user.

NB: it is mandatory for the user to fill up the `id` attribute before calling the `add()` method or else the code will throw an exception.

In the following example, the id, the buses and the ratedS attribute are set by the user before the new object is added to the network through the `add()` method.

```java
TwoWindingsTransformer transformer2 = substation.newTwoWindingsTransformer(transformer1)
        .setId("twt2")
        .setRatedS(7.0)
        .setBus1("busA")
        .setBus2("busB")
        .add();
```

NB : the pre-filled characteristics could also be modified if needed.

## Ratio tap changers

You can create a [`RatioTapChanger`](additional.md#ratio-tap-changer) object from another existing [`RatioTapChanger`](additional.md#ratio-tap-changer) object.
Some characteristics of the new [`RatioTapChanger`](additional.md#ratio-tap-changer) are pre-filled with the characteristics of the "model" [`RatioTapChanger`](additional.md#ratio-tap-changer):
- The regulation terminal
- The regulation mode
- The regulation value
- The`loadTapChangingCapabilities` value
- The `targetV` value
- The `lowTapPosition` value
- The `tapPosition` value
- The `regulating` value
- The `targetDeadBand` value

Besides that, the following characteristics for all the `RatioTapChangerStep` steps of the existing [`RatioTapChanger`](additional.md#ratio-tap-changer) are also copied to create the new object:
- rho
- b
- g
- x
- r

In the following example, a new ratio tap changer `newRatioTapChanger` is created from the `existingRatioTapChanger` object and added to a two-winding transformer through the `add()` method.

```java
RatioTapChanger newRatioTapChanger = network.getTwoWindingsTransformer("transformerId")
        .newRatioTapChanger(existingRatioTapChanger)
        .add();
```

NB : the pre-filled characteristics could also be modified if needed.


## Phase tap changers

You can create a [`PhaseTapChanger`](additional.md#phase-tap-changer) object from another existing [`PhaseTapChanger`](additional.md#phase-tap-changer) object.
Some characteristics of the new [`PhaseTapChanger`](additional.md#phase-tap-changer) are pre-filled with the characteristics of the "model" [`PhaseTapChanger`](additional.md#phase-tap-changer):
- The regulation terminal
- The regulation mode
- The regulation value
- The `lowTapPosition` value
- The `tapPosition` value
- The `regulating` value
- The `targetDeadBand` value

Besides that, the following characteristics for all the `PhaseTapChangerStep` steps of the existing [`PhaseTapChanger`](additional.md#phase-tap-changer) are also copied to create the new object:
- alpha
- rho
- b
- g
- x
- r

In the following example, a new phase tap changer `newPhaseTapChanger` is created from the `existingPhaseTapChanger` object and added to a two-winding transformer through the `add()` method.

```java
RatioTapChanger newPhaseTapChanger = network.getTwoWindingsTransformer("transformerId")
        .newPhaseTapChanger(existingPhaseTapChanger)
        .add();
```

NB : the pre-filled characteristics could also be modified if needed.

## Reactive capability curve

You can create a [`ReactiveCapabilityCurve`](additional.md#reactive-capability-curve) object from another existing  [`ReactiveCapabilityCurve`](additional.md#reactive-capability-curve) object.
The points of the new  [`ReactiveCapabilityCurve`](additional.md#reactive-capability-curve) are copied from the points of the "model"  [`ReactiveCapabilityCurve`](additional.md#reactive-capability-curve).

In the following example, a reactive capability curve is created from the `existingReactiveCapabilityCurve` and added to a [Generator](network_subnetwork.md#generator) object:

```java
generator.newReactiveCapabilityCurve(existingReactiveCapabilityCurve)
        .add();
```

NB : other points could be added to the new object if needed.

## Loading limits

You can add limits to an [OperationalLimitsGroup](additional.md#limit-group-collection) by copying existing limits. The following elements are copied:
- The permanent limit value;
- For all the temporary limits: their name, value, acceptable duration and whether they are flagged as fictitious.

In the following example, `CurrentLimits`, `ActivePowerLimits` and `ApparentPowerLimits` are added to an [OperationalLimitsGroup](additional.md#limit-group-collection) object from existing objects:

```java
operationalLimitsGroup.newCurrentLimits(existingCurrentLimits).add();
operationalLimitsGroup.newActivePowerLimits(existingActivePowerLimits).add();
operationalLimitsGroup.newCurrentLimits(existingApparentPowerLimits).add();
```

It is also possible to use a higher-level function to copy operational limits from an existing [Line](network_subnetwork.md#line) `existingLine` to a new [Line](network_subnetwork.md#line) `otherLine`:

```java
LoadingLimitsUtil.copyOperationalLimits(existingLine, otherLine);
```