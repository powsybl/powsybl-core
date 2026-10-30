/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.converter.util.UcteExporterReports;
import com.powsybl.ucte.network.UcteElement;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteElementStatus;
import com.powsybl.ucte.network.UcteNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exports as out of operation the elements that are left without voltage in the network's loadflow results : lines,
 * two-winding transformers, tie line halves, boundary lines and busbar couplers having a terminal connected to a bus
 * without voltage. If the main connected component has no voltage at all, the network has no load flow results: nothing
 * is changed and a warning is reported.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
@AutoService(UcteExportPostProcessor.class)
public class DeenergizedBranchesPostProcessor implements UcteExportPostProcessor {

    public static final String NAME = "deenergized-branches-out-of-operation";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeenergizedBranchesPostProcessor.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, UcteNetwork ucteNetwork, UcteExporterContext context) {
        boolean hasLoadFlowResults = network.getBusView().getBusStream()
                                            .filter(Bus::isInMainConnectedComponent)
                                            .anyMatch(bus -> !Double.isNaN(bus.getV()));
        if (!hasLoadFlowResults) {
            LOGGER.warn("No load flow results in the main connected component: no element exported out of operation");
            UcteExporterReports.noLoadFlowResults(context.getReportNode());
            return;
        }

        // Same order as the conversion of the exporter
        NamingStrategy namingStrategy = context.getNamingStrategy();
        Map<UcteElementId, IdentifiableType> deenergizedElements = new LinkedHashMap<>();

        network.getSubstations().forEach(substation -> substation.getVoltageLevels().forEach(voltageLevel ->
                voltageLevel.getBusBreakerView().getSwitches().forEach(sw -> {
                    if (isDeenergized(voltageLevel, sw)) {
                        setOutOfOperation(ucteNetwork.getLine(namingStrategy.getUcteElementId(sw)),
                                sw.getType(),
                                deenergizedElements);
                    }
                })));

        deenergizeBoundaryLines(network, ucteNetwork, namingStrategy, deenergizedElements);
        deenergizeLines(network, ucteNetwork, namingStrategy, deenergizedElements);
        deenergizeTieLines(network, ucteNetwork, namingStrategy, deenergizedElements);
        deenergizeTransformers(network, ucteNetwork, namingStrategy, deenergizedElements);

        if (!deenergizedElements.isEmpty()) {
            ReportNode reportNode =
                    UcteExporterReports.deenergizedElements(context.getReportNode(), deenergizedElements.size());
            deenergizedElements.forEach((ucteElementId, elementType) ->
                    UcteExporterReports.deenergizedElement(reportNode, elementType, ucteElementId.toString()));
        }
    }

    private static void deenergizeTransformers(Network network,
                                               UcteNetwork ucteNetwork,
                                               NamingStrategy namingStrategy,
                                               Map<UcteElementId, IdentifiableType> deenergizedElements) {
        network.getTwoWindingsTransformers().forEach(transformer -> {
            if (isDeenergized(transformer)) {
                setOutOfOperation(ucteNetwork.getTransformer(namingStrategy.getUcteElementId(transformer)),
                        transformer.getType(),
                        deenergizedElements);
            }
        });
    }

    private static void deenergizeTieLines(Network network,
                                           UcteNetwork ucteNetwork,
                                           NamingStrategy namingStrategy,
                                           Map<UcteElementId, IdentifiableType> deenergizedElements) {
        network.getTieLines().forEach(tieLine -> {
            for (BoundaryLine boundaryLine : new BoundaryLine[]{tieLine.getBoundaryLine1(),
                    tieLine.getBoundaryLine2()}) {
                if (isDeenergized(boundaryLine.getTerminal())) {
                    setOutOfOperation(ucteNetwork.getLine(namingStrategy.getUcteElementId(boundaryLine.getId())),
                            tieLine.getType(),
                            deenergizedElements);
                }
            }
        });
    }

    private static void deenergizeLines(Network network,
                                        UcteNetwork ucteNetwork,
                                        NamingStrategy namingStrategy,
                                        Map<UcteElementId, IdentifiableType> deenergizedElements) {
        network.getLines().forEach(line -> {
            if (isDeenergized(line)) {
                setOutOfOperation(ucteNetwork.getLine(namingStrategy.getUcteElementId(line)), line.getType(),
                        deenergizedElements);
            }
        });
    }

    private static void deenergizeBoundaryLines(Network network,
                                                UcteNetwork ucteNetwork,
                                                NamingStrategy namingStrategy,
                                                Map<UcteElementId, IdentifiableType> deenergizedElements) {
        network.getBoundaryLines(BoundaryLineFilter.UNPAIRED).forEach(boundaryLine -> {
            if (isDeenergized(boundaryLine.getTerminal())) {
                setOutOfOperation(ucteNetwork.getLine(namingStrategy.getUcteElementId(boundaryLine)),
                        boundaryLine.getType(),
                        deenergizedElements);
            }
        });
    }

    /**
     * Tell whether a terminal is connected to a bus without voltage, or to no bus of the bus view.
     *
     * @param terminal the terminal to test
     * @return true if the terminal is connected and has no voltage
     */
    private static boolean isDeenergized(Terminal terminal) {
        return terminal.isConnected() && isDeenergized(terminal.getBusView().getBus());
    }

    /**
     * Tell whether a branch has a de-energized terminal.
     *
     * @param branch the branch to test
     * @return true if one of the terminals of the branch is de-energized
     */
    private static boolean isDeenergized(Branch<?> branch) {
        return isDeenergized(branch.getTerminal1()) || isDeenergized(branch.getTerminal2());
    }

    /**
     * Tell whether a switch of the bus-breaker view is a closed switch without voltage. A closed switch joins two buses
     * that have the same voltage, so the voltage of its first bus is tested. In a bus-breaker voltage level, this is
     * the voltage of the bus view bus that this bus is merged into: a configured bus without voltage that is merged
     * with a configured bus with a voltage is energized.
     *
     * @param voltageLevel the voltage level of the switch
     * @param sw           the switch to test
     * @return true if the switch is closed and has no voltage
     */
    private static boolean isDeenergized(VoltageLevel voltageLevel, Switch sw) {
        if (sw.isOpen()) {
            return false;
        }
        Bus bus1 = voltageLevel.getBusBreakerView().getBus1(sw.getId());
        if (bus1 == null) {
            return true;
        }
        return isDeenergized(voltageLevel.getTopologyKind() == TopologyKind.BUS_BREAKER
                             ? voltageLevel.getBusView().getMergedBus(bus1.getId())
                             : bus1);
    }

    /**
     * Tell whether a bus has no voltage.
     *
     * @param bus the bus to test, possibly null
     * @return true if the bus is null or has no voltage
     */
    private static boolean isDeenergized(Bus bus) {
        return bus == null || Double.isNaN(bus.getV());
    }

    /**
     * Set a UCTE element out of operation if it is in operation, and record it.
     *
     * @param ucteElement         the UCTE element, possibly null if the element is not exported
     * @param elementType         the type of the IIDM element
     * @param deenergizedElements the types of the IIDM elements set out of operation, by UCTE element id
     */
    private static void setOutOfOperation(UcteElement ucteElement,
                                          IdentifiableType elementType,
                                          Map<UcteElementId, IdentifiableType> deenergizedElements) {
        if (ucteElement == null) {
            return;
        }
        UcteElementStatus status = ucteElement.getStatus();
        UcteElementStatus outOfOperationStatus = outOfOperation(status);
        if (outOfOperationStatus != status) {
            ucteElement.setStatus(outOfOperationStatus);
            deenergizedElements.put(ucteElement.getId(), elementType);
        }
    }

    /**
     * Get the out of operation status corresponding to a status.
     *
     * @param status the status of a UCTE element
     * @return the out of operation counterpart of an in operation status, the status itself otherwise
     */
    private static UcteElementStatus outOfOperation(UcteElementStatus status) {
        return switch (status) {
            case REAL_ELEMENT_IN_OPERATION -> UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION;
            case EQUIVALENT_ELEMENT_IN_OPERATION -> UcteElementStatus.EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            case BUSBAR_COUPLER_IN_OPERATION -> UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION;
            default -> status;
        };
    }
}
