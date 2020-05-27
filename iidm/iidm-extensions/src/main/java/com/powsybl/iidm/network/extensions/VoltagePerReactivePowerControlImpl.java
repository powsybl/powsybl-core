/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.StaticVarCompensator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public class VoltagePerReactivePowerControlImpl extends AbstractExtension<StaticVarCompensator> implements VoltagePerReactivePowerControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltagePerReactivePowerControlImpl.class);

    private double slope;

    public VoltagePerReactivePowerControlImpl(StaticVarCompensator svc, double slope) {
        super(svc);
        this.slope = checkSlope(svc, slope);
    }

    @Override
    public double getSlope() {
        return slope;
    }

    public VoltagePerReactivePowerControl setSlope(double slope) {
        this.slope = slope;
        return this;
    }

    private static double checkSlope(StaticVarCompensator svc, double slope) {
        if (Double.isNaN(slope)) {
            throw new PowsyblException("Undefined value for slope");
        }
        if (slope < 0) {
            LOGGER.debug("Slope value of svc {} should be positive: {}", svc.getId(), slope);
        }
        return slope;
    }
}
