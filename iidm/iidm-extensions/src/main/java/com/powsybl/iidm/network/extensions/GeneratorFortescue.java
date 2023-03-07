/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface GeneratorFortescue extends Extension<Generator> {

    String NAME = "generatorFortescue";

    @Override
    default String getName() {
        return NAME;
    }

    boolean isGrounded();

    void setGrounded(boolean grounded);

    double getGroundingR();

    void setGroundingR(double groundingR);

    double getGroundingX();

    void setGroundingX(double groundingX);

    double getR0();

    void setR0(double r0);

    double getX0();

    void setX0(double x0);

    double getR2();

    void setR2(double r2);

    double getX2();

    void setX2(double x2);
}
