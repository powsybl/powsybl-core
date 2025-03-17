/**
 * Copyright (c) 2025, University of West Bohemia (https://www.zcu.cz)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseArea;
import com.powsybl.psse.model.pf.PsseBus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Petr Janecek {@literal <pjanecek at ntis.zcu.cz>}
 */

public class AreaConverter extends AbstractConverter {
    AreaConverter(PsseArea psseArea, List<PsseBus> buses, ContainersMapping containersMapping, Network network) {
        super(containersMapping, network);
        this.psseArea = psseArea;
        this.buses = buses;
    }

    Area create() {
        String areaId = AREA_ID_PREFIX + psseArea.getI();
        var area = getNetwork().newArea()
                .setId(areaId)
                .setAreaType(CONTROL_AREA_TYPE)
                .setInterchangeTarget(psseArea.getPdes())
                .setName(psseArea.getArname())
                .add();

        addVoltageLevelsToArea(area);
        addAreaBoundaries(area);

        return area;
    }

    private void addVoltageLevelsToArea(Area area) {
        for (var bus : buses) {
            if (bus.getArea() == psseArea.getI()) {
                String voltageLevelId = getContainersMapping().getVoltageLevelId(bus.getI());
                VoltageLevel voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
                area.addVoltageLevel(voltageLevel);
            }
        }
    }

    private void addAreaBoundaries(Area area) {
        Set<VoltageLevel> areaVoltageLevels = extractVoltageLevels(area);
        addLineBoundaryTerminals(area, areaVoltageLevels);
        addHvdcLinesBoundaryTerminals(area, areaVoltageLevels);
    }

    private void addLineBoundaryTerminals(Area area, Set<VoltageLevel> areaVoltageLevels) {
        getNetwork().getLines().forEach(line ->
                processTerminals(line.getTerminal1(), line.getTerminal2(), area, areaVoltageLevels, IS_AC));
    }

    private void addHvdcLinesBoundaryTerminals(Area area, Set<VoltageLevel> areaVoltageLevels) {
        getNetwork().getHvdcLines().forEach(line -> processTerminals(
                line.getConverterStation1().getTerminal(),
                line.getConverterStation2().getTerminal(),
                area, areaVoltageLevels, IS_DC));
    }

    private void processTerminals(Terminal terminal1, Terminal terminal2, Area area, Set<VoltageLevel> areaVoltageLevels,
                                  boolean isAC) {
        var boundaryTerminal = boundaryTerminal(terminal1, terminal2, areaVoltageLevels);
        if (boundaryTerminal != null) {
            addAreaBoundary(area, boundaryTerminal, isAC);
        }
    }

    private Terminal boundaryTerminal(Terminal terminal1, Terminal terminal2, Set<VoltageLevel> areaVoltageLevels) {
        var isTerminal1InArea = areaVoltageLevels.contains(terminal1.getVoltageLevel());
        var isTerminal2InArea = areaVoltageLevels.contains(terminal2.getVoltageLevel());
        if (isTerminal1InArea != isTerminal2InArea) {
            return isTerminal1InArea ? terminal1 : terminal2;
        }
        return null;
    }

    private Set<VoltageLevel> extractVoltageLevels(Area area) {
        return StreamSupport.stream(area.getVoltageLevels().spliterator(), false)
                .collect(Collectors.toSet());
    }

    private void addAreaBoundary(Area area, Terminal terminal, boolean isAC) {
        area.newAreaBoundary()
                .setTerminal(terminal)
                .setAc(isAC)
                .add();
    }

    private static final String AREA_ID_PREFIX = "A";
    private static final boolean IS_AC = true;
    private static final boolean IS_DC = false;
    private static final String CONTROL_AREA_TYPE = "ControlArea";

    private final PsseArea psseArea;
    private final List<PsseBus> buses;
}
