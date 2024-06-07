---
layout: default
---

# Short-circuit API

The class `com.powsybl.shortcircuit.ShortCircuitAnalysis` is the main entry point to run a short-circuit analysis on a network.
It provides the static methods `run` and `runAsync`. These methods will run the computation and only differ in the
way they return the results. The `run` method returns the results directly and can be used in most
cases. The `runAsync` returns a `CompletableFuture` and can be used when a non-blocking computation is
preferred. The `ShortCircuitAnalysis` class doesn't implement the computation directly, but instead relies on a
`com.powsybl.shortcircuit.ShortCircuitAnalysisProvider` to implement it. This allows to use different
implementations with the same code.

**Note:** Powsybl does not provide an implementation for short-circuit analyses yet.
