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
import java.util.function.BooleanSupplier;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class BoundaryAdapter extends AbstractAdapter<Boundary> implements Boundary {

    static class BoundaryTerminalAdapter extends AbstractAdapter<BoundaryTerminal> implements BoundaryTerminal {

        private final BooleanSupplier isMerged;

        BoundaryTerminalAdapter(BooleanSupplier isMerged, BoundaryTerminal delegate, MergingViewIndex index) {
            super(delegate, index);
            this.isMerged = Objects.requireNonNull(isMerged);
        }

        @Override
        public VoltageLevel getVoltageLevel() {
            return getIndex().getVoltageLevel(getDelegate().getVoltageLevel());
        }

        @Override
        public Connectable getConnectable() {
            return getIndex().getConnectable(getDelegate().getConnectable());
        }

        @Override
        public double getP() {
            return getDelegate().getP();
        }

        @Override
        public double getQ() {
            return getDelegate().getQ();
        }

        @Override
        public double getI() {
            return getDelegate().getI();
        }

        @Override
        public boolean isConnected() {
            return isMerged.getAsBoolean() || getDelegate().isConnected();
        }
    }

    private final BooleanSupplier isMerged;

    BoundaryAdapter(BooleanSupplier isMerged, Boundary delegate, MergingViewIndex index) {
        super(delegate, index);
        this.isMerged = Objects.requireNonNull(isMerged);
    }

    @Override
    public double getV() {
        return getDelegate().getV();
    }

    @Override
    public double getAngle() {
        return getDelegate().getAngle();
    }

    @Override
    public double getP() {
        return getDelegate().getP();
    }

    @Override
    public double getQ() {
        return getDelegate().getQ();
    }

    @Override
    public BoundaryTerminal getTerminal() {
        return getIndex().getBoundaryTerminal(isMerged, getDelegate().getTerminal());
    }
}
