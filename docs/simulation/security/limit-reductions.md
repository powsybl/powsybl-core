# Limit reductions

## General description

Limit reductions can be specified in order to detect when a specific limit is **almost** reached, without having to change artificially this limit.
For instance, with a limit reduction set to 95% for a limit of 1,000MW, the security analysis will detect a limit violation for every current greater than 950MW.

Each limit reduction has its own criteria specifying for which limits and in which conditions it should be used. These criteria can be:
- the limit type (current, active power or apparent power)
- the use case: for monitoring only or for actions
- the contingency context (pre-contingency, after a specific contingency or after all contingencies, ...)
- concerning specific network elements (branches, three windings transformers, ...), which could be described by the following criteria:
    - a set of their ids
    - their countries
    - their nominal voltages
- their limit characteristics:
    - the limit type: permanent or temporary
    - their acceptable duration (for temporary limits)
        - equal to a specific value
        - inside an interval.

These criteria can be cumulative; several can be used at the same time to define a limit reduction.

Because a limit can validate the criteria of several limit reductions, the order the limit reductions are declared is important.
When the criteria of several limit reductions are met, the one that will be applied is the last encountered when reading them from start to end.

## Criteria details

### Limit type

The type of the limits on which to apply the reduction must be specified (mandatory item). The supported types are:
- `CURRENT`: for current limits;
- `ACTIVE_POWER`: for active power limits;
- `APPARENT_POWER`: for apparent power limits.


### Use cases (Monitoring or action)

The reduction may apply:

1. For monitoring only: when a limit is associated with an action and a reduction is applied for monitoring only,
if the encountered value is between the reduced limit value and the original limit value, the action is **not** applied.

2. For action: when a limit is associated with an action and a reduction is applied for action,
if the encountered value is between the reduced limit value and the original limit value, the action is be applied.


### Contingency context

Zero or one contingency context can be specified. It contains:
- a type among:
    - `ALL`: corresponding to all contingencies and pre-contingency situations;
    - `NONE`: corresponding to pre-contingency situations;
    - `SPECIFIC`: corresponding to a specific contingency situation;
    - `ONLY_CONTINGENCIES`: corresponding to all contingency situations (without the pre-contingency one).
- and when the type is `SPECIFIC`, the id of the contingency.

When no contingency context is present, the `ALL` policy is used.


### Network elements

The network elements whose limits will be affected by the limit reductions can be defined in using several criteria:
- a set of the network elements' ids;
- one or two countries (respectively for elements with one or two substations);
- their nominal voltages, by defining an interval for each of the voltage levels.

If no network elements is specified, the limit reduction applies to all of them.


### Limit duration characteristics

Zero or several duration criteria can be specified. Each one contains:
- a type among:
    - `PERMANENT`: corresponding to permanent limits only;
    - `TEMPORARY`: corresponding to temporary limits only;
- and when the type is `TEMPORARY`, one of the following options to restrict them accordingly to their acceptable duration:
    - `ALL`: to select all temporary limits, regardless their acceptable duration;
    - `EQUALITY`: to select the temporary limits whose acceptable duration is equal to a specified value, with:
        - `value`: the said value;
    - `INTERVAL`: to select the temporary limits whose acceptable duration is within an interval, with:
        - `lowBound` and `highBound`: minimum and maximum duration, each can be null;
        - `lowClosed` and `highClosed`: to indicate if the interval is open (`false`) or closed (`true`) on respectively the lower and the upper boundaries.
      This attribute is facultative if the corresponding bound value is `null`.

When no duration criteria are present, the reduction is applied to all permanent and temporary limits.

When several duration criteria are specified, the limit reductions applies to each one. 
For instance, if both criteria `PERMANENT` and (`TEMPORARY` ; `EQUALITY`: 600) are defined, the limit reduction will apply to permanent limits and 600s limits.
