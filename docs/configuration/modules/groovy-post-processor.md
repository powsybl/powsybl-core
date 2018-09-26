# Module groovy-post-processor

The `groovy-post-processor` module is used by the `GroovyScriptPostProcessor` which is an implementation of the
`ImportPostProcessor` to run a groovy script after a case is converted to an IIDM network.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| script | Path | no | import-post-processor.groovy | The groovy script to apply to the imported IIDM network. |

**script**: The path of the groovy script must be an absolute. If this property is not set, the `import-post-processor.groovy`
file is read from the [powsybl configuration](../itools.md) folder.

## Examples

### YAML
```yaml
groovy-post-processor:
    script: /tmp/my-script.groovy
```

### XML
```xml
<groovy-post-processor>
    <script>/tmp/my-script.groovy</script>
</groovy-post-processor>
```

## References
See also:
[ImportPostProcessor](../../architecture/iidm/post-processor/README.md),
[GroovyScriptPostProcessor](../../architecture/iidm/post-processor/groovyScriptPostProcessor.md)
