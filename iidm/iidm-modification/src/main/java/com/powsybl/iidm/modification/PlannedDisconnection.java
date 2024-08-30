/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;

/**
 * <p>This network modification is used to disconnect a network element from the bus or bus bar section to which it is
 * currently connected. This network modification should be used if the disconnection is planned. If it is not,
 * {@link UnplannedDisconnection} should be used instead.</p>
 * <p>It works on:</p>
 * <ul>
 *     <li>Connectables</li>
 *     <li>HVDC lines by disconnecting their converter stations</li>
 *     <li>Tie lines by disconnecting their underlying dangling lines</li>
 * </ul>
 * <p>The user can specify a side of the element to disconnect. If no side is specified, the network modification will
 * try to disconnect every side.</p>
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PlannedDisconnection extends AbstractDisconnection {

    PlannedDisconnection(String identifiableId, boolean openFictitiousSwitches, ThreeSides side) {
        super(identifiableId, openFictitiousSwitches ?
            SwitchPredicates.IS_OPEN.negate() :
            SwitchPredicates.IS_OPEN.negate().and(SwitchPredicates.IS_NONFICTIONAL), side);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        applyModification(network, true, throwException, reportNode);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(identifiableId);
        if (identifiable == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (identifiable instanceof Connectable<?> connectable && connectable.getTerminals().stream().noneMatch(Terminal::isConnected)
            || identifiable instanceof TieLine tieLine && !tieLine.getTerminal1().isConnected() && !tieLine.getTerminal2().isConnected()
            || identifiable instanceof HvdcLine hvdcLine && !hvdcLine.getConverterStation1().getTerminal().isConnected() && !hvdcLine.getConverterStation2().getTerminal().isConnected()) {
            // TODO: Some cases are not covered since we don't check if the connection can really happen
            impact = NetworkModificationImpact.HAS_IMPACT_ON_NETWORK;
        }
        return impact;
    }
}
