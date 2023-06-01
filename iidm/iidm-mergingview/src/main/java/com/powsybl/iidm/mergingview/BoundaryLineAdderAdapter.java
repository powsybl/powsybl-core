/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.BoundaryLineAdder;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BoundaryLineAdderAdapter extends AbstractInjectionAdderAdapter<BoundaryLine, BoundaryLineAdder> implements BoundaryLineAdder {

    class GenerationAdderAdapter implements GenerationAdder {

        private final GenerationAdder delegate;

        GenerationAdderAdapter(GenerationAdder delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public GenerationAdder setTargetP(double targetP) {
            delegate.setTargetP(targetP);
            return this;
        }

        @Override
        public GenerationAdder setMaxP(double maxP) {
            delegate.setMaxP(maxP);
            return this;
        }

        @Override
        public GenerationAdder setMinP(double minP) {
            delegate.setMinP(minP);
            return this;
        }

        @Override
        public GenerationAdder setTargetQ(double targetQ) {
            delegate.setTargetQ(targetQ);
            return this;
        }

        @Override
        public GenerationAdder setVoltageRegulationOn(boolean voltageRegulationOn) {
            delegate.setVoltageRegulationOn(voltageRegulationOn);
            return this;
        }

        @Override
        public GenerationAdder setTargetV(double targetV) {
            delegate.setTargetV(targetV);
            return this;
        }

        @Override
        public BoundaryLineAdder add() {
            delegate.add();
            return BoundaryLineAdderAdapter.this;
        }
    }

    BoundaryLineAdderAdapter(final BoundaryLineAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public BoundaryLine add() {
        checkAndSetUniqueId();
        final BoundaryLine bl = getDelegate().add();
        getIndex().checkNewBoundaryLine(bl);
        return getIndex().getBoundaryLine(bl);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public BoundaryLineAdder setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public BoundaryLineAdder setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public BoundaryLineAdder setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public BoundaryLineAdder setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public BoundaryLineAdder setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public BoundaryLineAdder setB(final double b) {
        getDelegate().setB(b);
        return this;
    }

    @Override
    public BoundaryLineAdder setUcteXnodeCode(final String ucteXnodeCode) {
        getDelegate().setUcteXnodeCode(ucteXnodeCode);
        return this;
    }

    @Override
    public GenerationAdder newGeneration() {
        return new GenerationAdderAdapter(getDelegate().newGeneration());
    }
}
