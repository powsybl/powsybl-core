# Outputs

Each of the three operations returns its own result, and all three are extendable, so an
implementation can attach what this API does not model without the API changing.

## StateEstimationResult

A load flow writes its answer into the network and returns a status. An estimate has to stay
comparable against the measurements it came from, so `StateEstimationResult` carries the state
itself and writing back is opt-in through
[writeResultsToNetwork](parameters.md#writeresultstonetwork).

| Attribute          | Type                    | Unit | Required | Default value | Description                                                |
|--------------------|-------------------------|------|----------|---------------|------------------------------------------------------------|
| status             | Status                  | -    | yes      | -             | `FULLY_ESTIMATED`, `PARTIALLY_ESTIMATED` or `FAILED`       |
| componentResults   | List<ComponentResult>   | -    | yes      | -             | One entry per component the estimation covered             |
| busResults         | List<BusResult>         | -    | no       | Empty list    | The estimated state, one entry per estimated bus           |
| measurementResults | List<MeasurementResult> | -    | no       | Empty list    | One entry per measurement consumed, in no guaranteed order |

`ComponentResult`:

| Attribute              | Type   | Unit | Required | Default value | Description                                                 |
|------------------------|--------|------|----------|---------------|-------------------------------------------------------------|
| status                 | Status | -    | yes      | -             | `ESTIMATED`, `UNOBSERVABLE`, `DID_NOT_CONVERGE` or `FAILED` |
| statusText             | String | -    | no       | Empty string  | The status in the implementation's own words                |
| iterationCount         | int    | -    | yes      | -             | Iterations taken                                            |
| referenceBusId         | String | -    | yes      | -             | The bus whose voltage angle was held fixed                  |
| objectiveFunctionValue | double | -    | no       | `NaN`         | Weighted sum of squared residuals at the solution           |

A `statusText`, here and on the other two results, is whatever the implementation wants to add
about its status, and is empty when it has nothing.

Only angle differences are physical, so one angle per synchronous component is not an unknown and
`referenceBusId` names the bus that carries it. It also identifies the component, since a component
number would depend on traversal order and would not survive a topology change.

`objectiveFunctionValue` is the quantity a least-squares estimator minimizes, and it is what a
chi-square test is computed from. An estimator in another family does not produce one and returns
`NaN`.

`BusResult` is in the units IIDM uses for a bus:

| Attribute | Type   | Unit    | Required | Default value | Description                 |
|-----------|--------|---------|----------|---------------|-----------------------------|
| busId     | String | -       | yes      | -             | The bus                     |
| v         | double | kV      | yes      | -             | Estimated voltage magnitude |
| angle     | double | degrees | yes      | -             | Estimated voltage angle     |

`MeasurementResult` is in the unit the measurement was given in:

| Attribute          | Type    | Unit | Required | Default value | Description                                                                  |
|--------------------|---------|------|----------|---------------|------------------------------------------------------------------------------|
| measurementId      | String  | -    | yes      | -             | The measurement                                                              |
| measuredValue      | double  | -    | yes      | -             | What the meter read                                                          |
| estimatedValue     | double  | -    | yes      | -             | What a meter in this position would read in the estimated state              |
| residual           | double  | -    | yes      | -             | Measured minus estimated                                                     |
| normalizedResidual | double  | -    | yes      | -             | The residual divided by its own standard deviation, comparable across meters |
| flagged            | boolean | -    | yes      | -             | Whether its absolute value exceeded the threshold                            |

A normalized residual is signed: positive means the meter read above the estimate. Flagging
compares the absolute value against
[residualFlaggingThreshold](#param-se-residual-flagging-threshold), so a flagged measurement can
sit on either side.

## ObservabilityResult

Computed from measurement placement and topology, so it is available whether or not an estimation
converged. The vocabulary follows the IIDM
[observability](../../grid_model/extensions.md#observability-area) extensions, so a provider can
publish the same information onto the network without translating it first.

| Attribute              | Type                       | Unit | Required | Default value | Description                                  |
|------------------------|----------------------------|------|----------|---------------|----------------------------------------------|
| status                 | Status                     | -    | yes      | -             | `COMPLETED` or `FAILED`                      |
| statusText             | String                     | -    | no       | Empty string  | The status in the implementation's own words |
| areas                  | List<Area>                 | -    | no       | Empty list    | Connected sets of buses sharing one status   |
| elementObservabilities | List<ElementObservability> | -    | no       | Empty list    | Per-element observability                    |

A `FAILED` analysis reports nothing meaningful in the other two.

`getArea(busId)` and `getElementObservability(elementId)` look either list up by identifier and
return an empty `Optional` when there is no entry.

`Area` is an observable island. These depend on where the measurements are rather than on
connectivity, so one connected component of the network can hold several of them. The structure
mirrors `ObservabilityArea.AreaCharacteristics`:

| Attribute  | Type        | Unit | Required | Default value | Description                                    |
|------------|-------------|------|----------|---------------|------------------------------------------------|
| areaNumber | int         | -    | yes      | -             | The area, numbered as the extension numbers it |
| status     | AreaStatus  | -    | yes      | -             | `OBSERVABLE`, `NON_OBSERVABLE` or `BORDER`     |
| busIds     | Set<String> | -    | yes      | -             | The buses in the area                          |

`ElementObservability` matches `InjectionObservability` for an injection and `BranchObservability`
for a branch:

| Attribute  | Type          | Unit | Required | Default value | Description                        |
|------------|---------------|------|----------|---------------|------------------------------------|
| elementId  | String        | -    | yes      | -             | The element                        |
| observable | boolean       | -    | yes      | -             | One answer covering every quantity |
| qualities  | List<Quality> | -    | no       | Empty list    | What was determined, per quantity  |

`Quality` matches `ObservabilityQuality`:

| Attribute         | Type                 | Unit           | Required | Default value  | Description                                      |
|-------------------|----------------------|----------------|----------|----------------|--------------------------------------------------|
| quantity          | Quantity             | -              | yes      | -              | `ACTIVE_POWER`, `REACTIVE_POWER` or `VOLTAGE`    |
| side              | Optional<ThreeSides> | -              | no       | Empty optional | The branch side, empty for an injection          |
| standardDeviation | double               | MW, MVar or kV | yes      | -              | The standard deviation of the estimated quantity |
| redundant         | Optional<Boolean>    | -              | no       | Empty optional | Whether anything else determines this quantity   |

An empty `redundant` means the estimation did not establish it. `false` means the quantity is
critical: losing the measurement behind it would leave a region unobservable, and an error in it
cannot be detected.

## BadDataResult

| Attribute                 | Type                     | Unit | Required | Default value | Description                                                   |
|---------------------------|--------------------------|------|----------|---------------|---------------------------------------------------------------|
| status                    | Status                   | -    | yes      | -             | `NO_BAD_DATA`, `BAD_DATA_DETECTED` or `INCONCLUSIVE`          |
| statusText                | String                   | -    | no       | Empty string  | The status in the implementation's own words                  |
| suspectMeasurements       | List<SuspectMeasurement> | -    | no       | Empty list    | The measurements that disagree with the estimate, worst first |
| uncheckableMeasurementIds | Set<String>              | -    | no       | Empty set     | The measurements that could not be tested                     |

`SuspectMeasurement`:

| Attribute          | Type   | Unit | Required | Default value | Description                                        |
|--------------------|--------|------|----------|---------------|----------------------------------------------------|
| measurementId      | String | -    | yes      | -             | The measurement                                    |
| normalizedResidual | double | -    | yes      | -             | The residual divided by its own standard deviation |

The residual is signed here too, so the list holds readings that sit above and below the estimate.

A measurement that nothing else determines always has a residual of zero, whatever its value, so
it is listed under `uncheckableMeasurementIds` instead of being reported as correct. Those are the
quantities whose `Quality` reports `redundant` as false.

Nothing is removed or reweighted. See [limitations](limitations.md#bad-data-removal).

## Extensions

All three results extend `Extendable`, so an implementation attaches its own statistics as a typed
extension:

```java
StateEstimationResult result = StateEstimation.estimate(network);
MyEstimatorStatistics statistics = result.getExtension(MyEstimatorStatistics.class);
```
