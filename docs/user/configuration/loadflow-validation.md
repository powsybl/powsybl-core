# loadflow-validation
The `loadflow-validation` module is used by the [loadflow-validation](../itools/loadflow-validation.md) command. It defines the parameters used during the validation of load flow results.

## Optional properties

**apply-reactance-correction**  
The `apply-reactance-correction` property is an optional property that defines whether the too small reactance values have to be fixed to `epsilon-x` value or not. To solve numeric issues with very small reactance values, it's necessary to set the too small values to a minimal value. The default value of this property is `false`.

**check-main-component-only**  
The `check-main-component-only` property is an optional property that defines whether the validation checks are done only on the equipments in the main connected component or in all components. The default value of this property is `true`.

**compare-results**  
Set the `compare-results` property to true to compare the results of 2 validations, i.e. print output files with data of both ones. The default value of this property is `false`.

**epsilon-x**  
The `epsilon-x` property is an optional property that defines the value used to correct the reactance in flows validation. The default value of this property is `0.1`.

**load-flow-name**  
The `load-flow-name` property is an optional property that defines the implementation name to use for running the load flow. If this property is not set, the default load flow implementation is used. See [Loadflow Configuration](load-flow.md) to configure the default load flow.

**Note**: In previous PowSyBl releases (before 3.0.0), this was configured by the `load-flow-factory` property with the full classname of the implementation.

**no-requirement-if-reactive-bound-inversion**  
The `no-requirement-if-reactive-bound-inversion` property is an optional property that defines whether the validation checks fail if there is a reactive bounds inversion (maxQ < minQ) or not. The default value of this property is `false`.

**no-requirement-if-setpoint-outside-power-bounds**  
The `no-requirement-if-setpoint-outside-power-bounds` property is an optional property that defines whether the validation checks fail if there is a setpoint outside the active power bounds (targetP < minP or targetP > maxP) or not. The default value of this property is `false`.

**ok-missing-values**  
The `ok-missing-values` property is an optional property that defines whether the validation checks fail if some parameters of connected components have `NaN` values or not. The default value of this property is `false`.

**output-writer**  
The `output-writer` property is an optional property that defines the output format. Currently, `CSV` and `CSV_MULTILINE` are supported. The default value of this property is set to `CSV_MULTILINE`.

If this property is set to `CSV`, in the output files a line contains all values of validated equipment. If the property is set to `CSV_MULTILINE`, in the output files the equipment values are split in multiple lines, one value for each line, see examples below:

**Example of output in CSV format:**
```
id;p;q;v;nominalV;reactivePowerSetpoint;voltageSetpoint;connected;regulationMode;bMin;bMax;mainComponent;validation
CSPCH.TC1;-0,00000;93,6368;238,307;225,000;0,00000;238,307;true;VOLTAGE;-0,00197531;0,00493827;true;success
CSPDO.TC1;-0,00000;0,00000;240,679;225,000;0,00000;240,713;true;VOLTAGE;-0,00493827;0,00493827;true;success
...
```

**Example of output in CSV_MULTILINE format:**
```
id;characteristic;value
CSPCH.TC1;p;-0,00000
CSPCH.TC1;q;93,6368
CSPCH.TC1;v;238,307
...
```

**table-formatter-factory:**  
The `table-formatter-factory` property is an optional property that defines the `com.powsybl.commons.io.table.TableFormatterFactory` implementation to use for writing the output files. If this property is not set, the `com.powsybl.commons.io.table.CsvTableFormatterFactory` implementation is used.

The available implementation of the `TableFormatterFactory` are:
- `com.powsybl.commons.io.table.CsvTableFormatterFactory`: to create a CSV file
- `com.powsybl.commons.io.table.AsciiTableFormatterFactory`: to render table in ASCII

The table formatter can be configured by the [table-formatter](table-formatter.md) module.

**threshold:**  
The `threshold` property is an optional property that defines the margin used for values comparison. The default value of this property is `0`.

**verbose:**  
The `verbose` property is an optional property that defines whether the [loadflow-validation](../itools/loadflow-validation.md) command runs in verbose or quiet mode.

If this property is set to `true`, the output files contain all the data of the validated equipment; otherwise they contain only the main data of the validated equipment.

## Examples

**YAML configuration:**
```yaml
loadflow-validation:
    threshold: 0.1
    verbose: false
    load-flow-name: Mock
    table-formatter-factory: com.powsybl.commons.io.table.CsvTableFormatterFactory
    epsilon-x: 0.1
    apply-reactance-correction: false
    output-writer: CSV_MULTILINE
    ok-missing-values: false
    no-requirement-if-reactive-bound-inversion: false
    compare-results: false
    check-main-component-only: true
    no-requirement-if-setpoint-outside-power-bounds: false
```

**XML configuration:**
```xml
<loadflow-validation>
    <threshold>0.1</threshold>
    <verbose>false</verbose>
    <load-flow-name>Mock</load-flow-name>
    <table-formatter-factory>com.powsybl.commons.io.table.CsvTableFormatterFactory</table-formatter-factory>
    <epsilon-x>0.1</epsilon-x>
    <apply-reactance-correction>false</apply-reactance-correction>
    <output-writer>CSV_MULTILINE</output-writer>
    <ok-missing-values>false</ok-missing-values>
    <no-requirement-if-reactive-bound-inversion>false</no-requirement-if-reactive-bound-inversion>
    <compare-results>false</compare-results>
    <check-main-component-only>true</check-main-component-only>
    <no-requirement-if-setpoint-outside-power-bounds>false</no-requirement-if-setpoint-outside-power-bounds>
</loadflow-validation>
```
