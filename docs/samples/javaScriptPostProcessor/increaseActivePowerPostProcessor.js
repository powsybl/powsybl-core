/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
var debug = true; 

function increaseLoadActivePower( load, percent) {
	if (load != null) {
		var p = load.getTerminal().getP();
		load.getTerminal().setP(p * percent);
		if (debug)
			print("Load id: "+load.getId() +" Increase load active power, from " + p + " to " +  load.getTerminal().getP());
	}
        
}

var percent = 1.01;

if (network == null) {
    throw new NullPointerException()
}

for each (load in network.getLoads()) {
    increaseLoadActivePower(load , percent);    
}