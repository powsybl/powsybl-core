/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class VoltagePerReactivePowerControlAdderImpl
        extends AbstractExtensionAdder<StaticVarCompensator, VoltagePerReactivePowerControl>
        implements VoltagePerReactivePowerControlAdder {

    private double slope;

    protected VoltagePerReactivePowerControlAdderImpl(StaticVarCompensator svc) {
        super(svc);
    }

    @Override
    protected VoltagePerReactivePowerControlImpl createExtension(StaticVarCompensator svc) {
        return new VoltagePerReactivePowerControlImpl(svc, slope);
    }

    @Override
    public VoltagePerReactivePowerControlAdder withSlope(double slope) {
        this.slope = slope;
        return this;
    }
}
