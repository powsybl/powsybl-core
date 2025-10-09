/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class VoltagePerReactivePowerControlImpl extends AbstractExtension<StaticVarCompensator> implements VoltagePerReactivePowerControl {

    private double slope;

    public VoltagePerReactivePowerControlImpl(StaticVarCompensator svc, double slope) {
        super(svc);
        this.slope = checkSlope(slope);
    }

    @Override
    public double getSlope() {
        return slope;
    }

    public VoltagePerReactivePowerControl setSlope(double slope) {
        this.slope = checkSlope(slope);
        return this;
    }

    private double checkSlope(double slope) {
        if (Double.isNaN(slope)) {
            throw new PowsyblException("Undefined value for slope");
        }
        if (slope < 0) {
            throw new PowsyblException("Slope value of SVC " + getExtendable().getId() + " must be positive: " + slope);
        }
        return slope;
    }
}
