# iTools loadflow-validation

The `loadflow-validation` command is used to validate load-flow results of a network. The command, besides validating
the results, also prints the data of the validated equipments in output files.
The consistency checks performed by the load flow validation may also be applied to results obtained with an optimal power flow or to the final state of a long dynamic simulation.

## Usage

```
$> itools loadflow-validation --help
usage: itools [OPTIONS] loadflow-validation --case-file <FILE>
       [--compare-case-file <FILE>] [--compare-results <COMPARISON_TYPE>]
       [--help] [-I <property=value>] [--import-parameters <IMPORT_PARAMETERS>]
       [--load-flow] --output-folder <FOLDER> [--output-format
       <VALIDATION_WRITER>] [--run-computation <COMPUTATION>] [--types
       <VALIDATION_TYPE,VALIDATION_TYPE,...>] [--verbose]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --case-file <FILE>                              case file path
    --compare-case-file <FILE>                      path to the case file to
                                                    compare
    --compare-results <COMPARISON_TYPE>             compare results of two
                                                    validations, printing output
                                                    files with results of both
                                                    ones. Available comparisons
                                                    are [COMPUTATION (compare
                                                    the validation of a basecase
                                                    before and after the
                                                    computation), BASECASE
                                                    (compare the validation of
                                                    two basecases)]
    --help                                          display the help and quit
 -I <property=value>                                use value for given importer
                                                    parameter
    --import-parameters <IMPORT_PARAMETERS>         the importer configuation
                                                    file
    --load-flow                                     run loadflow
    --output-folder <FOLDER>                        output folder path
    --output-format <VALIDATION_WRITER>             output format [CSV,
                                                    CSV_MULTILINE]
    --run-computation <COMPUTATION>                 run a computation on the
                                                    network before validation,
                                                    available computations are
                                                    [loadflow,
                                                    loadflowResultsCompletion]
    --types <VALIDATION_TYPE,VALIDATION_TYPE,...>   validation types [FLOWS,
                                                    GENERATORS, BUSES, SVCS,
                                                    SHUNTS, TWTS, TWTS3W] to
                                                    run, all of them if the
                                                    option if not specified
    --verbose                                       verbose output
```

### Required arguments

`--case-file`  
Use the `--case-file` parameter to define the path of the case file.

`--output-folder`  
Use the `--output-folder` parameter to define the path of the folder where the output files will be stored.


### Optional arguments

`--compare-case-file`   
Use the `--compare-case-file` parameter to define the path of the second case file, in order to compare the load flow
results of two case files.

`--compare-results`  
Use the `--compare-results` parameter to define the type of results to compare. The available types are:
- `BASECASE`: compare results of the two base cases
- `COMPUTATION`: run a computation on the two base cases and compare results of the resulting states.

`--import-parameters`  
Use the `--import-parameters` parameter to specify the path of the configuration file of the importer. It is possible to
overload one or many parameters using the `-I property=value` parameter. The properties depend on the input format.
Refer to the documentation page of each [importer](../../grid_exchange_formats/index.md) to know their specific configuration.

`--load-flow`  
Use the `--load-flow` parameter to run a load-flow before the validation. This option is equivalent to
`--run-computation loadflow`.

`--output-format`  
Use the `--output-format` parameter to specify the format of the output files. The available output formats are `CSV` or `CSV_MULTILINE`.

If this parameter is set to `CSV`, in the output files a line contains all values of validated equipment. If the parameter
is set to `CSV_MULTILINE`, in the output files the values of a piece of equipment are split in multiple lines, one value for each
line, see examples below:

**CSV**  
```
id;p;q;v;nominalV;reactivePowerSetpoint;voltageSetpoint;connected;regulationMode;bMin;bMax;mainComponent;validation
CSPCH.TC1;-0,00000;93,6368;238,307;225,000;0,00000;238,307;true;VOLTAGE;-0,00197531;0,00493827;true;success
CSPDO.TC1;-0,00000;0,00000;240,679;225,000;0,00000;240,713;true;VOLTAGE;-0,00493827;0,00493827;true;success
...
```

**CSV_MULTILINE**  
```
id;characteristic;value
CSPCH.TC1;p;-0,00000
CSPCH.TC1;q;93,6368
CSPCH.TC1;v;238,307
...
```

`--run-computation**  
Use the `--run-computation` parameter to run a computation before the validation. The supported computations are:
- `loadflow`: run a load-flow
- `loadflowResultsCompletion`: compute the missing `P`, `Q`, `V` and $\theta$ values

`--types**  
Use the `--types` parameter to define the types of checks to run. If this parameter is not set, run all the checks. 
The supported types are `FLOWS`, `GENERATORS`, `BUSES`, `SVCS`, `SHUNTS`, `TWTS`.

To learn more about the different checks, read the [loadflow validation](../../grid_features/loadflow_validation.md) documentation page.

### Summary
The following table summarizes the possible combinations of `compare-results` and `run-computation` parameters, and the
corresponding case states validated and written in the output files. Some remarks:
- State 1 is the state analyzed in the first validation
- State 2 is the state analyzed in the second validation (columns with the suffix `_postComp` in the output files)
- Case 1 is the value of `case-file` parameter
- Case 2 is the value of `compare-case-file` parameter
- some combinations are not available, e.g., if you use the `compare-results` parameter, with the `COMPUTATION` value,
you have to use the `run-computation` (or `load-flow`) parameter.

| Number | compare-results | run-computation                        | State 1                             | State 2 (_postComp)                 |
|--------|-----------------|----------------------------------------|-------------------------------------|-------------------------------------|
| 1      | absent          | absent                                 | Case 1 after import                 | None                                |
| 2      | absent          | `loadflow`/`loadflowResultsCompletion` | Case 1 after import and computation | None                                |
| 3      | `BASECASE`      | absent                                 | Case 1 after import                 | Case 2 after import                 |
| 4      | `BASECASE`      | `loadflow`/`loadflowResultsCompletion` | Case 1 after import and computation | Case 2 after import                 |
| 5      | `COMPUTATION`   | `loadflow`/`loadflowResultsCompletion` | Case 1 after import                 | Case 1 after import and computation |

## Parameters

To learn how to configure the `loadflow-validation` command, read the documentation of the
[loadflow validation](../configuration/loadflow-validation.md) module.

You may also configure the load flow itself to tune the load flow validation using the `--run-computation` option (check the [loadflow configuration page](../configuration/load-flow.md)).

## Load flow results validation

Overall, in the PowSyBl validation, the tests are not made overly tight. In particular, leniency is preferred to tightness in case approximations are needed or
when expectations are unclear (typically when the input data is inconsistent). For example, there is a switch to test
only the main component because it is not clear what to expect from load flow results on small connected components.

Another important global setting available in the PowSyBl validation is the `ok-missing-values` parameter, which determines if is OK to have missing
values or `NaN`. Normally, it should be set to false, but it may be useful in the cases where the power flow results are
incomplete to go through the rest of the validation.

In this section, we go into more details about the checks performed by the validation feature of load-flow results available in PowSyBl.

### Buses
If all values are present, or if only one value is missing, the result is considered to be consistent.
Note that if the result contains only the voltages (phase and angle), the PowSyBl validation provides a load-flow results completion feature.
It can be used to compute the flows from the voltages to ensure the result consistency, with the run-computation option of
the PowSyBl validation.

### Branches
The result on the branch is considered consistent if:

$$\max( \left| P_1^{calc} - P_1 \right|, \left| Q_1^{calc} - Q_1 \right|, \left| P_2^{calc} - P_2 \right|, \left| Q_2^{calc} - Q_2 \right| ) \leq \epsilon$$


For a branch that is disconnected on one end (for example, end 2), then $P_2 = Q_2 = 0$. As a result, it is
possible to recompute $(V_2, \theta_2)$ which are usually not returned by power flows and which are not stored in node-breaker
[network](../../grid_model/index.md) format. The quality checks are done when this is done.

In case of missing results (usually the powers $P_1$, $Q_1$, $P_2$, $Q_2$ which are not mandatory), the PowSyBl validation
will consider the results as inconsistent, unless `ok-missing-values` was set to `true` by the user on purpose to make the consistency
check more leniently. 

In case the voltages are available but not the powers, the result completion feature of the PowSyBl validation
can be used to recompute them using the validation equations (meaning that the branch validation tests will always be OK, so that it allows performing the bus validation tests).

### Three-windings transformers
<span style="color: red">To be implemented, based on a conversion into 3 two-windings transformers.</span>

### Generators

#### Active power

The load-flow validation of PowSyBl checks whether the adjustment of balances has been done consistently by the power flow.
The load-flow results do not include the adjustment mode used, nor the participation factors. They thus have to be inferred. 
If deviations are perfect, the proportion factor $\hat{K}$ estimated for the right mode will
be the same for all the deviating units for which $P$ is strictly $P_{min}$ and $P_{max}$. Therefore, the inferred
deviation is the one for which the standard deviation of the estimated proportion factor is the lowest.

Once the mode is determined, the new target can be computed for each unit. The following check is done:

$$\left| \max(P_{min}, \min(P_{max}, (1+\hat{K} F(g)))) targetP - P \right| < \epsilon$$

#### Voltage and reactive power

When the voltage regulation is disabled, the results' validity follows the condition below:

$$\left| targetQ - Q \right| < \epsilon$$

On the other hand, when the voltage regulation is enabled, depending on the generator's mode, one of the three conditions should be respected:

$$
\begin{align*}
    |V - targetV| & \leq && \epsilon && \& && minQ & \leq & Q \leq maxQ \\
    V - targetV & < & -& \epsilon && \& && |Q-maxQ| & \leq & \epsilon \\
    targetV - V & < && \epsilon && \& && |Q-minQ| & \leq & \epsilon \\
\end{align*}
$$

In the PowSyBl validation, there are a few tricks to handle special cases:
- if $minQ > maxQ$, then the values are switched to recover a meaningful interval if `noRequirementIfReactiveBoundInversion = false`
- in case of a missing value, the corresponding test is OK
- $minQ$ and $maxQ$ are function of $P$. If $targetP$ is outside $[minP, maxP]$, no test is done.

### Loads
<span style="color: red">To be implemented, with tests similar to generators with voltage regulation.</span>

### Shunts
The two following conditions must be fulfilled in valid results:

$$
\begin{align*}
\left| P \right| < \epsilon \\
\left| Q + \text{#sections} * B  V^2 \right| < \epsilon
\end{align*}
$$

### Static VAR Compensators
The following conditions must be fulfilled in valid results:
$targetP = 0$ MW
- If the regulation mode is `OFF`, then
 $$\left| targetQ - Q \right| < \epsilon$$
- If the regulation mode is `REACTIVE_POWER`, same checks as a generator without voltage regulation
- If the regulation mode is `VOLTAGE`, same checks as a generator with voltage regulation with the following bounds:
$$minQ = - Bmax * V^2$$ and $$maxQ = - Bmin V^2$$

### HVDC lines
<span style="color: red">To be done.</span>

#### VSC
Same checks as a generator. Besides, for stations paired by a cable:
$$\sum_{\text{stations}}{P} = \sum_{\text{stations}}{Loss} + Loss_{cable}$$

#### LCC
<span style="color: red">To be done.</span>

### Transformers with a ratio tap changer

To check a steady-state has been reached, an upper bound of the deadband value is needed. Generally, the value of the
deadband is not available in data models. Usual load flow solvers simply consider a continuous tap that is rounded
afterward. As a result, one should compute an upper bound of the effect of the rounding. Under the usual situation where
the low voltage (side one) is controlled, the maximum effect is expected if the high voltage is fixed (usually it decreases), 
and if the network connected to the low voltage is an antenna. If the transformer is perfect, the equations are:

- With the current tap `tap`, and if the regulated side is side `TWO`:

$$V_2(tap) = \rho_{tap} V_1$$

- With the next tap, the new voltage would be:

$$V_2(tap+1) = \rho_{tap+1} V_1 = \frac{\rho_{tap+1}}{\rho_{tap}} V_2(tap)$$

We can therefore compute approximately the voltage increments corresponding to $tap-1$ and $tap+1$.

- We then assume the *deadband* of the regulation to be equal to the voltage increase/decrease that can be performed with
taps $tap-1$ and $tap+1$:

$$
\begin{align*}
    & \text{up deadband} = - \min(V_2(tap+1) - V_2(tap), V_2(tap-1) - V_2(tap)) \\
    & \text{down deadband} = \max(V_2(tap+1) - V_2(tap), V_2(tap-1) - V_2(tap)) \\
\end{align*}
$$

Finally, we check that the voltage deviation $$\text{deviation} = V_2(tap) - targetV2$$ stays inside the deadband.
- If $deviation < 0$, meaning that the voltage is too low, it should be checked if the deviation is smaller by
increasing V2, i.e., the following condition should be satisfied: $$\left| deviation \right| < down deadband + threshold$$
- If $$deviation > 0$$, meaning that the voltage is too high, it should be checked if the deviation is smaller by
decreasing V2, i.e., the following condition should be satisfied: $$deviation < up deadband  + threshold$$

The test is done only if the regulated voltage is on one end of the transformer, and it always returns OK if the controlled voltage is remote.

## Examples

### Example 1
The following example shows how to run a load flow validation on a UCTE network model: 
```
$> itools loadflow-validation --case-file 20170322_1844_SN3_FR2.uct --output-folder /tmp/results
```

The validation results, printed to the standard output:
```
Loading case 20170322_1844_SN3_FR2.uct
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: TWTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: FLOWS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: BUSES - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SVCS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SHUNTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: GENERATORS - result: fail
```

Eventually, you will find in your output-folder one csv file for each validation type.

### Example 2
In this example, we are comparing results of two validations: before and after load flow computation. Two additional
arguments are needed:
- `load-flow`
- `compare_results`: COMPUTATION

```
$> itools loadflow-validation --case-file 20170322_1844_SN3_FR2.uct --output-folder tmp/loadFlowValidationResults
--verbose --output-format CSV --load-flow --compare-results COMPUTATION
```

The validation results, printed to the standard output:
```
Loading case 20170322_1844_SN3_FR2.uct
Running pre-loadflow validation on network 20170322_1844_SN3_FR2.uct.uct
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: TWTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: GENERATORS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: FLOWS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SHUNTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: BUSES - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SVCS - result: success
Running loadflow on network 20170322_1844_SN3_FR2.uct
Running post-loadflow validation on network 20170322_1844_SN3_FR2.uct
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: TWTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: GENERATORS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: FLOWS - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SHUNTS - result: success
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: BUSES - result: fail
Validate load-flow results of network 20170322_1844_SN3_FR2.uct - validation type: SVCS - result: success
```

Eventually, you will find in your output-folder one csv file for each validation type, containing the data pre- and post-computation (load flow).

