# limit-violation-default-filter
The `limit-violation-default-filter` module is used by the [security-analysis](../itools/security-analysis.md) and the
[action-simulator](../itools/action-simulator.md) commands to filter the violations displayed.

## Optional properties

**countries**  
The `countries` property is an optional property that defines a list of [ISO-3166](https://en.wikipedia.org/wiki/ISO_3166-1) country codes used for violations filtering. A violation is displayed only if at least one of its sides has its substation's country in the list. If this property is not set, there is no filtering based on the countries.

**minBaseVoltage**  
The `minBaseVoltage` property is an optional property that defines a threshold value for the nominal voltage of the voltage levels. The default value of this property is `0`.

**violationTypes**  
The `violationTypes` property is an optional property that defines a list of `com.powsybl.security.LimitViolationType` used for violations filtering. a violation is displayed if its type is in the list. The available `LimitViolationType` values are:
- CURRENT
- LOW_VOLTAGE
- HIGH_VOLTAGE
- LOW_SHORT_CIRCUIT_CURRENT
- HIGH_SHORT_CIRCUIT_CURRENT
- OTHER

## Examples

**YAML configuration:**
```yaml
limit-violation-default-filter:
    countries:
        - FR
        - BE
    minBaseVoltage: 225
    violationTypes:
        - CURRENT
        - LOW_VOLTAGE
        - HIGH_VOLTAGE
```

**XML configuration:**
```xml
<limit-violation-default-filter>
    <countries>FR,BE</countries>
    <minBaseVoltage>225</minBaseVoltage>
    <violationTypes>CURRENT,LOW_VOLTAGE,HIGH_VOLTAGE</violationTypes>
</limit-violation-default-filter>
```

