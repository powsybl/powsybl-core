---
layout: default
---
# Compare security analysis results

The `compare-security-analysis-results` command is used to compare [security-analysis](../../simulation/securityanalysis/index.md#outputs) results, stored in JSON.

The outcome of the comparison is `success` if the results are equivalent, `fail` otherwise.  
  
This tool compares for the pre-contingency state and for all the post-contingency states of the two results:
- the outcome (convergence/divergence) of the load flow computations
- the values of the constraints violations
  
Two security analysis results are considered equivalent if all the following conditions are satisfied:
- for all the pre-contingency and post-contingency states, the corresponding (i.e. related to the same state) outcome of the load flow computation is the same
- for all the constraints violations, the difference of value of a corresponding (i.e. related to the same contingency
and equipment) violation is less than a predefined threshold
- if a constraints violation is contained in just one result, the violation is less than a predefined threshold
- if a contingency is contained in just one result, all the post-contingency violations are less than a predefined threshold
  
The comparison process can optionally output in a CSV file all the compared values (pre and post-contingency load flow computation outcomes, and related constraints violations), with a corresponding comparison result (`equivalent`,`different`). See example below.

```csv
Contingency;StatusResult1;StatusResult2;Equipment;End;ViolationType;ViolationNameResult1;ValueResult1;LimitResult1;ViolationNameResult2;ValueResult2;LimitResult2;ActionsResult1;ActionsResult2;Comparison
;converge;converge;;;;;;;;;;;;equivalent
;;;NHV1_NHV2_1;ONE;CURRENT;;1100,00;950,000;;1100,09;950,000;;;equivalent
contingency1;converge;converge;;;;;;;;;;;;equivalent
contingency1;;;NHV1_NHV2_1;ONE;CURRENT;;1100,00;950,000;;1100,09;950,000;;;equivalent
contingency1;;;NHV1_NHV2_1;TWO;CURRENT;;1100,00;950,000;;1101,00;950,000;;;different
contingency2;converge;converge;;;;;;;;;;;;equivalent
contingency2;;;NHV1_NHV2_1;ONE;CURRENT;;1100,00;950,000;;1100,09;950,000;;;equivalent
contingency2;;;NHV1_NHV2_2;ONE;CURRENT;;1100,00;950,000;;1100,09;950,000;;;equivalent
```

## Usage
```
$> itools compare-security-analysis-results --help
usage: itools [OPTIONS] compare-security-analysis-results [--help] --output-file
       <FILE> --result1-file <FILE> --result2-file <FILE> [--threshold
       <THRESHOLD>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --help                    display the help and quit
    --output-file <FILE>      output file path, where the comparison results
                              will be stored
    --result1-file <FILE>     security analysis result 1 file path
    --result2-file <FILE>     security analysis result 2 file path
    --threshold <THRESHOLD>   threshold used for results comparison, default is 0.0
```

### Required parameters

**output-file**  
The `--output-file` parameter defines the path of the output file, where the comparison results will be stored.

**result1-file**  
The `--result1-file` parameter defines the path of the JSON file containing the first security analysis result. 

**result2-file**  
The `--result2-file` parameter defines the path of the JSON file containing the second security analysis result.

### Optional parameters

### threshold
Use the `--threshold` parameter to specify the threshold used for comparing values of the two security analysis results. Default value is `0`.

## Examples

This example shows how to compare two security analysis results and to store the comparison results in the `$HOME/comparison_results.csv` file:

```
$> itools compare-security-analysis-results --result1-file $HOME/result1.json --result2-file $HOME/result2.json --output-file $HOME/comparison_results.csv
Comparison result: fail
```

This example shows how to specify the threshold to be used in the comparison:

```
$> itools compare-security-analysis-results --result1-file $HOME/result1.json --result2-file $HOME/result2.json --output-file $HOME/comparison_results.csv  --threshold 0.1
Comparison result: success
```

# Going further
To go further, check the following content:
-[security-analysis](security-analysis.md): an iTools command to run [security analyses](../../simulation/securityanalysis/index.md)

