package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseArea;
import com.powsybl.psse.model.pf.PsseBus;

import java.util.HashSet;
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
        String areaId = "A" + psseArea.getI();
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
        Set<VoltageLevel> voltageLevels = extractVoltageLevels(area);
        Set<Terminal> boundaryTerminals = findLineBoundaryTerminals(voltageLevels);

        for (var terminal : boundaryTerminals) {
            addAreaBoundary(area, terminal, IS_AC);
        }
    }

    private Set<Terminal> findLineBoundaryTerminals(Set<VoltageLevel> voltageLevels) {
        Set<Terminal> boundaryTerminals = new HashSet<>();
        getNetwork().getLines().forEach(line -> {
            boolean isTerminal1InArea = voltageLevels.contains(line.getTerminal1().getVoltageLevel());
            boolean isTerminal2InArea = voltageLevels.contains(line.getTerminal2().getVoltageLevel());
            if (isTerminal1InArea != isTerminal2InArea) {
                boundaryTerminals.add(isTerminal1InArea ? line.getTerminal1() : line.getTerminal2());
            }
        });
        return boundaryTerminals;
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

    private static final boolean IS_AC = true;
    private static final String CONTROL_AREA_TYPE = "ControlArea";

    private final PsseArea psseArea;
    private final List<PsseBus> buses;
}