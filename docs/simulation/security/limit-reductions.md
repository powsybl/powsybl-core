# Limit reductions

## General description

Limit reductions can be specified in order to detect when a specific limit is **nearly** reached, without having to artificially modify the limit itself.
For instance, with a limit reduction set to 95% for a limit of 1000 MW, the security analysis will flag a limit violation for any value exceeding 950 MW.

Each limit reduction has its own criteria specifying for which limits and under what conditions it should be applied. These criteria can include:
- the type of limit (current, active power or apparent power)
- the use case: for monitoring only or also for applying remedial actions
- the contingency context (pre-contingency, after a specific contingency or after all contingencies, etc.)
- the network elements targeted by the reduction (branches, three-winding transformers, ...), which can be described by the following criteria:
    - a set of their ids;
    - their countries;
    - their nominal voltages.
- which operational limits are affected by the reduction:
    - the severity of the limit: permanent or temporary;
    - and for temporary limits, their acceptable duration:
        - equal to a specific value;
        - inside an interval.

These criteria can be cumulative; multiple criteria can be used simultaneously to define a limit reduction.

Since a network operational limit may meet the criteria of several limit reductions, the order in which these reductions
are declared is important: the last one encountered when reading them from start to finish is applied.

## Criteria details

### Limit type

The type of limits targeted by the reduction must be specified (mandatory item). The supported types are:
- `CURRENT`: for current limits;
- `ACTIVE_POWER`: for active power limits;
- `APPARENT_POWER`: for apparent power limits.


### Use cases (monitoring or action)

The reduction may affect results:

1. Monitoring only (`monitoringOnly` set to `true`) means that if reductions are provided in a security analysis, only
reported violations are affected.

2. Else (`monitoringOnly` set to `false`) if reductions are provided in a security analysis, they affect not only the 
reported violations but also the conditions for applying remedial actions.


### Contingency context

A contingency context can be optionally specified. It contains:
- a type among:
    - `ALL`: corresponding to all contingencies and pre-contingency situations;
    - `NONE`: corresponding to pre-contingency situations;
    - `SPECIFIC`: corresponding to a specific contingency situation;
    - `ONLY_CONTINGENCIES`: corresponding to all contingency situations (without the pre-contingency one).
- and when the type is `SPECIFIC`, the id of the contingency.

When no contingency context is present, the `ALL` policy is used.


### Network elements

The network elements whose limits will be affected by the limit reductions can be selected in using several criteria:
- a set of the network elements' ids;
- one or two countries (respectively for elements with one or two substations);
- their nominal voltages, by defining an interval for each of the voltage levels.

If no network elements are specified, the limit reduction applies to all of them.


### Limit duration criteria

Duration criteria can be optionally specified. It contains:
- a type among:
    - `PERMANENT`: corresponding to permanent limits only;
    - `TEMPORARY`: corresponding to temporary limits only.
- and when the type is `TEMPORARY`, one of the following options to restrict them accordingly to their acceptable duration:
    - `ALL`: to select all temporary limits, regardless their acceptable duration;
    - `EQUALITY`: to select the temporary limits whose acceptable duration is equal to a specified value, with:
        - `value`: the said value;
    - `INTERVAL`: to select the temporary limits whose acceptable duration is within an interval, with:
        - `lowBound` and `highBound`: minimum and maximum duration, each can be null;
        - `lowClosed` and `highClosed`: to indicate if the interval is open (`false`) or closed (`true`) on respectively the lower and the upper boundaries.
      This attribute is facultative if the corresponding bound value is `null`.

When no duration criteria are present, the reduction is applied to all permanent and temporary limits.

When several duration criteria are specified, the limit reductions apply to each one. 
For instance, if both criteria `PERMANENT` and (`TEMPORARY` ; `EQUALITY`: 600) are defined, the limit reduction will apply to permanent limits and 600 s limits.
