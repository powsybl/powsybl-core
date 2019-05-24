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
public interface TwoWindingsTransformerAdder extends BranchAdder<TwoWindingsTransformerAdder> {

    TwoWindingsTransformerAdder setR(double r);

    TwoWindingsTransformerAdder setX(double x);

    TwoWindingsTransformerAdder setB1(double b);

    TwoWindingsTransformerAdder setG1(double g);

    TwoWindingsTransformerAdder setB2(double b);

    TwoWindingsTransformerAdder setG2(double g);

    TwoWindingsTransformerAdder setRatedU1(double ratedU1);

    TwoWindingsTransformerAdder setRatedU2(double ratedU2);

    TwoWindingsTransformerAdder setPhaseAngleClock1(int phaseAngleClock1);

    TwoWindingsTransformerAdder setPhaseAngleClock2(int phaseAngleClock2);

    TwoWindingsTransformer add();

}
