/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class RatioTapChangerAdderImpl implements RatioTapChangerAdder {

    private final RatioTapChangerParent parent;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<RatioTapChangerTapImpl> taps = new ArrayList<>();

    private boolean onLoadTapChanger = false;

    private boolean regulating = false;

    private double targetV = Double.NaN;

    private TerminalExt regulationTerminal;

    class TapAdderImpl implements TapAdder {

        private double ratio = 1.0;

        private double rdr = 0.0;

        private double rdx = 0.0;

        private double rdg = 0.0;

        private double rdb = 0.0;

        @Override
        public TapAdder setRatio(double ratio) {
            this.ratio = ratio;
            return this;
        }

        @Override
        public TapAdder setRdr(double rdr) {
            this.rdr = rdr;
            return this;
        }

        @Override
        public TapAdder setRdx(double rdx) {
            this.rdx = rdx;
            return this;
        }

        @Override
        public TapAdder setRdg(double rdg) {
            this.rdg = rdg;
            return this;
        }

        @Override
        public TapAdder setRdb(double rdb) {
            this.rdb = rdb;
            return this;
        }

        @Override
        public RatioTapChangerAdder endTap() {
            if (Double.isNaN(ratio)) {
                throw new ValidationException(parent, "tap ratio is not set");
            }
            if (Double.isNaN(rdr)) {
                throw new ValidationException(parent, "tap rdr is not set");
            }
            if (Double.isNaN(rdx)) {
                throw new ValidationException(parent, "tap rdx is not set");
            }
            if (Double.isNaN(rdg)) {
                throw new ValidationException(parent, "tap rdg is not set");
            }
            if (Double.isNaN(rdb)) {
                throw new ValidationException(parent, "tap rdb is not set");
            }
            RatioTapChangerTapImpl tap = new RatioTapChangerTapImpl(ratio, rdr, rdx, rdg, rdb);
            taps.add(tap);
            return RatioTapChangerAdderImpl.this;
        }

    }

    RatioTapChangerAdderImpl(RatioTapChangerParent parent) {
        this.parent = parent;
    }

    NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

    @Override
    public RatioTapChangerAdder setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setOnLoadTapChanger(boolean onLoadTapChanger) {
        this.onLoadTapChanger = onLoadTapChanger;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return this;
    }

    @Override
    public TapAdder beginTap() {
        return new TapAdderImpl();
    }

    @Override
    public RatioTapChanger add() {
        if (tapPosition == null) {
            throw new ValidationException(parent, "tap position is not set");
        }
        if (taps.isEmpty()) {
            throw new ValidationException(parent, "ratio tap changer should have at least one tap");
        }
        int highTapPosition = lowTapPosition + taps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkRatioTapChangerRegulation(parent, onLoadTapChanger, regulating, regulationTerminal, targetV, getNetwork());
        RatioTapChangerImpl tapChanger
                = new RatioTapChangerImpl(parent, lowTapPosition, taps, regulationTerminal, onLoadTapChanger,
                                          tapPosition, regulating, targetV);
        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

}
