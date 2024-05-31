---
layout: default
---

# Security analysis API

The `com.powsybl.security.SecurityAnalysisImpl` class is an implementation of the `com.powsybl.security.SecurityAnalysis` interface that detects security violations by running load-flows after applying each contingency. This implementation can be very slow if the contingency list contains a lot of contingencies, thus triggering a lot of load-flow computations, but it has the advantage of only requiring a `com.powsybl.loadflow.LoadFlow`. The default loadflow implementation is used, see [loadFlow](../configuration/modules/loadflow.md) for details about how default implementation is determined.

