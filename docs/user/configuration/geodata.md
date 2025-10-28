# geo-json-importer-post-processor
The `geo-json-importer-post-processor` module is used to configure the paths of the files containing the geographical data.

The names of the properties are the two types of elements for which data are expected. Each value must be the complete path to the corresponding file.
- substations
- lines

## Example

**YAML configuration:**
```yaml
geo-json-importer-post-processor:
  substations: /path/to/substations.geojson
  lines: /path/to/lines.geojson
```

**XML configuration**
```xml
<geo-json-importer-post-processor>
    <substations>/path/to/substations.geojson</substations>
    <lines>/path/to/lines.geojson</lines>
</geo-json-importer-post-processor>
```
