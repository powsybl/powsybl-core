/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerAdderImpl implements PhaseTapChangerAdder {

    private final TwoWindingsTransformerImpl transformer;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<PhaseTapChangerTapImpl> taps = new ArrayList<>();

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;

    private double regulationValue = Double.NaN;

    private boolean regulating = false;

    private TerminalExt regulationTerminal;

    class TapAdderImpl implements TapAdder {

        private double phaseShift = 0.0;

        private double ratio = 1.0;

        private double rdr = 0.0;

        private double rdx = 0.0;

        private double rdg = 0.0;

        private double rdb = 0.0;

        @Override
        public TapAdder setPhaseShift(double phaseShift) {
            this.phaseShift = phaseShift;
            return this;
        }

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
        public PhaseTapChangerAdder endTap() {
            if (Double.isNaN(phaseShift)) {
                throw new ValidationException(transformer, "tap phaseShift is not set");
            }
            if (Double.isNaN(ratio)) {
                throw new ValidationException(transformer, "tap ratio is not set");
            }
            if (Double.isNaN(rdr)) {
                throw new ValidationException(transformer, "tap rdr is not set");
            }
            if (Double.isNaN(rdx)) {
                throw new ValidationException(transformer, "tap rdx is not set");
            }
            if (Double.isNaN(rdg)) {
                throw new ValidationException(transformer, "tap rdg is not set");
            }
            if (Double.isNaN(rdb)) {
                throw new ValidationException(transformer, "tap rdb is not set");
            }
            PhaseTapChangerTapImpl tap = new PhaseTapChangerTapImpl(phaseShift, ratio, rdr, rdx, rdg, rdb);
            taps.add(tap);
            return PhaseTapChangerAdderImpl.this;
        }

    }

    PhaseTapChangerAdderImpl(TwoWindingsTransformerImpl transformer) {
        this.transformer = transformer;
    }

    NetworkImpl getNetwork() {
        return transformer.getNetwork();
    }

    @Override
    public PhaseTapChangerAdder setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return this;
    }

    @Override
    public TapAdder endTap() {
        return new TapAdderImpl();
    }

    @Override
    public PhaseTapChanger add() {
        if (tapPosition == null) {
            throw new ValidationException(transformer, "tap position is not set");
        }
        if (taps.isEmpty()) {
            throw new ValidationException(transformer, "a phase tap changer shall have at least one tap");
        }
        int highTapPosition = lowTapPosition + taps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(transformer, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkPhaseTapChangerRegulation(transformer, regulationMode, regulationValue, regulating, regulationTerminal, getNetwork());
        PhaseTapChangerImpl tapChanger
                = new PhaseTapChangerImpl(transformer, lowTapPosition, taps, regulationTerminal, tapPosition, regulating, regulationMode, regulationValue);
        transformer.setPhaseTapChanger(tapChanger);
        return tapChanger;
    }

}
