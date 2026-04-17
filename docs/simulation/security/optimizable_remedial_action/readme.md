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

# Design choices and examples

## Optimizable Remedial Action

The optimizer will choose the optimal set point of the actions.

This means that we need a new object to represent the operator strategy not yet optimized "OptimizableOperatorStrategy".




TODO:
- write this page
- add examples
