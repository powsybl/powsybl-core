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

    /**
     * @deprecated Use {@link HalfLineAdder#setR(double)} instead.
     */
    @Deprecated
    TieLineAdder setR(double r);

    /**
     * @deprecated Use {@link HalfLineAdder#setX(double)} instead.
     */
    @Deprecated
    TieLineAdder setX(double x);

    /**
     * @deprecated Use {@link HalfLineAdder#setG1(double)} instead.
     */
    @Deprecated
    TieLineAdder setG1(double g1);

    /**
     * @deprecated Use {@link HalfLineAdder#setB1(double)} instead.
     */
    @Deprecated
    TieLineAdder setB1(double b1);

    /**
     * @deprecated Use {@link HalfLineAdder#setG2(double)} instead.
     */
    @Deprecated
    TieLineAdder setG2(double g2);

    /**
     * @deprecated Use {@link HalfLineAdder#setB2(double)} instead.
     */
    @Deprecated
    TieLineAdder setB2(double b2);

    /**
     * @deprecated Use {@link HalfLineAdder#setXnodeP(double)} instead.
     */
    @Deprecated
    TieLineAdder setXnodeP(double xnodeP);

    /**
     * @deprecated Use {@link HalfLineAdder#setXnodeQ(double)} instead.
     */
    @Deprecated
    TieLineAdder setXnodeQ(double xnodeQ);

    /**
     * @deprecated Use {@link #newHalfLine1()} and {@link HalfLineAdder#add()} instead.
     */
    @Deprecated
    TieLineAdder line1();

    /**
     * @deprecated Use {@link #newHalfLine2()} and {@link HalfLineAdder#add()} instead.
     */
    @Deprecated
    TieLineAdder line2();

    interface HalfLineAdder {

        TieLineAdder.HalfLineAdder setId(String id);

        TieLineAdder.HalfLineAdder setName(String name);

        TieLineAdder.HalfLineAdder setFictitious(boolean fictitious);

        TieLineAdder.HalfLineAdder setXnodeP(double xnodeP);

        TieLineAdder.HalfLineAdder setXnodeQ(double xnodeQ);

        TieLineAdder.HalfLineAdder setR(double r);

        TieLineAdder.HalfLineAdder setX(double x);

        TieLineAdder.HalfLineAdder setG1(double g1);

        TieLineAdder.HalfLineAdder setG2(double g2);

        TieLineAdder.HalfLineAdder setB1(double b1);

        TieLineAdder.HalfLineAdder setB2(double b2);

        TieLineAdder add();
    }

    TieLineAdder setUcteXnodeCode(String ucteXnodeCode);

    TieLineAdder.HalfLineAdder newHalfLine1();

    TieLineAdder.HalfLineAdder newHalfLine2();

    TieLine add();

}
