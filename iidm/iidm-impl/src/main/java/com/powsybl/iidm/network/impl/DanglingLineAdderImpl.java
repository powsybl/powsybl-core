/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.iidm.network.ValidationUtil;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 *
 */
class DanglingLineAdderImpl extends AbstractInjectionAdder<DanglingLineAdderImpl> implements DanglingLineAdder {

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = 0.0;

    private double b = 0.0;

    private String pairingKey;

    private String countryFrom;

    private String countryTo;

    private GenerationAdderImpl generationAdder;

    DanglingLineAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    void setGenerationAdder(GenerationAdderImpl adder) {
        generationAdder = adder;
    }

    @Override
    protected String getTypeDescription() {
        return "Dangling line";
    }

    @Override
    public DanglingLineAdderImpl setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public DanglingLineAdder setPairingKey(String pairingKey) {
        this.pairingKey = pairingKey;
        return this;
    }

    @Override
    public DanglingLineAdder setCountryFrom(String countryFrom) {
        this.countryFrom = countryFrom;
        return this;
    }

    @Override
    public DanglingLineAdder setCountryTo(String countryTo) {
        this.countryTo = countryTo;
        return this;
    }

    @Override
    public GenerationAdder newGeneration() {
        return new GenerationAdderImpl(this);
    }

    @Override
    public DanglingLineImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();

        network.setValidationLevelIfGreaterThan(ValidationUtil.checkP0(this, p0, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkQ0(this, q0, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);

        DanglingLineImpl.GenerationImpl generation = null;
        if (generationAdder != null) {
            generation = generationAdder.build();
        }

        DanglingLineImpl danglingLine = new DanglingLineImpl(network.getRef(), id, getName(), isFictitious(), p0, q0, r, x, g, b, pairingKey, countryFrom, countryTo, generation);
        danglingLine.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(danglingLine);
        network.getListeners().notifyCreation(danglingLine);
        return danglingLine;
    }

}
