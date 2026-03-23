## Rules / loadflow validation rules review (this file just for review purpose, do not merge )

- [x] ShuntCompensator
- [x] Static VAR Compensator (SVC)
- [x] Generator
- [ ] Buses
- [ ] Flows (Branch Data :Line, TwoWindingsTransformer, TieLine)
- [ ] Transformers (TWT)
- [ ] Transformers3W (TWT 3W)

### ShuntCompensator validation

#### Doc
- core grid model: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/grid_model/network_subnetwork.html#shunt-compensator
- core tool loadflow-validation: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/user/itools/loadflow-validation.html#shunts
#### Notes
- [ ] Rule1: **|p| < e**
   - if connected, p must be undefined or 0
- [x] Rule2: **| q + #sections * B * v^2 | < e**
   - if connected, q must match expectedQ (within threshold), ( **expectedQ = - #sections * B * v^2** ==> **| q + expectedQ | < e** )
        - if LinearModel then #sections = bPerSection else #sections = B
        - **bPerSection**: the susceptance per section in S
        - **currentSectionCount** = B (The susceptance of the shunt compensator in its current state)
- [ ] Rule3: if the shunt is disconnected, q should be undefined or 0
#### Summary and actions

|            |           Documentation           |                                                                                Code (ShuntCompensatorsValidation) |                                                                                   Description |                                                        Suggestions (TODO) |
|:-----------|:---------------------------------:|------------------------------------------------------------------------------------------------------------------:|----------------------------------------------------------------------------------------------:|--------------------------------------------------------------------------:|
| Condition1 |             \|P\| < ε             |                                                                                 if(!Double.isNaN(p)) return false |                                        if shunt compensator is connected, p must be undefined | - Add to condition: `or p != 0 return false`, to match the rule \|P\| < ε |
| Condition2 | \| q + #sections * B * v^2 \| < ε | if (ValidationUtils.areNaN(config, q, expectedQ) \| Math.abs(q - expectedQ) > config.getThreshold()) return false | if connected, q must match expectedQ (within threshold) <br/> expectedQ = #sections * B * v^2 |                                                                         - |
| Condition3 |                 -                 |                                                        if (!connected && !Double.isNaN(q) && q != 0) return false |                                      if the shunt is disconnected, q should be undefined or 0 |                                              - `add this rule in the doc` |


### Static VAR compensator validation

#### Doc
- core grid model: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/grid_model/network_subnetwork.html#static-var-compensator
- core tool loadflow-validation: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/user/itools/loadflow-validation.html#static-var-compensators

#### Notes
- Regulation : VOLTAGE, REACTIVE_POWER
- [ ] Rule1: active power (p) (within threshold) should be equal to 0
- [ ] Rule2: **reactivePowerSetpoint** must be undefined or equal to 0 if NO (**p** or **q**) 
    - TODO (doc states that p should be equal to 0 !, if so **reactivePowerSetpoint** must be undefined or equal to 0 !)
    - Suggestion => check only if (q undefined or equal to 0 then **reactivePowerSetpoint** ~ 0)
- [x] Rule3: **regulationMode = REACTIVE_POWER** then same condition as generator without voltage regulation
    - Rule3.1: => (config, reactivePowerSetpoint, qMin, qMax) not defined => OK
    - Rule3.2: => q must match reactivePowerSetpoint (within threshold)
- [x] Rule4: **regulationMode = VOLTAGE** then same condition as generator with voltage regulation
    - Rule4.1: => (config, qMin, qMax, vControlled, voltageSetpoint) not defined => OK
    - Rule4.2: => V is lower than voltageSetpoint (within threshold) AND q must match qMax (within threshold)
    - Rule4.3: => V is higher than voltageSetpoint (within threshold) AND q must match Qmin (within threshold)
    - Rule4.4: => V is at the controlled bus (within threshold) AND q is bounded within [Qmin=-bMax*V*V, Qmax=-bMin*V*V]
- [ ] Rule5: if regulating is false then reactive power (q) should be equal to 0
##### Actions

|            |                                       Documentation                                        |               Code (StaticVarCompensator) |                                                                      Description |                                                                                           Suggestions (TODO) |
|:-----------|:------------------------------------------------------------------------------------------:|------------------------------------------:|---------------------------------------------------------------------------------:|-------------------------------------------------------------------------------------------------------------:|
| Condition1 |                                      `targetP = 0 MW`                                      | if (Math.abs(p) > Threshold) return false |                                                active power should be equal to 0 |                                                                                                            ? | 
| Condition2 |                                             -                                              |                      `checkSVCsNaNValues` | **reactivePowerSetpoint** must be undefined or equal to 0 if NO (**p** or **q**) | - `add this rule in the doc`, - check only if (q undefined or equal to 0 then **reactivePowerSetpoint** ~ 0) |
| Condition3 |                  `same checks as a generator without voltage regulation`                   |           `reactivePowerRegulationModeKo` |                                                                 Rule3.1, Rule3.2 |                                                                                                            - | 
| Condition4 | `same checks as a generator with voltage regulation with the following bounds: Qmin, Qmax` |                 `voltageRegulationModeKo` |                                               Rule4.1, Rule4.2, Rule4.3, Rule4.4 |                                                                                                            - |
| Condition5 |                                             -                                              |                         `notRegulatingKo` |                  if regulating is false then reactive power should be equal to 0 |                                                                                 - `add this rule in the doc` |
| Condition6 |                 `If the regulation mode is OFF, then \|targetQ -Q \| <  ε`                 |                                -        ` |                  if regulating is false then reactive power should be equal to 0 |                                                              - `remove this rule from the doc` source: #2790 |


### Generator validation

#### Doc
- core grid model: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/grid_model/network_subnetwork.html#generator
- core tool loadflow-validation: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/user/itools/loadflow-validation.html#generators
#### Notes (draft) 
- [ ] Rule1: when maxQ < minQ if noRequirementIfReactiveBoundInversion (parameter) return true (TODO)
- [ ] Rule2: when targetP < minP or targetP > maxP if noRequirementIfSetpointOutsidePowerBounds (parameter) return true (TODO)
- [ ] Rule3: p or q should be defined if voltage and a target (targetP and targetQ) defined => voltage not mentioned in the condition in code (TODO)
- [x] Rule4: Active power (p) must match setpoint (expectedP) (within threshold)
- [x] Rule5: if voltageRegulatorOn="false" then reactive power (Q) should match to setpoint (targetQ) (within threshold)
- [x] Rule6: if voltageRegulatorOn="true"
    * Rule6.1: (minQ/maxQ/targetV) are not defined => OK
    * Rule6.2: If V > targetV + threshold, generator (Qgen) must be at min reactive limit
    * Rule6.3: If V < targetV - threshold, generator (Qgen) must be at max reactive limit
    * Rule6.4: If |V-targetV| <= threshold, generator (Qgen) must be within [minQ, maxQ]

##### Actions TODO


|            |            Documentation             |                                                                                   Code (GeneratorValidation) |                                                                    Description | Suggestions (TODO)                                      |
|:-----------|:------------------------------------:|-------------------------------------------------------------------------------------------------------------:|-------------------------------------------------------------------------------:|---------------------------------------------------------|
| Condition1 |                  -                   |                             `p or q should be defined if voltage and a target (targetP and targetQ) defined` |                                                                              - | - `voltage not mentioned in the condition in code TODO` |
| Condition2 |                  -                   |                          `when maxQ < minQ if noRequirementIfReactiveBoundInversion (parameter) return true` |                                                                              - | - `Add to the doc`                                      |
| Condition3 |                  -                   | `when targetP < minP or targetP > maxP if noRequirementIfSetpointOutsidePowerBounds (parameter) return true` |                                                                              - | - `Add to the doc`                                      |
| Condition4 | (added with recompute of expected P) |                                                                                                              |          - Active power (p) must match setpoint (expectedP) (within threshold) |                                                         |
| Condition5 |         \|targetQ - Q\| < ε          |                                                                                 `voltageRegulatorOn="false"` | - The reactive power (Q) should match to setpoint (targetQ) (within threshold) |                                                         |
| Condition6 |                  ..                  |                                                                                 `voltageRegulatorOn="true" ` |                                           - Rule6.1, Rule6.2, Rule6.3, Rule6.4 |                                                         |

