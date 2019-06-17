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

    private final List<RatioTapChangerStepImpl> steps = new ArrayList<>();

    private boolean loadTapChangingCapabilities = false;

    private boolean regulating = false;

    private double targetV = Double.NaN;

    private TerminalExt regulationTerminal;

    class StepAdderImpl extends AbstractTapChangerStepAdder<StepAdderImpl> implements StepAdder {

        @Override
        public RatioTapChangerAdderImpl endStep() {
            checkValues(parent);
            RatioTapChangerStepImpl step = new RatioTapChangerStepImpl(rho, r, x, g, b);
            steps.add(step);
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
    public RatioTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
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
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public RatioTapChanger add() {
        if (tapPosition == null) {
            throw new ValidationException(parent, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(parent, "ratio tap changer should have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkRatioTapChangerRegulation(parent, loadTapChangingCapabilities, regulating, regulationTerminal, targetV, getNetwork());
        RatioTapChangerImpl tapChanger
                = new RatioTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, loadTapChangingCapabilities,
                                          tapPosition, regulating, targetV);
        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

}
