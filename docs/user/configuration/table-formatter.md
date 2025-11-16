---
layout: default
---

# table-formatter
The `table-formatter` module is used to configure the rendering of tables displayed in the console. It is also used to export data in CSV files.

## Optional properties

**invalidString**  
The `invalidString` property is an optional property that defines the replacement string to display when a value is absent. The default value of this property is `inv`.

**language**  
The `language` property is an optional property that defines the language code of the locale to use. The default value
of this property is the language code (2-characters code) of the system default locale.

**printHeader**  
The `printHeader` property is an optional property that defines whether the headers of the columns are displayed or not. The default value of this property is `true`.

**printTitle**  
The `printTitle` property is an optional property that defines whether the title of the table is displayed or not. The default value of this property is `true`.

**separator**  
The `separator` property is an optional property that defines the column separator used in CSV files. The default value of this property is `;`.

## Examples

**YAML configuration:**
```yaml
table-formatter:
    invalidString: inv
    language: FR
    printHeader: true
    printTitle: true
    separator: ;
```

**XML configuration:**
```xml
<table-formatter>
    <invalidString>inv</invalidString>
    <language>FR</language>
    <printHeader>true</printHeader>
    <printTitle>true</printTitle>
    <separator>;</separator>
</table-formatter>
```
