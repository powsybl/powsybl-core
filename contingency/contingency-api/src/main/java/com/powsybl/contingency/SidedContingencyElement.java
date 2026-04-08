/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface SidedContingencyElement extends ContingencyElement {

    Logger LOGGER = LoggerFactory.getLogger(SidedContingencyElement.class);

    String getVoltageLevelId();

    static TwoSides getContingencySide(Network network, SidedContingencyElement element) {
        String voltageLevelId = element.getVoltageLevelId();
        if (voltageLevelId != null) {
            Function<TwoSides, Terminal> terminalSupplier = getTerminalSupplier(network, element);
            if (terminalSupplier != null) {
                if (voltageLevelId.equals(terminalSupplier.apply(TwoSides.ONE).getVoltageLevel().getId())) {
                    return TwoSides.ONE;
                } else if (voltageLevelId.equals(terminalSupplier.apply(TwoSides.TWO).getVoltageLevel().getId())) {
                    return TwoSides.TWO;
                } else {
                    LOGGER.warn("Voltage id '{}' of contingency '{}' not found", voltageLevelId, element.getId());
                }
            } else {
                LOGGER.warn("Id of contingency '{}' not found", element.getId());
            }
        }
        return null;
    }

    private static Function<TwoSides, Terminal> getTerminalSupplier(Network network, SidedContingencyElement element) {
        return switch (element.getType()) {
            case BRANCH -> getBranchTerminalSupplier(network, element.getId());
            case HVDC_LINE -> getHvdcLineTerminalSupplier(network, element.getId());
            case LINE -> getLineTerminalSupplier(network, element.getId());
            case TIE_LINE -> getTieLineTerminalSupplier(network, element.getId());
            case TWO_WINDINGS_TRANSFORMER -> getTransformerTerminalSupplier(network, element.getId());
            default -> null;
        };
    }

    private static Function<TwoSides, Terminal> getBranchTerminalSupplier(Network network, String id) {
        Branch<?> eq = network.getBranch(id);
        return eq != null ? eq::getTerminal : null;
    }

    private static Function<TwoSides, Terminal> getLineTerminalSupplier(Network network, String id) {
        Line eq = network.getLine(id);
        return eq != null ? eq::getTerminal : null;
    }

    private static Function<TwoSides, Terminal> getTieLineTerminalSupplier(Network network, String id) {
        TieLine eq = network.getTieLine(id);
        return eq != null ? eq::getTerminal : null;
    }

    private static Function<TwoSides, Terminal> getTransformerTerminalSupplier(Network network, String id) {
        TwoWindingsTransformer eq = network.getTwoWindingsTransformer(id);
        return eq != null ? eq::getTerminal : null;
    }

    private static Function<TwoSides, Terminal> getHvdcLineTerminalSupplier(Network network, String id) {
        HvdcLine eq = network.getHvdcLine(id);
        return eq != null ? s -> eq.getConverterStation(s).getTerminal() : null;
    }
}
