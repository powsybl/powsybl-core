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

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CoordinatedReactiveControl extends AbstractExtension<Generator> {

    private double qPercent;

    public CoordinatedReactiveControl(Generator generator, double qPercent) {
        super(generator);
        this.qPercent = checkQPercent(qPercent);
    }

    @Override
    public String getName() {
        return "coordinatedReactiveControl";
    }

    public double getQPercent() {
        return qPercent;
    }

    public void setQPercent(double qPercent) {
        this.qPercent = checkQPercent(qPercent);
    }

    private static double checkQPercent(double qPercent) {
        if (Double.isNaN(qPercent)) {
            throw new PowsyblException("Undefined value for qPercent");
        }
        if (qPercent < 0.0 || qPercent > 100.0) {
            throw new PowsyblException("Unexpected value for qPercent: " + qPercent);
        }
        return qPercent;
    }
}
