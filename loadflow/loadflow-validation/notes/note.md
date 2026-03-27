## Rules / loadflow validation rules review (this file just for review purpose, do not merge )

- [x] ShuntCompensator
- [x] Static VAR Compensator (SVC)
- [x] Generator
- [x] Buses
- [ ] Flows (Branch Data :Line, TwoWindingsTransformer, TieLine)
- [ ] Transformers (TWT)
- [ ] Transformers3W (TWT 3W)

### ShuntCompensator validation

##### Doc
- core grid model: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_model/network_subnetwork.html#shunt-compensator
- core grid features: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_features/loadflow_validation.html#shunts
- core tool loadflow-validation: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/user/itools/loadflow-validation.html#shunts
#### Notes
##### Gaps between current code and current doc:
- Disconnected rule (Rule 3) is missing in docs
    - Code/comment includes: if disconnected, Q must be NaN or 0 
    - doc only lists 2 rules; no disconnected terminal rule.
##### Current doc:
> [!NOTE]
> Rule1: **|p| < e**
> 
> if connected, p must be undefined or 0

> [!NOTE]
> Rule2: **| q + #sections * B * v^2 | < e** 
> 
> if connected, q must match expectedQ (within threshold), ( **expectedQ = - #sections * B * v^2** ==> **| q + expectedQ | < e** )
> - if LinearModel then #sections = bPerSection else #sections = B
> - **bPerSection**: the susceptance per section in S
> - **currentSectionCount** = B (The susceptance of the shunt compensator in its current state)

> [!WARNING]
> Rule3: if the shunt is disconnected, q should be undefined or 0

#### Summary and actions

|            |           Documentation           |                                                                                Code (ShuntCompensatorsValidation) |                                                                                   Description |                                                        Suggestions (TODO) |
|:-----------|:---------------------------------:|------------------------------------------------------------------------------------------------------------------:|----------------------------------------------------------------------------------------------:|--------------------------------------------------------------------------:|
| Condition1 |             \|P\| < ε             |                                                                                 if(!Double.isNaN(p)) return false |                                        if shunt compensator is connected, p must be undefined | - Add to condition: `or p != 0 return false`, to match the rule \|P\| < ε |
| Condition2 | \| q + #sections * B * v^2 \| < ε | if (ValidationUtils.areNaN(config, q, expectedQ) \| Math.abs(q - expectedQ) > config.getThreshold()) return false | if connected, q must match expectedQ (within threshold) <br/> expectedQ = #sections * B * v^2 |                                                                         - |
| Condition3 |                 -                 |                                                        if (!connected && !Double.isNaN(q) && q != 0) return false |                                      if the shunt is disconnected, q should be undefined or 0 |                                              - `add this rule in the doc` |

- Added util methods inValidationUtils
    - `isUndefinedOrZero` 
    - `isOutsideTolerance`
    - `isConnectedAndMainComponent`
    - `computeShuntExpectedQ`

### Static VAR compensator validation

##### Doc
- core grid model: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_model/network_subnetwork.html#static-var-compensator
- core grid features: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_features/loadflow_validation.html#static-var-compensators
- core tool loadflow-validation: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/user/itools/loadflow-validation.html#static-var-compensators

#### Notes
##### Gaps between current code and current doc:
- `OFF` mode in docs does not exist in `SVC validation API`
    - In IIDM, StaticVarCompensator.RegulationMode has only:
        - VOLTAGE 
        - REACTIVE_POWER
        - => The code equivalent of “OFF” is actually regulating = false.
- Rule for missing P/Q exists in code but not in docs
    - Current code behavior:
        - if P or Q is NaN, if it fails when reactivePowerSetpoint is defined and non zero.
---
##### Current code rules
> [!NOTE]
> Rule1: active power (p) (within threshold) should be equal to 0

> [!WARNING]
> Rule2: **reactivePowerSetpoint** must be undefined or equal to 0 if NO (**p** or **q**) 

> [!WARNING]
> Rule3: **regulationMode = REACTIVE_POWER** then same condition as generator without voltage regulation  
>   - Rule3.1: => (config, reactivePowerSetpoint, qMin, qMax) not defined => OK
>   - Rule3.2: => q must match reactivePowerSetpoint (within threshold)

> [!WARNING]
> Rule4: **regulationMode = VOLTAGE** then same condition as generator with voltage regulation 
>   - Rule4.1: => (config, qMin, qMax, vControlled, voltageSetpoint) not defined => OK
>   - Rule4.2: => V is lower than voltageSetpoint (within threshold) AND q must match qMax (within threshold)
>   - Rule4.3: => V is higher than voltageSetpoint (within threshold) AND q must match Qmin (within threshold)
>   - Rule4.4: => V is at the controlled bus (within threshold) AND q is bounded within [Qmin=-bMax*V*V, Qmax=-bMin*V*V]

> [!WARNING]
> Rule5: if regulating is false then reactive power (q) should be equal to 0 
>   - If the regulation mode is OFF: remove this rule from the doc source: #2790

##### Actions

|            |                                       Documentation                                        |               Code (StaticVarCompensator) |                                                                      Description |                                                                                           Suggestions (TODO) |
|:-----------|:------------------------------------------------------------------------------------------:|------------------------------------------:|---------------------------------------------------------------------------------:|-------------------------------------------------------------------------------------------------------------:|
| Condition1 |                                      `targetP = 0 MW`                                      | if (Math.abs(p) > Threshold) return false |                                                active power should be equal to 0 |                                                                                                            ? | 
| Condition2 |                                             -                                              |                      `checkSVCsNaNValues` | **reactivePowerSetpoint** must be undefined or equal to 0 if NO (**p** or **q**) | - `add this rule in the doc`, - check only if (q undefined or equal to 0 then **reactivePowerSetpoint** ~ 0) |
| Condition3 |                  `same checks as a generator without voltage regulation`                   |           `reactivePowerRegulationModeKo` |                                                                 Rule3.1, Rule3.2 |                                                                                                            - | 
| Condition4 | `same checks as a generator with voltage regulation with the following bounds: Qmin, Qmax` |                 `voltageRegulationModeKo` |                                               Rule4.1, Rule4.2, Rule4.3, Rule4.4 |                                                                                                            - |
| Condition5 |                                             -                                              |                         `notRegulatingKo` |                  if regulating is false then reactive power should be equal to 0 |                                                                                 - `add this rule in the doc` |

- Used util methods in ValidationUtils
    - `isUndefinedOrZero`
    - `isOutsideTolerance`
### Generator validation

##### Doc
- core grid model: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_model/network_subnetwork.html#generator
- core grid features: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_features/loadflow_validation.html#generators
- core tool loadflow-validation: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/user/itools/loadflow-validation.html#generators
##### Notes (draft) 

> [!NOTE]
> Rule1 : If `P` or `Q` is missing, validation fails if setpoints are defined and non-zero

> [!NOTE]
> Rule2 : If reactive limits are inverted (`maxQ < minQ`) and noRequirementIfReactiveBoundInversion = true, generator validation are bypassed.

> [!NOTE]
> Rule3 : active setpoint outside bounds bypass
>  If `targetP` is outside `[minP, maxP]` (with tolerance) and noRequirementIfSetpointOutsidePowerBounds = true, generator validation are bypassed

> [!NOTE]
> Rule4: Active power p matches expected setpoint
> Active power p must match setpoint (expectedP) (within threshold)

> [!NOTE]
> Rule5: If voltage regulator is disabled, Q matches targetQ
> Reactive power q should match to setpoint (targetQ) (within threshold) when voltageRegulatorOn = false

> [!NOTE]
> Rule6: If voltage regulator ON, Reactive power q follow V/targetV logic
>   - qGen at minQ if V > targetV + threshold
>   - qGen at maxQ if V < targetV - threshold
>   - else qGen within [minQ, maxQ])

##### Actions TODO

|            |            Documentation             |                                                                                   Code (GeneratorValidation) |                                                                    Description | Suggestions (TODO)                                      |
|:-----------|:------------------------------------:|-------------------------------------------------------------------------------------------------------------:|-------------------------------------------------------------------------------:|---------------------------------------------------------|
| Condition1 |                  -                   |                             `p or q should be defined if voltage and a target (targetP and targetQ) defined` |                                                                              - | - `voltage not mentioned in the condition in code TODO` |
| Condition2 |                  -                   |                          `when maxQ < minQ if noRequirementIfReactiveBoundInversion (parameter) return true` |                                                                              - | - `Add to the doc`                                      |
| Condition3 |                  -                   | `when targetP < minP or targetP > maxP if noRequirementIfSetpointOutsidePowerBounds (parameter) return true` |                                                                              - | - `Add to the doc`                                      |
| Condition4 | (added with recompute of expected P) |                                                                                                              |          - Active power (p) must match setpoint (expectedP) (within threshold) |                                                         |
| Condition5 |         \|targetQ - Q\| < ε          |                                                                                 `voltageRegulatorOn="false"` | - The reactive power (Q) should match to setpoint (targetQ) (within threshold) |                                                         |
| Condition6 |                  ..                  |                                                                                 `voltageRegulatorOn="true" ` |                                           - Rule6.1, Rule6.2, Rule6.3, Rule6.4 |                                                         |

- Used util methods in ValidationUtils
    - `isActivePowerKo`
    - `isReactivePowerKo`
    - `isVoltageRegulationKo`

### Buses validation

##### Doc
- core grid model:
- core grid features: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_features/loadflow_validation.html#buses
- core tool loadflow-validation: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/user/itools/loadflow-validation.html#buses
##### Notes
> [!NOTE]
> Rule: load p = incoming p And load q = incoming q
- Added method
    - `isBalanceInconsistent`

### Flows validation (Branch Data :Line, TwoWindingsTransformer, TieLine)
##### Doc
- core grid model:
- core grid features: https://powsybl--3849.org.readthedocs.build/projects/powsybl-core/en/3849/grid_features/loadflow_validation.html#branches
##### Notes (draft)
  - Flows (BranchData) can be constructed from
      - Line Flows => Line specific rules to clarify (TODO)
      - TwoWindingsTransformer Flows => TWT specific rules to clarify (TODO)
      - TieLine Flows => TieLine specific rules to clarify (TODO)
  - Rule 1: checks disconnected terminal 
  - Rule 2: checks connected terminal
##### Actions
- Refactor: `isUndefinedOrZero`
- Documentation: add Flows section

### Transformers (TWT) validation TODO

##### Doc
##### Notes
##### Actions

### Transformers3W (TWT 3W) TODO

##### Doc
##### Notes
##### Actions


