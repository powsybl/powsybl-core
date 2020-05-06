/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CoordinatedReactiveControlImpl extends AbstractExtension<Generator> implements CoordinatedReactiveControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatedReactiveControlImpl.class);
    private double qPercent;

    public CoordinatedReactiveControlImpl(Generator generator, double qPercent) {
        super(generator);
        this.qPercent = checkQPercent(generator, qPercent);
    }

    @Override
    public double getQPercent() {
        return qPercent;
    }

    @Override
    public void setQPercent(double qPercent) {
        this.qPercent = checkQPercent(getExtendable(), qPercent);
    }

    private static double checkQPercent(Generator generator, double qPercent) {
        if (Double.isNaN(qPercent)) {
            throw new PowsyblException("Undefined value for qPercent");
        }
        if (qPercent < 0 || qPercent > 100) {
            LOGGER.debug("qPercent value of generator {} does not seem to be a valid percent: {}", generator.getId(), qPercent);
        }
        return qPercent;
    }
}
