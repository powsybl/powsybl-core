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

import java.util.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaAdderImpl extends AbstractIdentifiableAdder<AreaAdderImpl> implements AreaAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private String areaType;

    private double acInterchangeTarget;

    private final Set<VoltageLevel> voltageLevels;

    private final Map<Terminal, Boolean> terminalAreaBoundaries;

    private final Map<Boundary, Boolean> boundaryAreaBoundaries;

    AreaAdderImpl(Ref<NetworkImpl> networkRef, final RefChain<SubnetworkImpl> subnetworkRef) {
        this.networkRef = networkRef;
        this.subnetworkRef = subnetworkRef;
        this.terminalAreaBoundaries = new HashMap<>();
        this.boundaryAreaBoundaries = new HashMap<>();
        this.voltageLevels = new HashSet<>();
        this.acInterchangeTarget = Double.NaN;
    }

    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    public AreaAdder setAreaType(String areaType) {
        this.areaType = areaType;
        return this;
    }

    public AreaAdder setAcInterchangeTarget(double acInterchangeTarget) {
        this.acInterchangeTarget = acInterchangeTarget;
        return this;
    }

    public AreaAdder addVoltageLevel(VoltageLevel voltageLevel) {
        this.voltageLevels.add(voltageLevel);
        return this;
    }

    public AreaAdder addAreaBoundary(Terminal terminal, boolean ac) {
        this.terminalAreaBoundaries.put(terminal, ac);
        return this;
    }

    public AreaAdder addAreaBoundary(Boundary boundary, boolean ac) {
        this.boundaryAreaBoundaries.put(boundary, ac);
        return this;
    }

    protected Set<VoltageLevel> getVoltageLevels() {
        return voltageLevels;
    }

    @Override
    public Area add() {
        String id = checkAndGetUniqueId();
        AreaImpl area = new AreaImpl(networkRef, subnetworkRef, id, getName(), isFictitious(), areaType, acInterchangeTarget);
        terminalAreaBoundaries.forEach((terminal, ac) -> area.newAreaBoundary().setTerminal(terminal).setAc(ac).add());
        boundaryAreaBoundaries.forEach((boundary, ac) -> area.newAreaBoundary().setBoundary(boundary).setAc(ac).add());
        voltageLevels.forEach(area::addVoltageLevel);
        getNetwork().getIndex().checkAndAdd(area);
        getNetwork().getListeners().notifyCreation(area);
        return area;
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }
}

