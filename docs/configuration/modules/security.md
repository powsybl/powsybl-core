# Module security

The `security` module is used by the AFS web-server to define the authentication token validity.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| token-validity | Long | no | 3600 | The validity of the connection token in minutes. |
| skip-token-validity-check | Boolean | no | true | If true, the check of the token validity is skipped. |
 
## Examples

### YAML
```yaml
security:
    skip-token-validity-check: true
    token-validity: 3600
```

### XML
```xml
<security>
    <skip-token-validity-check>true</skip-token-validity-check>
    <token-validity>3600</token-validity>
</security>
```

## Reference
See also:
[AFS](../../architecture/afs/README.md)
