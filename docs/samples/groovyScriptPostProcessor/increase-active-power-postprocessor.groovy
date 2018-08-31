/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
double  percent = 1.01
network.getLoads().each { load ->
    load.setP0(load.getP0() * percent)
    double p = load.getTerminal().getP()
    load.getTerminal().setP(p * percent)
	println(load.getTerminal().getP())
}