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

    private double g = Double.NaN;

    private double b = Double.NaN;

    private double ratedU1 = Double.NaN;

    private double ratedU2 = Double.NaN;

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
    public TwoWindingsTransformerAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setG(double g) {
        this.g = g;
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
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);
        ValidationUtil.checkRatedU1(this, ratedU1);
        ValidationUtil.checkRatedU2(this, ratedU2);

        TwoWindingsTransformerImpl transformer
                = new TwoWindingsTransformerImpl(id, getName(),
                                                 voltageLevel1.getSubstation(),
                                                 r, x, g, b,
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
        getNetwork().getObjectStore().checkAndAdd(transformer);
        getNetwork().getListeners().notifyCreation(transformer);
        return transformer;

    }

}
