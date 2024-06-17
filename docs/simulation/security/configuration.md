# Configuration

(security-generic-parameters)=
## Parameters

(violations-increase-thresholds)=
### Violations increase thresholds
The user can provide parameters to define which violations must be raised after a contingency, if the violation was already present in the pre-contingency state (`IncreasedViolationsParameters`).

**flow-proportional-threshold**  
After a contingency, only flow violations (either current, active power or apparent power violations) that have increased in proportion by more than a threshold value compared to the pre-contingency state are listed in the limit violations. The other ones are filtered. The threshold value is unitless and should be positive. This method gets the flow violation proportional threshold. The default value is 0.1, meaning that only violations that have increased by more than 10% appear in the limit violations.

**low-voltage-proportional-threshold**  
After a contingency, only low-voltage violations that have increased by more than the proportional threshold compared to the pre-contingency state, are listed in the limit violations, the other ones are filtered. This method gets the low voltage violation proportional threshold (unitless, should be positive). The default value is 0.0, meaning that only violations that have increased by more than 0.0 % appear in the limit violations (note that for low-voltage violation, it means that the voltage in the post-contingency state is lower than the voltage in the pre-contingency state).

**low-voltage-absolute-threshold**  
After a contingency, only low-voltage violations that have increased by more than an absolute threshold compared to the pre-contingency state, are listed in the limit violations, the other ones are filtered. This method gets the low voltage violation absolute threshold (in kV, should be positive). The default value is 0.0, meaning that only violations that have increased by more than 0.0 kV appear in the limit violations (note that for low-voltage violation, it means that the voltage in the post-contingency state is lower than the voltage in the pre-contingency state).

**high-voltage-proportional-threshold**  
Same as before but for high-voltage violations.

**high-voltage-absolute-threshold**  
Same as before but for high-voltage violations.

(violation-filtering)=
### Violations filtering
The violations listed in the results can be filtered to consider only certain type of violations, to consider only few voltage levels or to limit the geographical area by filtering equipments by countries. Check out the documentation of the [limit-violation-default-filter](../../user/configuration/limit-violation-default-filter.md) configuration module.

**Example**
Using the following configuration, the results will contain only voltage violations for equipments in France or Belgium:
```yaml
limit-violation-default-filter:
    countries:
        - FR
        - BE
    violationTypes:
        - LOW_VOLTAGE
        - HIGH_VOLTAGE
```

