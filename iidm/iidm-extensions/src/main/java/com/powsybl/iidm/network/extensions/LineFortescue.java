/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Line;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface LineFortescue extends Extension<Line> {

    String NAME = "lineFortescue";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * The zero-sequence resistance of the line.
     */
    double getRz();

    void setRz(double rz);

    /**
     * The zero-sequence reactance of the line.
     */
    double getXz();

    void setXz(double xz);

    boolean isOpenPhaseA();

    void setOpenPhaseA(boolean openPhaseA);

    boolean isOpenPhaseB();

    void setOpenPhaseB(boolean openPhaseB);

    boolean isOpenPhaseC();

    void setOpenPhaseC(boolean openPhaseC);

    double getB1z();

    void setB1z(double b1z);

    double getB2z();

    void setB2z(double b2z);

    double getG1z();

    void setG1z(double g1z);

    double getG2z();

    void setG2z(double g2z);
}
