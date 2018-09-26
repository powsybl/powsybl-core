# Module javaScriptPostProcessor

The `javaScriptPostProcessor` module is used by the `JavaScriptPostProcessor` which is an implementation of the
`ImportPostProcessor` to run a javascript script after a case is converted to an IIDM network.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| printToStdOut | Boolean | no | true | If true, prints the output of the script to the standard output stream. |
| script | Path | no | import-post-processor.js | The javascript script to apply to the imported IIDM network. |

**script**: The path of the javascript script must be an absolute. If this property is not set, the `import-post-processor.js`
file is read from the [powsybl configuration](../itools.md) folder.

## Examples

### YAML
```yaml
javaScriptPostProcessor:
    printToStdOut: true
    script: /tmp/my-script.js
```

### XML
```xml
<javaScriptPostProcessor>
    <printToStdOut>true</printToStdOut>
    <script>/tmp/my-script.js</script>
</javaScriptPostProcessor>
```

## References
See also:
[ImportPostProcessor](../../architecture/iidm/post-processor/README.md),
[JavaScriptPostProcessor](../../architecture/iidm/post-processor/javaScriptPostProcessor.md)
