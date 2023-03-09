/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TwoWindingsTransformerFortescue extends Extension<TwoWindingsTransformer> {

    String NAME = "twoWindingsTransformerFortescue";

    @Override
    default String getName() {
        return NAME;
    }

    double getRz();

    void setRz(double rz);

    double getXz();

    void setXz(double xz);

    boolean isFreeFluxes();

    void setFreeFluxes(boolean freeFluxes);

    WindingConnectionType getConnectionType1();

    void setConnectionType1(WindingConnectionType connectionType1);

    WindingConnectionType getConnectionType2();

    void setConnectionType2(WindingConnectionType connectionType2);

    double getGroundingR1();

    void setGroundingR1(double groundingR1);

    double getGroundingR2();

    void setGroundingR2(double groundingR2);

    double getGroundingX1();

    void setGroundingX1(double groundingX1);

    double getGroundingX2();

    void setGroundingX2(double groundingX2);
}
