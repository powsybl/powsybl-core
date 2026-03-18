## Rules summary / loadflow validation rules review (this file just for review purpose, do not merge )

- [x] ShuntCompensator
- [x] Static VAR Compensator (SVC)
- [x] Generator
- [x] Buses
- [ ] Flows (Branch Data :Line, TwoWindingsTransformer, TieLine)
- [ ] Transformers (TWT)
- [ ] Transformers3W (TWT 3W)

### ShuntCompensator validation
#### Rules for valid results
* Rule 1: |p| < e
* Rule 2: q must match expectedQ
* Rule 3: if the shunt is disconnected, q should be undefined or 0

### Static VAR compensator validation
#### Rules for valid results
* Rule 1: active power should be equal to 0 
* Rule 2: reactivePowerSetpoint must be 0 if p or q is missing 
* Rule 3: regulationMode = REACTIVE_POWER, q must match reactivePowerSetpoint 
* Rule 4: regulationMode = VOLTAGE 
*    - V is lower than voltageSetpoint (within threshold) AND q must match qMax (within threshold) 
*    - V is higher than voltageSetpoint (within threshold) AND q must match Qmin (within threshold) 
*    - V is at the controlled bus (within threshold) AND q is bounded within [Qmin=-bMax*V*V, Qmax=-bMin*V*V]
* Rule 5: if regulating is false then reactive power (q) should be equal to 0

### Generator validation
#### Rules for valid results
* Rule 1: A validation error should be detected if there is both a voltage and a target but no p or q
* Rule 2: If reactive limits are inverted (`maxQ < minQ`) and `noRequirementIfReactiveBoundInversion = true`, generator validation OK.
* Rule 3: Active setpoint outside bounds, if `targetP` is outside `[minP, maxP]` and `noRequirementIfSetpointOutsidePowerBounds = true`, generator validation OK
* Rule 4: Active power p matches expected setpoint
* Rule 5: If voltage regulator is disabled, reactive power Q matches targetQ
* Rule 6: If voltage regulator is enabled, reactive power q follow V/targetV logic
*   - qGen ~ minQ if V > targetV + threshold
*   - qGen ~ maxQ if V < targetV - threshold
*   - else qGen within [minQ, maxQ])

### Buses validation
#### Rules for valid results
* Rule 1 : |incomingP + loadP| <= threshold and |incomingQ + loadQ| <= threshold

### Flows validation (Branch Data :Line, TwoWindingsTransformer, TieLine)
#### Rules for valid results

### Transformers (TWT) validation 

### Transformers3W (TWT 3W) TODO
