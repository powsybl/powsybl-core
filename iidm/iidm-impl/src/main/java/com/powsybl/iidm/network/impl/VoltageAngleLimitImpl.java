/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import java.util.Optional;

import com.powsybl.iidm.network.*;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class VoltageAngleLimitImpl implements VoltageAngleLimit {

    private final String name;
    private final Terminal referenceTerminal;
    private final Terminal otherTerminal;
    private final double lowLimit;
    private final double highLimit;

    VoltageAngleLimitImpl(String name, Terminal referenceTerminal, Terminal otherTerminal, double lowLimit, double highLimit) {
        this.name = name;
        this.referenceTerminal = referenceTerminal;
        this.otherTerminal = otherTerminal;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Terminal getReferenceTerminal() {
        return referenceTerminal;
    }

    @Override
    public Terminal getOtherTerminal() {
        return otherTerminal;
    }

    @Override
    public Optional<Double> getLowLimit() {
        return Double.isNaN(lowLimit) ? Optional.empty() : Optional.of(lowLimit);
    }

    @Override
    public Optional<Double> getHighLimit() {
        return Double.isNaN(highLimit) ? Optional.empty() : Optional.of(highLimit);
    }
}
