### Rules / Config Threshold
#### Generator
* Rule1: Active power (p) must match setpoint (expectedP) (within threshold)
* Rule2: if voltageRegulatorOn="false" then reactive power (Q) should match to setpoint (targetQ) (within threshold)
* Rule3: if voltageRegulatorOn="true"
* Rule3.1: (minQ/maxQ/targetV) are not NaN
* Rule3.2: If V > targetV + threshold, generator (Qgen) must be at min reactive limit
* Rule3.3: If V < targetV - threshold, generator (Qgen) must be at max reactive limit
* Rule3.4: If |V-targetV| <= threshold, generator (Qgen) must be within [minQ, maxQ]

[//]: # (TODO)