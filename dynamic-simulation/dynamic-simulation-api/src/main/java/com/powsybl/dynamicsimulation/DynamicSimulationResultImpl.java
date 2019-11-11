/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationResultImpl implements DynamicSimulationResult {

    private final boolean status;
    private final String logs;

    public DynamicSimulationResultImpl(boolean status, String logs) {
        this.status = status;
        this.logs = logs;
    }

    @Override
    public boolean isOk() {
        return status;
    }

    @Override
    public String getLogs() {
        return logs;
    }

}
