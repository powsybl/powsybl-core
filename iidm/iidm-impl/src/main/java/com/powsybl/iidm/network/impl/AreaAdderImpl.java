/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.ref.RefChain;
import com.powsybl.iidm.network.*;
import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaAdderImpl extends AbstractIdentifiableAdder<AreaAdderImpl> implements AreaAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private String areaType;

    private Double acNetInterchangeTarget;

    private final Set<VoltageLevel> voltageLevels;

    private final List<Pair<Terminal, Boolean>> terminalBoundaries;

    private final List<Pair<DanglingLine, Boolean>> danglingLineBoundaries;

    AreaAdderImpl(Ref<NetworkImpl> networkRef, final RefChain<SubnetworkImpl> subnetworkRef) {
        this.networkRef = networkRef;
        this.subnetworkRef = subnetworkRef;
        this.terminalBoundaries = new ArrayList<>();
        this.danglingLineBoundaries = new ArrayList<>();
        this.voltageLevels = new HashSet<>();
    }

    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    public AreaAdder setAreaType(String areaType) {
        this.areaType = areaType;
        return this;
    }

    public AreaAdder setAcNetInterchangeTarget(Double acNetInterchangeTarget) {
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        return this;
    }

    public AreaAdder addVoltageLevel(VoltageLevel voltageLevel) {
        this.voltageLevels.add(voltageLevel);
        return this;
    }

    public AreaAdder addAreaBoundary(Terminal terminal, boolean ac) {
        this.terminalBoundaries.add(Pair.of(terminal, ac));
        return this;
    }

    public AreaAdder addAreaBoundary(DanglingLine danglingLine, boolean ac) {
        this.danglingLineBoundaries.add(Pair.of(danglingLine, ac));
        return this;
    }

    protected Ref<NetworkImpl> getNetworkRef() {
        return networkRef;
    }

    protected String getAreaType() {
        return areaType;
    }

    protected Double getAcNetInterchangeTarget() {
        return acNetInterchangeTarget;
    }

    protected Set<VoltageLevel> getVoltageLevels() {
        return voltageLevels;
    }

    protected List<Pair<Terminal, Boolean>> getTerminalAreaBoundaries() {
        return terminalBoundaries;
    }

    protected List<Pair<DanglingLine, Boolean>> getDanglingLineAreaBoundaries() {
        return danglingLineBoundaries;
    }

    @Override
    public Area add() {
        String id = checkAndGetUniqueId();
        AreaImpl area = new AreaImpl(getNetworkRef(), subnetworkRef, id, getName(), isFictitious(), getAreaType(), getAcNetInterchangeTarget());
        getTerminalAreaBoundaries().forEach(pair -> area.newAreaBoundary().setTerminal(pair.getFirst()).setAc(pair.getSecond()).add());
        getDanglingLineAreaBoundaries().forEach(pair -> area.newAreaBoundary().setBoundary(pair.getFirst().getBoundary()).setAc(pair.getSecond()).add());
        getVoltageLevels().forEach(area::addVoltageLevel);
        getNetwork().getIndex().checkAndAdd(area);
        getNetwork().getListeners().notifyCreation(area);
        return area;
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }
}

