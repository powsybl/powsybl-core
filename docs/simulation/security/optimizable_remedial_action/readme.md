# Goal

This folder contains some work about how to better incorporate operator strategies optimizer in PowSyBl.

The base idea is to work around the existing concept of "operator strategy".  
An operator strategy is composed of a set of actions applied in a given context after some conditions.  
Those actions have a fixed set point and have been already optimized.

We want to optimize which actions are applied and to which set point.  
This optimization is already done by several algorithms, but they do not share a common interface.  
We want to standardize this interface.

It is recommended to take a look at the
diagram [operatorStrategy_simple_future.puml](operatorStrategy_simple_future.puml).

# General remarks

- All names are not final, they are just placeholders and can be changed.
- This is a work in progress, it has not been reviewed, validated or tested.
- The physical units are the ones of PowSyBl (power in MW and time deltas in seconds).
- Objects with optional values are stored with a null value. The corresponding getter will return an optional.

## Known model limitations

- This operator strategy optimizer design is a bit verbose to define arbitrary set points ranges (you need to define a
  big union).
- Durée de mobilisation (DMO) and Durée de prévenance (DP) are equivalent to lead time.
- Energy constraints require to define how to interpolate between timesteps.  
  This will be done by the implementations.

# Work in progress

This section contains the list of questions that need to be answered before we can implement the feature.

## Model extension

We might not want to put all implementations of all interfaces in powsybl-core.

This means that we might need some ways to extend the model without modifying the core library.  
This can be tricky because of serialization and deserialization.  
To answer this need, we have not chosen between a modular design (jackson.databind.ObjectMapper) or an extension-based
design (for example).

## TODO

- (P1) Choose how to extend our model across multiple repositories?
- (P1) Redispatching needs saturation and merit order. How to manage non-linearities?
- (P2) How to add GLSK support?
- (P3) Check which UML formats readthedocs supports (puml, mermaid ?, ...)
- Finish writing doc
- expliciter is Relative

## Draft

