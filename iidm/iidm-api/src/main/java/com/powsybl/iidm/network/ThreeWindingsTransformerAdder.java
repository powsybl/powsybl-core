/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ThreeWindingsTransformerAdder extends IdentifiableAdder<ThreeWindingsTransformer, ThreeWindingsTransformerAdder> {

    public interface LegAdder {

        LegAdder setVoltageLevel(String voltageLevelId);

        LegAdder setNode(int node);

        LegAdder setBus(String bus);

        LegAdder setConnectableBus(String connectableBus);

        LegAdder setR(double r);

        LegAdder setX(double x);

        LegAdder setG(double g);

        LegAdder setB(double b);

        LegAdder setRatedU(double ratedU);

        default LegAdder setRatedS(double ratedS) {
            throw new UnsupportedOperationException();
        }

        ThreeWindingsTransformerAdder add();
    }

    LegAdder newLeg1();

    LegAdder newLeg2();

    LegAdder newLeg3();

    ThreeWindingsTransformerAdder setRatedU0(double ratedU0);

    @Override
    ThreeWindingsTransformer add();
}
