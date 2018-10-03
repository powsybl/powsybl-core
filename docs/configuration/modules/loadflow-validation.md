# Module loadflow-validation

The `loadflow-validation` module is used by the [loadflow-validation](../../tools/loadflow-validation.md) tool. It defines the parameters used during the validation of the loadflow results.


## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
|threshold | double | no | 0 | margin used for values comparison|
|verbose | boolean | no | false | verbose output |
|load-flow-factory | String | no | `LoadFlowFactory` implementation set in `ComponentDefaultConfig` module| the `LoadFlowFactory` implementation to use for the loadFLowValidation |
|table-formatter-factory| String | no | `com.powsybl.commons.io.table.CsvTableFormatterFactory`| the `TableFormatterFactory` implementation to use for writing the output files |
|epsilon-x| double | no | 0.1 | value used to correct the reactance in flows validation, used only if `apply-reactance-correction` is true |
|apply-reactance-correction| boolean | no | false | apply reactance correction in flows validation |
|output-writer| String | no | `CSV_MULTILINE` | output format, possible values: [`CSV`, `CSV_MULTILINE`] |
|ok-missing-values| boolean | no | false | perform validation check even if some parameters of connected components have NaN values, i.e. if false, validation check fails if some parameters of connected components have NaN Values
|no-requirement-if-reactive-bound-inversion | boolean | no | false | return validation success if there is a reactive bounds inversion (maxQ < minQ) |
|compare-results| boolean | no | false | compare results of two validations, printing output files with results of both ones |
|check-main-component-only | boolean | no | true | validate only the equipment in the main connected component |
|no-requirement-if-setpoint-outside-power-bounds | boolean | no | false| return validation success if the set point is outside the active power bounds (targetP < minP or targetP > maxP) |

**verbose**: if this property is set to true, the output files contain all the data of the validated equipments, if false they contain only the main data of the validated equipments.  
**table-formatter-factory**: the properties of the table formatter are read from the [table-formatter](table-formatter.md) configuration.  
**output-writer**: if this property is set to `CSV`, in the output files a line contains all values of a validated equipment, if the property is set to `CSV_MULTILINE`, in the output files the values of an equipment are split in multiple lines, one value for each line, see examples below.  

### CSV
```csv
id;p;q;v;nominalV;reactivePowerSetpoint;voltageSetpoint;connected;regulationMode;bMin;bMax;mainComponent;validation
CSPCH.TC1;-0,00000;93,6368;238,307;225,000;0,00000;238,307;true;VOLTAGE;-0,00197531;0,00493827;true;success
CSPDO.TC1;-0,00000;0,00000;240,679;225,000;0,00000;240,713;true;VOLTAGE;-0,00493827;0,00493827;true;success
...
```

### CSV_MULTILINE
```csv
id;characteristic;value
CSPCH.TC1;p;-0,00000
CSPCH.TC1;q;93,6368
CSPCH.TC1;v;238,307
...
```

## Examples

### YAML
```yaml
loadflow-validation:
    threshold: 0.1
    verbose: false
    load-flow-factory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
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

### XML
```xml
<loadflow-validation>
    <threshold>0.1</threshold>
    <verbose>false</verbose>
    <load-flow-factory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</load-flow-factory>
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
## References
See also:
[table-formatter](table-formatter.md), [componentDefaultConfig](componentDefaultConfig.md)
