/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadAdderImpl extends AbstractInjectionAdder<LoadAdderImpl> implements LoadAdder {

    private LoadType loadType = LoadType.UNDEFINED;

    private AbstractLoadModelImpl model;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    LoadAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected String getTypeDescription() {
        return "Load";
    }

    @Override
    public LoadAdder setLoadType(LoadType loadType) {
        this.loadType = loadType;
        return this;
    }

    @Override
    public LoadAdderImpl setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public LoadAdderImpl setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    void setModel(AbstractLoadModelImpl model) {
        this.model = model;
    }

    @Override
    public ZipLoadModelAdder newZipModel() {
        return new ZipLoadModelAdderImpl(this);
    }

    @Override
    public ExponentialLoadModelAdder newExponentialModel() {
        return new ExponentialLoadModelAdderImpl(this);
    }

    @Override
    public LoadImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkLoadType(this, loadType);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkP0(this, p0, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkQ0(this, q0, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        LoadImpl load = new LoadImpl(getNetworkRef(), id, getName(), isFictitious(), loadType, model, p0, q0);
        if (model != null) {
            model.setLoad(load);
        }
        load.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        network.getIndex().checkAndAdd(load);
        network.getListeners().notifyCreation(load);
        return load;
    }

}
