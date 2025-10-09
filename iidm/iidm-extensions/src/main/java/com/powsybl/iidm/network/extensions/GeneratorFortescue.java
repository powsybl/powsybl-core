/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface GeneratorFortescue extends Extension<Generator> {

    String NAME = "generatorFortescue";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * True if the generator is earthed.
     */
    boolean isGrounded();

    void setGrounded(boolean grounded);

    /**
     * If the generator is earthed, it represents the resistance part of the impedance to ground.
     */
    double getGroundingR();

    void setGroundingR(double groundingR);

    /**
     * If the generator is earthed, it represents the reactance part of the impedance to ground.
     */
    double getGroundingX();

    void setGroundingX(double groundingX);

    /**
     * The zero sequence resistance of the generator.
     */
    double getRz();

    void setRz(double rz);

    /**
     * The zero sequence reactance of the generator.
     */
    double getXz();

    void setXz(double xz);

    /**
     * The negative sequence resistance of the generator.
     */
    double getRn();

    void setRn(double rn);

    /**
     * The negative sequence reactance of the generator.
     */
    double getXn();

    void setXn(double xn);
}
