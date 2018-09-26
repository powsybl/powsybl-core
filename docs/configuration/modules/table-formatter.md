# Module table-formatter

The `table-formatter` module is used to configure the format to display in the console simulation results as tables.
It is also used to export data in CSV files.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| invalidString | String | no | inv | The replacement string which is displayed when a value is absent. |
| language | String | no | - | The language code of the locale to use. |
| printHeader | Boolean | no | true | If true, print the column names. |
| printTitle | Boolean | no | true | If true, print the title of the table. |
| separator | Character | no | ; | The column separator used in CSV files. |

**language**: The default value for this property is the language code (2-characters code) of the system default locale.

## Examples

### YAML
```yaml
table-formatter:
    invalidString: inv
    language: FR
    printHeader: true
    printTitle: true
    separator: ;
```

### XML
```xml
<table-formatter>
    <invalidString>inv</invalidString>
    <language>FR</language>
    <printHeader>true</printHeader>
    <printTitle>true</printTitle>
    <separator>;</separator>
</table-formatter>
```
