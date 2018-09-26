# Module limit-violation-default-filter

The `limit-violation-default-filter` module is used in the [security-analysis](../../tools/security-analysis.md) and the
[action-simulator](../../tools/action-simulator.md) tools to filter the violations displayed.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| countries | List of String | no | All | A list of [ISO-3166](https://en.wikipedia.org/wiki/ISO_3166-1) country codes |
| minBaseVoltage | Double | no | 0 | A minimal nominal voltage |
| violationTypes | List of `ViolationType` | no | All | A list of `ViolationType` |

**country**: a violation is displayed only if at least one of its side has its substation's country in the list.

**minBaseVoltage**: a violation is displayed only if at least one of its side has its VoltageLevel's nominal voltage greater
than the minBaseVoltage value.

**violationTypes**: a violation is displayed if its type is in the list. The available ViolationType values are:
- CURRENT
- LOW_VOLTAGE
- HIGH_VOLTAGE
- LOW_SHORT_CIRCUIT_CURRENT
- HIGH_SHORT_CIRCUIT_CURRENT
- OTHER

## Examples

### YAML
```yaml
limit-violation-default-filter:
    countries: FR,BE
    minBaseVoltage: 225
    violationTypes: CURRENT,LOW_VOLTAGE,HIGH_VOLTAGE
```

### XML
```xml
<limit-violation-default-filter>
    <countries>FR,BE</countries>
    <minBaseVoltage>225</minBaseVoltage>
    <violationTypes>CURRENT,LOW_VOLTAGE,HIGH_VOLTAGE</violationTypes>
</limit-violation-default-filter>
```

## Reference
See also:
[action-simulator](../../tools/action-simulator.md),
[security-analysis](../../tools/security-analysis.md)
