# Configuration

The `security-analysis` module is used to configure the execution of the [security-analysis](../../user/itools/security-analysis.md) command and simulation.

## Implementation

**preprocessor**<br>
The `preprocessor` property is an optional property which requires that the `SecurityAnalysisPreprocessor` with specified
name is used to preprocess inputs, based on the contingency file, before actually running the security analysis.

Such a preprocessor will have the possibility to programmatically transform the following objects before the security
analysis is actually executed :
- The `Network`
- The `ContingenciesProvider`
- The `LimitViolationDetector`
- The `LimitViolationFilter`
- The `SecurityAnalysisParameters`
- The `SecurityAnalysisInterceptor`s

It enables, for example, to customize what should be considered a limit violation and what should not.

If absent, the default behavior of the tool is used: the contingency file is simply interpreted by the configured contingency provider.

**YAML configuration:**
```yaml
security-analysis:
    preprocessor: my_custom_preprocessor_name
```

**XML configuration:**
```xml
<security-analysis>
    <preprocessor>my_custom_preprocessor_name</preprocessor>
</security-analysis>
```


(security-generic-parameters)=
## Parameters

(violations-increase-thresholds)=
### Violations increase thresholds
The user can provide parameters to define which violations must be raised after a contingency, if the violation was already
present in the pre-contingency state (`IncreasedViolationsParameters`).

(param-secu-flow-proportional-threshold)=
#### flow-proportional-threshold
After a contingency, only flow violations (either current, active power or apparent power violations) that have increased
in proportion by more than a threshold value, compared to the pre-contingency state, are listed in the limit violations.
The other ones are filtered. The threshold value is unitless and should be positive.
This method gets the flow violation proportional threshold.

The default value is 0.1, meaning that only violations that have increased by more than 10% appear in the limit violations.

(param-secu-low-voltage-proportional-threshold)=
#### low-voltage-proportional-threshold
After a contingency, only low-voltage violations that have increased by more than the proportional threshold compared to
the pre-contingency state, are listed in the limit violations, the other ones are filtered. This method gets the low-voltage
violation proportional threshold (unitless, should be positive). The default value is 0.0, meaning that only violations
that have increased by more than 0.0 % appear in the limit violations (note that for low-voltage violation, it means that
the voltage in the post-contingency state is lower than the voltage in the pre-contingency state).

(param-secu-low-voltage-absolute-threshold)=
#### low-voltage-absolute-threshold
After a contingency, only low-voltage violations that have increased by more than an absolute threshold compared to the
pre-contingency state, are listed in the limit violations, the other ones are filtered. This method gets the low-voltage
violation absolute threshold (in kV, should be positive). The default value is 0.0, meaning that only violations that
have increased by more than 0.0 kV appear in the limit violations (note that for low-voltage violation, it means that
the voltage in the post-contingency state is lower than the voltage in the pre-contingency state).

(param-secu-high-voltage-proportional-threshold)=
#### high-voltage-proportional-threshold
Same as before but for high-voltage violations.

(param-secu-high-voltage-absolute-threshold)=
#### high-voltage-absolute-threshold
Same as before but for high-voltage violations.

(monitoring-modification-thresholds)=
### Monitoring modification thresholds
When monitoring elements before and after contingencies, thresholds can be defined to limit the number
of element states stored in the post-contingency results.

#### Thresholds on power
**power-modification-threshold**<br>
After a contingency, all the monitored branches and three-windings transformers whose active and reactive power has not
changed by more than the threshold (in MW and MVAr) compared to pre-contingency state are filtered from the post-contingency results.

It is set to 0 MW/MVAr by default, meaning no filtering is performed.

#### Thresholds on voltage
The following two parameters are not mutually exclusive. When both are defined, the effective threshold in kV is the minimum
of the absolute threshold and the value obtained by applying the proportional threshold to the pre-contingency voltage.

**voltage-modification-proportional-threshold**<br>
After a contingency, monitored buses whose voltage magnitude has not changed by more than the specified threshold relative
to the pre-contingency state are filtered out of the post-contingency results.
For example, when set to 0.01, voltage magnitude changes lower than 1% of pre-contingency bus voltage magnitude value are not reported.

It is set to 0 by default, meaning no filtering is performed.

**voltage-modification-absolute-threshold**<br>
After a contingency, monitored buses whose voltage magnitude has not changed by more than the specified absolute threshold (in kV)
compared to pre-contingency state are filtered from the post-contingency results.

It is set to 0 kV by default, meaning no filtering is performed.

(violation-filtering)=
### Violations filtering
The violations listed in the results can be filtered to consider only a certain type of violations, to consider only a
few voltage levels or to limit the geographical area by filtering equipment by countries. Check out the documentation
of the [limit-violation-default-filter](../../user/configuration/limit-violation-default-filter.md) configuration module.

**Example**<br>
Using the following configuration, the results will contain only voltage violations for equipment in France or Belgium:
```yaml
limit-violation-default-filter:
    countries:
        - FR
        - BE
    violationTypes:
        - LOW_VOLTAGE
        - HIGH_VOLTAGE
```

(param-secu-debug-dir)=
### debug-dir
This property specifies the directory path where debug files will be dumped. If `null`, no file will be dumped.

The default value is `null`.

## Examples

**YAML configuration:**
```yaml
security-analysis-default-parameters:
  intermediate-results-in-operator-strategy: true
  increased-flow-violations-proportional-threshold: 0.2
  increased-low-voltage-violations-proportional-threshold: 0.1
  increased-high-voltage-violations-proportional-threshold: 0.1
  increased-low-voltage-violations-absolute-threshold: 0.1
  increased-high-voltage-violations-absolute-threshold: 0.1
  power-modification-threshold: 0.0
  voltage-modification-proportional-threshold: 0.0
  voltage-modification-absolute-threshold: 0.0
  debug-dir: /tmp/debugDir
```

**XML configuration:**
```xml
<security-analysis-default-parameters>
  <intermediate-results-in-operator-strategy>true</intermediate-results-in-operator-strategy>
  <increased-flow-violations-proportional-threshold>0.2</increased-flow-violations-proportional-threshold>
  <increased-low-voltage-violations-proportional-threshold>0.1</increased-low-voltage-violations-proportional-threshold>
  <increased-high-voltage-violations-proportional-threshold>0.1</increased-high-voltage-violations-proportional-threshold>
  <increased-low-voltage-violations-absolute-threshold>0.1</increased-low-voltage-violations-absolute-threshold>
  <increased-high-voltage-violations-absolute-threshold>0.1</increased-high-voltage-violations-absolute-threshold>
  <power-modification-threshold>0.0</power-modification-threshold>
  <voltage-modification-proportional-threshold>0.0</voltage-modification-proportional-threshold>
  <voltage-modification-absolute-threshold>0.0</voltage-modification-absolute-threshold>
  <debug-dir>/tmp/debugDir</debug-dir>
</security-analysis-default-parameters>
```
