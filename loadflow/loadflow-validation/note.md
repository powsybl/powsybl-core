## Rules / loadflow validation rules review (this file just for review purpose, do not merge )

### ShuntCompensator validation 

#### Doc
- core grid model: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/grid_model/network_subnetwork.html#shunt-compensator
- core tool loadflow-validation: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/user/itools/loadflow-validation.html#shunts
#### Notes (draft)
1. Rule1: **|p| < e**
   - if connected, p must be undefined or 0
2. Rule2: **| q + #sections * B * v^2 | < e**
   - if connected, q must match expectedQ (within threshold), ( **expectedQ = #sections * B * v^2** ==> **| q + expectedQ | < e** )
        - if LinearModel then #sections = bPerSection else #sections = B
        - **bPerSection**: the susceptance per section in S
        - **currentSectionCount** = B (The susceptance of the shunt compensator in its current state)
3. Rule3: if the shunt is disconnected, q should be undefined or 0
#### Summary and actions

|            |           Documentation           |                                                                                Code (ShuntCompensatorsValidation) |                                                                                   Description |                                                        Suggestions (TODO) |
|:-----------|:---------------------------------:|------------------------------------------------------------------------------------------------------------------:|----------------------------------------------------------------------------------------------:|--------------------------------------------------------------------------:|
| Condition1 |             \|P\| < ε             |                                                                                 if(!Double.isNaN(p)) return false |                                        if shunt compensator is connected, p must be undefined | - Add to condition: `or p != 0 return false`, to match the rule \|P\| < ε |
| Condition2 | \| q + #sections * B * v^2 \| < ε | if (ValidationUtils.areNaN(config, q, expectedQ) \| Math.abs(q - expectedQ) > config.getThreshold()) return false | if connected, q must match expectedQ (within threshold) <br/> expectedQ = #sections * B * v^2 |                                                                         - |
| Condition3 |                 -                 |                                                        if (!connected && !Double.isNaN(q) && q != 0) return false |                                      if the shunt is disconnected, q should be undefined or 0 |                                              - `add this rule in the doc` |

### Generator validation TODO

#### Doc
- core grid model: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/grid_model/network_subnetwork.html#generator
- core tool loadflow-validation:: https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/user/itools/loadflow-validation.html#generators
#### Notes (Draft) 
* Rule1: Active power (p) must match setpoint (expectedP) (within threshold)
* Rule2: if voltageRegulatorOn="false" then reactive power (Q) should match to setpoint (targetQ) (within threshold)
* Rule3: if voltageRegulatorOn="true"
* Rule3.1: (minQ/maxQ/targetV) are not NaN
* Rule3.2: If V > targetV + threshold, generator (Qgen) must be at min reactive limit
* Rule3.3: If V < targetV - threshold, generator (Qgen) must be at max reactive limit
* Rule3.4: If |V-targetV| <= threshold, generator (Qgen) must be within [minQ, maxQ]

##### Actions TODO
