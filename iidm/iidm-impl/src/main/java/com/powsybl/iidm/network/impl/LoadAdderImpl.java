/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadAdderImpl extends AbstractInjectionAdder<LoadAdderImpl> implements LoadAdder {

    private final VoltageLevelExt voltageLevel;

    private LoadType loadType = LoadType.UNDEFINED;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    LoadAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
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

    @Override
    public LoadImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkLoadType(this, loadType);
        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkQ0(this, q0);
        LoadImpl load = new LoadImpl(getNetwork().getRef(), id, getName(), loadType, p0, q0);
        load.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(load);
        getNetwork().getListeners().notifyCreation(load);
        return load;
    }

}
