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

    TieLineAdder setR(float r);

    TieLineAdder setX(float x);

    TieLineAdder setG1(float g1);

    TieLineAdder setB1(float b1);

    TieLineAdder setG2(float g2);

    TieLineAdder setB2(float b2);

    TieLineAdder setXnodeP(float xnodeP);

    TieLineAdder setXnodeQ(float xnodeQ);

    TieLineAdder setUcteXnodeCode(String ucteXnodeCode);

    TieLineAdder line1();

    TieLineAdder line2();

    TieLine add();

}
