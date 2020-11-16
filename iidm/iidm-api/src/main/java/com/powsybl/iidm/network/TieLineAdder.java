/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TieLineAdder extends BranchAdder<TieLineAdder> {

    interface HalfLineAdder {

        HalfLineAdder setId(String id);

        HalfLineAdder setName(String name);

        HalfLineAdder setFictitious(boolean fictitious);

        HalfLineAdder setXnodeP(double xnodeP);

        HalfLineAdder setXnodeQ(double xnodeQ);

        HalfLineAdder setR(double r);

        HalfLineAdder setX(double x);

        HalfLineAdder setG1(double g1);

        HalfLineAdder setG2(double g2);

        HalfLineAdder setB1(double b1);

        HalfLineAdder setB2(double b2);

        TieLineAdder add();
    }

    TieLineAdder setUcteXnodeCode(String ucteXnodeCode);

    TieLineAdder.HalfLineAdder newHalfLine1();

    TieLineAdder.HalfLineAdder newHalfLine2();

    TieLine add();

}
