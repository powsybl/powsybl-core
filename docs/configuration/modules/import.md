# Module import

The `import` module is used by the `Importers` class, after a case is loaded. A post-processor is a class used to modify
a network after its loading.
- [GroovyPostProcessor](../../architecture/iidm/post-processor/groovyScriptPostProcessor.md): to run a groovy script 
- [JavaScriptPostProcessor](../../architecture/iidm/post-processor/javaScriptPostProcessor.md) to run a javascript script
- [LoadFlowResultsCompletion](../../architecture/iidm/post-processor/loadflowResultsCompletion.md) to complete missing P, Q, V and angle values

Other post-processors might be available in the platform: the [plugins-info](../../tools/plugins-info.md) itool command can be used to list the usable implementations. 

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| postProcessors | List of String | no | - | The list of `ImportPostProcessor` names |

## Examples

### YAML
```yaml
import:
    postProcessors: groovyScript,javaScript
```

### XML
```xml
<import>
    <postProcessors>groovyScript,javaScript</postProcessors>
</import>
```

## References
See also:
[convert-network](../../tools/convert-network.md),
[PostProcessor](../../architecture/iidm/post-processor/README.md)
