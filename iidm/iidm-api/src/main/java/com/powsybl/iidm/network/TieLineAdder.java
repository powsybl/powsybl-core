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

    TieLineAdderProxy PROXY = new TieLineAdderProxy();

    /**
     * @deprecated Use {@link HalfLineAdder#setR(double)} instead.
     */
    @Deprecated
    default TieLineAdder setR(double r)  {
        return this;
    }

    /**
     * @deprecated Use {@link HalfLineAdder#setX(double)} instead.
     */
    @Deprecated
    default TieLineAdder setX(double x) {
        return this;
    }

    /**
     * @deprecated Use {@link HalfLineAdder#setG1(double)} instead.
     */
    @Deprecated
    default TieLineAdder setG1(double g1)  {
        return this;
    }

    /**
     * @deprecated Use {@link HalfLineAdder#setB1(double)} instead.
     */
    @Deprecated
    default TieLineAdder setB1(double b1)  {
        return this;
    }

    /**
     * @deprecated Use {@link HalfLineAdder#setG2(double)} instead.
     */
    @Deprecated
    default TieLineAdder setG2(double g2)  {
        return this;
    }

    /**
     * @deprecated Use {@link HalfLineAdder#setB2(double)} instead.
     */
    @Deprecated
    default TieLineAdder setB2(double b2)  {
        return this;
    }

    /**
     * @deprecated Use {@link HalfLineAdder#setXnodeP(double)} instead.
     */
    @Deprecated
    default TieLineAdder setXnodeP(double xnodeP) {
        return this;
    }

    /**
     * @deprecated Use {@link HalfLineAdder#setXnodeQ(double)} instead.
     */
    @Deprecated
    default TieLineAdder setXnodeQ(double xnodeQ) {
        return this;
    }

    /**
     * @deprecated Use {@link #newHalfLine1()} and {@link HalfLineAdder#add()} instead.
     */
    @Deprecated
    default TieLineAdder line1() {
        PROXY.setDelegate(this);
        PROXY.line1();
        return PROXY;
    }

    /**
     * @deprecated Use {@link #newHalfLine2()} and {@link HalfLineAdder#add()} instead.
     */
    @Deprecated
    default TieLineAdder line2() {
        PROXY.setDelegate(this);
        PROXY.line2();
        return PROXY;
    }

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

    default TieLineAdder.HalfLineAdder newHalfLine1() {
        throw new UnsupportedOperationException();
    }

    default TieLineAdder.HalfLineAdder newHalfLine2() {
        throw new UnsupportedOperationException();
    }

    TieLine add();

}
