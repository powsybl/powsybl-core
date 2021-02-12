/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class BoundaryAdapter implements Boundary {

    static class BoundaryTerminalAdapter implements BoundaryTerminal {

        private final boolean merged;
        private final BoundaryTerminal delegate;
        private final MergingViewIndex index;

        BoundaryTerminalAdapter(boolean merged, BoundaryTerminal delegate, MergingViewIndex index) {
            this.merged = merged;
            this.delegate = Objects.requireNonNull(delegate);
            this.index = Objects.requireNonNull(index);
        }

        @Override
        public VoltageLevel getVoltageLevel() {
            return index.getVoltageLevel(delegate.getVoltageLevel());
        }

        @Override
        public Connectable getConnectable() {
            return index.getConnectable(delegate.getConnectable());
        }

        @Override
        public double getP() {
            return delegate.getP();
        }

        @Override
        public double getQ() {
            return delegate.getQ();
        }

        @Override
        public double getI() {
            return delegate.getI();
        }

        @Override
        public boolean isConnected() {
            return merged || delegate.isConnected();
        }
    }

    private final boolean merged;
    private final Boundary delegate;
    private final MergingViewIndex index;

    BoundaryAdapter(boolean merged, Boundary delegate, MergingViewIndex index) {
        this.merged = merged;
        this.delegate = Objects.requireNonNull(delegate);
        this.index = Objects.requireNonNull(index);
    }

    @Override
    public double getV() {
        return delegate.getV();
    }

    @Override
    public double getAngle() {
        return delegate.getAngle();
    }

    @Override
    public double getP() {
        return delegate.getP();
    }

    @Override
    public double getQ() {
        return delegate.getQ();
    }

    @Override
    public BoundaryTerminal getTerminal() {
        return new BoundaryTerminalAdapter(merged, delegate.getTerminal(), index);
    }
}
