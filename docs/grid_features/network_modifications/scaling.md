# Scaling

[![Javadoc](https://img.shields.io/badge/-javadoc-blue.svg)](https://javadoc.io/doc/com.powsybl/powsybl-core/latest/com/powsybl/iidm/modification/scalable/Scalable.html)

The Scalable API in PowSyBl Core provides a flexible, composable framework for modifying active power injections:
- **Single-injection** scalable: Define scaling for generators, loads, and boundary lines. These are leaf nodes in the scalable model.
- **Proportional** scalable: ProportionalScalable holds a list of child scalables and a matching list of percentage weights.
- **Stack** scalable: StackScalable applies its children in order. The first child receives the full amount asked, the next receives the remaining unsatisfied power, and so on.
- **Up-Down** scalable: Combines two scalables, one scalable for upward scaling, a second scalable for downward scaling.

Above scalables can be composed and nested at will. One can for example create:
- A Proportional scalable as a list of loads and associated weights
- A Stack scalable as an ordered list of generators
- A Stack scalable as an ordered list of Proportional scalables
- An Up-Down scalable using a Stack scalable to be used for upward scaling and a Generator scalable for downward scaling.
- etc...

Scaling is then performed using the `scale` method of the scalable where the asked volume in MW must be provided,
optionally with the scaling parameters.

Interface: `Scalable`

## Scaling parameters

### scalingConvention
Defines the sign convention used in scaling:
- `GENERATOR`: A positive asked volume means increasing generation / decreasing load.
- `LOAD`: A positive asked volume means increasing load / decreasing generation.

The default value is `GENERATOR`.

### scalingType
Defines what the asked volume represents:
- `DELTA_P`: the asked volume is applied as a variation of active power relative to the current active power.
- `TARGET_P`: the asked volume is the target total active power.

The default value is `DELTA_P`.

### constantPowerFactor
When true, Load Scalable scales Q0 proportionally with P0 to maintain the original cos(φ) power factor.
The change of reactive power may be further constrained by [loadMinPowerFactor](#loadminpowerfactor),
[loadMinQRate](#loadminqrate), and [loadMaxQRate](#loadmaxqrate).

### reconnect
When true, disconnected generators/loads/boundary lines can be reconnected by scaling.

### priority
Controls saturation redistribution mode for ProportionalScalable.
- `RESPECT_OF_VOLUME_ASKED`: the scaling will distribute the power asked as much as possible by iterating if elements
get saturated, even if it means not respecting potential percentages.
- `RESPECT_OF_DISTRIBUTION`: the scaling will respect the percentages even if it means not scaling all that is asked.
- `ONESHOT`: the scaling will distribute the power asked as is, in one iteration even if elements get saturated and even
if it means not respecting potential percentages.

The default value is `ONESHOT`.

### ignoredInjectionIds
A list of injections to ignore in the scaling.

The default value is an empty list.

### loadMinPowerFactor
The minimum active/apparent power factor (i.e., P/S factor) allowed when scaling load reactive power Q.
Only applies when `constantPowerFactor` is `true`.

Normally, the reactive power Q is scaled proportionally to the active power P to keep the power factor constant.
If the initial power factor is below this value, Q is instead recomputed from the new P using this minimum power factor.
Default is `0.0` meaning that no minimum power factor is enforced. Must be in the range `[0, 1]`.

### loadMinQRate
The minimum allowed ratio between the scaled reactive power and the initial reactive power.
Only applies when `constantPowerFactor` is `true`.

Prevents Q from deviating too much from its initial value by enforcing that:
- `Q_scaled >= Q_initial * loadMinQRate` if `Q_initial >= 0`,
- or `Q_scaled <= Q_initial * loadMinQRate` otherwise.

Default is `null` meaning that no minimum Q rate limit is applied. Must be less than or equal to `1.0`.

`loadMinQRate` is applied after [`loadMinPowerFactor`](#loadminpowerfactor),
ensuring that reactive power does not change excessively even when power factor limits have already been enforced.

### loadMaxQRate
The maximum allowed ratio between the scaled reactive power and the initial reactive power.
Only applies when `constantPowerFactor` is `true`.

Prevents Q from deviating too much from its initial value by enforcing that:
- `Q_scaled <= Q_initial * loadMaxQRate` if `Q_initial >= 0`,
- or `Q_scaled >= Q_initial * loadMaxQRate` otherwise. 

Default is `null` meaning that no maximum Q rate limit is applied. Must be greater than or equal to `1.0`.

`loadMaxQRate` is applied after [`loadMinPowerFactor`](#loadminpowerfactor),
ensuring that reactive power does not change excessively even when power factor limits have already been enforced.
