/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractConverter {

    private final ImportContext importContext;

    private final Network network;

    AbstractConverter(ImportContext importContext, Network network) {
        this.importContext = Objects.requireNonNull(importContext);
        this.network = Objects.requireNonNull(network);
    }

    Network getNetwork() {
        return network;
    }

    ImportContext getImportContext() {
        return importContext;
    }

    static double microFaradToSiemens(double frnom, double capacitance) {
        return 2 * Math.PI * frnom * capacitance * 1.0e-6;
    }

    static double microSiemensToSiemens(double susceptance) {
        return susceptance * 1.0e-6;
    }

    static double impedanceFromPerUnitToEngineeringUnits(double impedance, double vnom, double sbase) {
        return impedance * vnom * vnom / sbase;
    }

    static double admittanceFromPerUnitToEngineeringUnits(double admitance, double vnom, double sbase) {
        return admitance * sbase / (vnom * vnom);
    }

    static double impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double impedance, double vnom1, double vnom2, double sbase) {
        return impedance * vnom1 * vnom2 / sbase;
    }

    static double admittanceEnd1ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmissionEu, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return shuntAdmittance * sbase / (vnom1 * vnom1) - (1 - vnom2 / vnom1) * admittanceTransmissionEu;
    }

    static double admittanceEnd2ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmissionEu, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return shuntAdmittance * sbase / (vnom2 * vnom2) - (1 - vnom1 / vnom2) * admittanceTransmissionEu;
    }

    static void createInternalConnection(VoltageLevel vl, int node1, int node2) {
        vl.getNodeBreakerView().newInternalConnection()
            .setNode1(node1)
            .setNode2(node2)
            .add();
    }

    Optional<NodeRef> findNodeFromElmTerm(DataObject elmTerm) {
        return Optional.ofNullable(importContext.elmTermIdToNode.get(elmTerm.getId()));
    }

    NodeRef getNodeFromElmTerm(DataObject elmTerm) {
        if (importContext.elmTermIdToNode.containsKey(elmTerm.getId())) {
            return importContext.elmTermIdToNode.get(elmTerm.getId());
        } else {
            throw new PowerFactoryException("NodeRef not found for elmTerm '" + elmTerm + "'");
        }
    }

    List<NodeRef> findNodes(DataObject obj) {
        List<NodeRef> nodeRefs = importContext.objIdToNode.get(obj.getId());
        return nodeRefs.stream().sorted(Comparator.comparing(nodoref -> nodoref.busIndexIn)).collect(Collectors.toList());
    }

    List<NodeRef> checkNodes(DataObject obj, int connections) {
        List<NodeRef> nodeRefs = findNodes(obj);
        if (nodeRefs == null || nodeRefs.size() != connections) {
            throw new PowerFactoryException("Inconsistent number (" + (nodeRefs != null ? nodeRefs.size() : 0)
                    + ") of connections for '" + obj + "'");
        }
        return nodeRefs;
    }

    static class NodeRef {

        final String voltageLevelId;
        final int node;
        final int busIndexIn;

        NodeRef(String voltageLevelId, int node, int busIndexIn) {
            this.voltageLevelId = voltageLevelId;
            this.node = node;
            this.busIndexIn = busIndexIn;
        }

        @Override
        public String toString() {
            return "NodeRef(voltageLevelId='" + voltageLevelId + '\'' +
                    ", node=" + node +
                    ')';
        }
    }
}
