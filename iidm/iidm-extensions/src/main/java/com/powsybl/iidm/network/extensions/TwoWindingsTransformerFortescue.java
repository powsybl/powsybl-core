/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TwoWindingsTransformerFortescue extends Extension<TwoWindingsTransformer> {

    String NAME = "twoWindingsTransformerFortescue";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * The not-split zero-sequence resistance of the two-winding transformer.
     */
    double getRz();

    void setRz(double rz);

    /**
     * The not-split zero-sequence reactance of the two-winding transformer.
     */
    double getXz();

    void setXz(double xz);

    /**
     * The zero-sequence resistance of side 1 of the two-winding transformer.
     */
    double getRz1();

    void setRz1(double rz1);

    /**
     * The zero-sequence reactance of side 1 of the two-winding transformer.
     */
    double getXz1();

    void setXz1(double xz1);

    /**
     * The zero-sequence resistance of side 2 of the two-winding transformer.
     */
    double getRz2();

    void setRz2(double rz2);

    /**
     * The zero-sequence reactance of side 2 of the two-winding transformer.
     */
    double getXz2();

    void setXz2(double xz2);

    /**
     * Free fluxes set to true means that the magnetizing impedance Zm is infinite, i.e. fluxes are free.
     */
    boolean isFreeFluxes();

    void setFreeFluxes(boolean freeFluxes);

    /**
     * The zero-sequence magnetizing reactance of the two-winding transformer, if not considered as infinite.
     */
    double getXmz();

    void setXmz(double xmz);

    /**
     * Get the winding connection type of transformer side 1, see {@link WindingConnectionType}).
     */
    WindingConnectionType getConnectionType1();

    void setConnectionType1(WindingConnectionType connectionType1);

    /**
     * Get the winding connection type of transformer side 2, see {@link WindingConnectionType}).
     */
    WindingConnectionType getConnectionType2();

    void setConnectionType2(WindingConnectionType connectionType2);

    /**
     * If the transformer side 1 is earthed, depending on {@link WindingConnectionType} side 1, it represents the
     * resistance part of the impedance to ground.
     */
    double getGroundingR1();

    void setGroundingR1(double groundingR1);

    /**
     * If the transformer side 2 is earthed, depending on {@link WindingConnectionType} side 2, it represents the
     * resistance part of the impedance to ground.
     */
    double getGroundingR2();

    void setGroundingR2(double groundingR2);

    /**
     * If the transformer side 1 is earthed, depending on {@link WindingConnectionType} side 1, it represents the
     * reactance part of the impedance to ground.
     */
    double getGroundingX1();

    void setGroundingX1(double groundingX1);

    /**
     * If the transformer side 2 is earthed, depending on {@link WindingConnectionType} side 2, it represents the
     * reactance part of the impedance to ground.
     */
    double getGroundingX2();

    void setGroundingX2(double groundingX2);
}
