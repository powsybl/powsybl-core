# ImportConfig

Source file:
[com.powsybl.iidm.import_.ImportConfig.java](https://github.com/powsybl/powsybl-core/blob/master/iidm/iidm-converter-api/src/main/java/com/powsybl/iidm/import_/ImportConfig.java)

| Property | Type | Default value | Required | Comment |
| -------- | ---- | ------------- | -------- | ------- |
| postProcessors | List | empty | false | The postProcessors are successiveley applied following the order of the list |

```xml
<import>
  <!--<postProcessors></postProcessors>-->
</import>
```

In the postProcessors option, the post processors must be separated with commas. For example : javaScript, groovyScript .

Please note : only one process of each type can be applied. To apply two processes defined in two different .js files for example, bring them togather in just one file.
