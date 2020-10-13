/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 *
 * @deprecated Use {@link TieLineAdder#newHalfLine1()} or {@link TieLineAdder#newHalfLine2()} instead.
 */
@Deprecated
class TieLineAdderProxy implements TieLineAdder, Validable {

    private final TieLineAdderImpl delegate;

    private TieLineAdderImpl.HalfLineAdderImpl halfLine1;

    private TieLineAdderImpl.HalfLineAdderImpl halfLine2;

    private TieLineAdderImpl.HalfLineAdderImpl activeHalf;

    TieLineAdderProxy(TieLineAdderImpl delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    private TieLineAdderImpl.HalfLineAdderImpl getActiveHalf() {
        if (activeHalf == null) {
            throw new ValidationException(this, "No active half of the line");
        }
        return activeHalf;
    }

    @Override
    public TieLineAdder setId(String id) {
        if (activeHalf == null) {
            delegate.setId(id);
        } else {
            activeHalf.setId(id);
        }
        return this;
    }

    @Override
    public TieLineAdder setName(String name) {
        if (activeHalf == null) {
            delegate.setName(name);
        } else {
            activeHalf.setName(name);
        }
        return this;
    }

    @Override
    public TieLineAdder setFictitious(boolean fictitious) {
        if (activeHalf == null) {
            delegate.setFictitious(fictitious);
        } else {
            activeHalf.setFictitious(fictitious);
        }
        return this;
    }

    @Override
    public TieLineAdder setR(double r) {
        getActiveHalf().setR(r);
        return this;
    }

    @Override
    public TieLineAdder setX(double x) {
        getActiveHalf().setX(x);
        return this;
    }

    @Override
    public TieLineAdder setG1(double g1) {
        getActiveHalf().setG1(g1);
        return this;
    }

    @Override
    public TieLineAdder setB1(double b1) {
        getActiveHalf().setB1(b1);
        return this;
    }

    @Override
    public TieLineAdder setG2(double g2) {
        getActiveHalf().setG2(g2);
        return this;
    }

    @Override
    public TieLineAdder setB2(double b2) {
        getActiveHalf().setB2(b2);
        return this;
    }

    public TieLineAdder setXnodeP(double xnodeP) {
        getActiveHalf().setXnodeP(xnodeP);
        return this;
    }

    @Override
    public TieLineAdder setXnodeQ(double xnodeQ) {
        getActiveHalf().setXnodeQ(xnodeQ);
        return this;
    }

    @Override
    public TieLine add() {
        // Check if half line have been set
        if (halfLine1 == null) {
            throw new ValidationException(this, "half line 1 is not set");
        }
        if (halfLine2 == null) {
            throw new ValidationException(this, "half line 2 is not set");
        }

        // Reset activeHalf
        activeHalf = null;

        // Call validity check & set half lines on delegate
        halfLine1.add();
        halfLine2.add();

        return delegate.add();
    }

    @Override
    public TieLineAdder line1() {
        this.halfLine1 = delegate.newHalfLine1();
        this.activeHalf = halfLine1;
        return this;
    }

    @Override
    public TieLineAdder line2() {
        this.halfLine2 = delegate.newHalfLine2();
        this.activeHalf = halfLine2;
        return this;
    }

    @Override
    public TieLineAdderImpl.HalfLineAdderImpl newHalfLine1() {
        throw new UnsupportedOperationException("Use line1() instead");
    }

    @Override
    public TieLineAdderImpl.HalfLineAdderImpl newHalfLine2() {
        throw new UnsupportedOperationException("Use line2() instead");
    }

    // Following methods are simply delegated

    @Override
    public TieLineAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        delegate.setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public TieLineAdder setUcteXnodeCode(String ucteXnodeCode) {
        delegate.setUcteXnodeCode(ucteXnodeCode);
        return this;
    }

    @Override
    public TieLineAdder setVoltageLevel1(String voltageLevelId1) {
        delegate.setVoltageLevel1(voltageLevelId1);
        return this;
    }

    @Override
    public TieLineAdder setNode1(int node1) {
        delegate.setNode1(node1);
        return this;
    }

    @Override
    public TieLineAdder setBus1(String bus1) {
        delegate.setBus1(bus1);
        return this;
    }

    @Override
    public TieLineAdder setConnectableBus1(String connectableBus1) {
        delegate.setConnectableBus1(connectableBus1);
        return this;
    }

    @Override
    public TieLineAdder setVoltageLevel2(String voltageLevelId2) {
        delegate.setVoltageLevel2(voltageLevelId2);
        return this;
    }

    @Override
    public TieLineAdder setNode2(int node2) {
        delegate.setNode2(node2);
        return this;
    }

    @Override
    public TieLineAdder setBus2(String bus2) {
        delegate.setBus2(bus2);
        return this;
    }

    @Override
    public TieLineAdder setConnectableBus2(String connectableBus2) {
        delegate.setConnectableBus2(connectableBus2);
        return this;
    }

    @Override
    public String getMessageHeader() {
        return delegate.getMessageHeader();
    }
}
