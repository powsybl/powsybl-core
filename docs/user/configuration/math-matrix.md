# math-matrix
The `math-matrix` module is used to configure the matrix print formatting.

- `print-decimal-places` (optional integer, `>= 0`): maximum number of decimal places.
- If missing: default `Double.toString` formatting is used.
- If invalid (`< 0`): ignored, with a warning.

## Example

- `2` is printed as `2.0`
- with `print-decimal-places: 3`, `1.23456` is printed as `1.235` (default `DecimalFormat` rounding)

**YAML configuration:**
```yaml
math-matrix:
  print-decimal-places: 3
```


