# math-matrix
The `math-matrix` module is used to configure the matrix print formatting.

## Optional properties

**print-decimal-places**<br>
The `print-decimal-places` property is an optional property that defines the maximum number of decimal places.

If this property is missing or set to negative value, the default `Double.toString` formatting is used.

## Example

- `2` is printed as `2.0`
- with `print-decimal-places: 3`, `1.23456` is printed as `1.235` (default `DecimalFormat` rounding)

**YAML configuration:**
```yaml
math-matrix:
  print-decimal-places: 3
```
**XML configuration:**
```xml
<config>
    <math-matrix>
        <print-decimal-places>3</print-decimal-places>
    </math-matrix>
</config>
```


