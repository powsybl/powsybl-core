/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.VoltageLevel;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BreakerClosing implements ModificationTask {

    private final String voltageLevelId;

    private final String breakerId;

    public BreakerClosing(String voltageLevelId, String breakerId) {
        this.voltageLevelId = voltageLevelId;
        this.breakerId = breakerId;
    }

    @Override
    public void modify(Network network) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        if (vl == null) {
            throw new ITeslaException("Voltage level '" + voltageLevelId + "' not found");
        }
        Switch s = vl.getBusBreakerView().getSwitch(breakerId);
        if (s == null) {
            throw new ITeslaException("Breaker '" + breakerId + "' not found");
        }
        s.setOpen(false);
    }

}
