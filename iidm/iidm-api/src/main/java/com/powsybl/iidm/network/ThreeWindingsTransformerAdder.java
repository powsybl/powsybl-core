/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ThreeWindingsTransformerAdder extends IdentifiableAdder<ThreeWindingsTransformerAdder> {

    public interface LegAdder<L extends LegAdder> {

        L setVoltageLevel(String voltageLevelId);

        L setNode(int node);

        L setBus(String bus);

        L setConnectableBus(String connectableBus);

        L setR(double r);

        L setX(double x);

        L setG1(double g);

        L setB1(double b);

        L setG2(double g);

        L setB2(double b);

        L setRatedU(double ratedU);

        ThreeWindingsTransformerAdder add();
    }

    LegAdder newLeg1();

    LegAdder newLeg2();

    LegAdder newLeg3();

    ThreeWindingsTransformer add();

}
