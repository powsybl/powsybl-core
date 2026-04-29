This folder contains some work about to better incorporate operator strategies optimizer in PowSyBl.

The base idea is to work around the concept of "operator strategy".
An operator strategy is composed of a set of actions applied in a given context after some conditions.
Those actions have a fixed set point and have been already optimized.

We want to optimize the set point of the actions and which actions are applied.
This optimization is already done by several algorithms, but they do not share a common interface.
We want to standardize this interface.

# General remarks

- All names are not final, they are just placeholders and can be changed.
- This is a work in progress, it has not been reviewed, validated or tested.
- The units are the ones of PowSyBl (power in MW and times in seconds)

# Design choices and examples

## Optimizable Remedial Action

The optimizer will choose the optimal set point of the actions.

This means that we need a new object to represent the operator strategy not yet optimized "OptimizableOperatorStrategy".

### Examples

#### Optimizable PST Range Action



TODO:
- write this page
- add examples


Notes en pagaille:
 - DMO/DP are equivalent to lead time.
 - Energy constraints require to define how to interpolate between timesteps.
  This will be done by the implementations.


Turn `RangeRemedialAction` into an interface? 3 useful use-cases:
1. `SimpleRangeRemedialAction` : only one `RangeAction` involved
2. `MultipleRangeRemedialAction` : several correlated `RangeAction`s involved, each having a distribution key
3. `GlskRangeRemedialAction` : `RangeAction`s are associated with a GLSK (linear or not) -> might require a network