/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerAdderImpl extends AbstractBranchAdder<TwoWindingsTransformerAdderImpl> implements TwoWindingsTransformerAdder {

    private final SubstationImpl substation;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g1 = Double.NaN;

    private double b1 = Double.NaN;

    private double g2 = Double.NaN;

    private double b2 = Double.NaN;

    private double ratedU1 = Double.NaN;

    private double ratedU2 = Double.NaN;

    private int phaseAngleClock1 = 0;

    private int phaseAngleClock2 = 0;

    TwoWindingsTransformerAdderImpl(SubstationImpl substation) {
        this.substation = substation;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return substation.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "2 windings transformer";
    }

    @Override
    public TwoWindingsTransformerAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setB1(double b) {
        this.b1 = b;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setG1(double g) {
        this.g1 = g;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setB2(double b) {
        this.b2 = b;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setG2(double g) {
        this.g2 = g;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU1(double ratedU1) {
        this.ratedU1 = ratedU1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU2(double ratedU2) {
        this.ratedU2 = ratedU2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setPhaseAngleClock1(int phaseAngleClock1) {
        this.phaseAngleClock1 = phaseAngleClock1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setPhaseAngleClock2(int phaseAngleClock2) {
        this.phaseAngleClock2 = phaseAngleClock2;
        return this;
    }

    @Override
    public TwoWindingsTransformer add() {
        String id = checkAndGetUniqueId();
        VoltageLevelExt voltageLevel1 = checkAndGetVoltageLevel1();
        VoltageLevelExt voltageLevel2 = checkAndGetVoltageLevel2();
        if (voltageLevel1.getSubstation() != substation || voltageLevel2.getSubstation() != substation) {
            throw new ValidationException(this,
                    "the 2 windings of the transformer shall belong to the substation '"
                    + substation.getId() + "' ('" + voltageLevel1.getSubstation().getId() + "', '"
                    + voltageLevel2.getSubstation().getId() + "')");
        }
        TerminalExt terminal1 = checkAndGetTerminal1();
        TerminalExt terminal2 = checkAndGetTerminal2();

        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g1);
        ValidationUtil.checkB(this, b1);
        ValidationUtil.checkG(this, g2);
        ValidationUtil.checkB(this, b2);
        ValidationUtil.checkRatedU1(this, ratedU1);
        ValidationUtil.checkRatedU2(this, ratedU2);

        TwoWindingsTransformerImpl transformer
                = new TwoWindingsTransformerImpl(id, getName(),
                                                 voltageLevel1.getSubstation(),
                                                 r, x, g1, b1, g2, b2,
                                                 ratedU1, ratedU2);
        terminal1.setNum(1);
        terminal2.setNum(2);
        transformer.addTerminal(terminal1);
        transformer.addTerminal(terminal2);

        // check that the two windings transformer is attachable on both side
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);

        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        getNetwork().getIndex().checkAndAdd(transformer);
        getNetwork().getListeners().notifyCreation(transformer);
        return transformer;

    }

}
