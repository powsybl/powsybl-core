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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
        return nodeRefs != null ? nodeRefs.stream().sorted(Comparator.comparing(nodoref -> nodoref.busIndexIn)).collect(Collectors.toList()) : Collections.emptyList();
    }

    List<NodeRef> checkNodes(DataObject obj, int connections) {
        List<NodeRef> nodeRefs = findNodes(obj);
        if (nodeRefs == null || nodeRefs.size() != connections) {
            throw new PowerFactoryException("Inconsistent number (" + (nodeRefs != null ? nodeRefs.size() : 0)
                    + ") of connections for '" + obj.getId() + " " + obj + "'");
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

    static Double float2Double(Float f) {
        return f != null ? f.doubleValue() : null;
    }

    private enum Mode { PQ, PS, QS, PC, QC, SC, NAN }

    static PQ calculate(String modeInp, Double p, Double q, Double s, Double cosPhi) {
        return switch (detectMode(modeInp, p, q, s, cosPhi)) {
            case PQ -> new PQ(p, q);
            case PS -> calculatePQFromPandS(p, s);
            case QS -> calculatePQFromQandS(q, s);
            case PC -> calculatePQFromPandPowerFactor(p, cosPhi);
            case QC -> calculatePQFromQandPowerFactor(q, cosPhi);
            case SC -> calculatePQFromSandPowerFactor(s, cosPhi);
            case NAN -> new PQ(Double.NaN, Double.NaN);
        };
    }

    private static Mode detectMode(String modeInp, Double p, Double q, Double s, Double cosPhi) {

        if (modeInp != null) {
            Mode mode = switch (modeInp) {
                case "PQ" -> checkMode(p, q, Mode.PQ);
                case "SP" -> checkMode(p, s, Mode.PS);
                case "SQ" -> checkMode(q, s, Mode.QS);
                case "PC" -> checkMode(p, cosPhi, Mode.PC);
                case "QC" -> checkMode(q, cosPhi, Mode.QC);
                case "SC" -> checkMode(s, cosPhi, Mode.SC);
                case "EC", "DEF" -> {
                    LOGGER.warn("mode_inp {} not supported ", modeInp);
                    yield Mode.NAN;
                }
                default -> throw new PowerFactoryException(String.format("Unexpected mode_inp %s", modeInp));
            };
            if (mode != Mode.NAN) {
                return mode;
            }
        }

        if (p != null && q != null) {
            return Mode.PQ;
        }
        if (p != null && s != null) {
            return Mode.PS;
        }
        if (q != null && s != null) {
            return Mode.QS;
        }
        if (p != null && cosPhi != null) {
            return Mode.PC;
        }
        if (q != null && cosPhi != null) {
            return Mode.QC;
        }
        if (s != null && cosPhi != null) {
            return Mode.SC;
        }

        return Mode.NAN;
    }

    private static Mode checkMode(Double value1, Double value2, Mode mode) {
        return value1 != null && value2 != null ? mode : Mode.NAN;
    }

    private static PQ calculatePQFromPandS(double p, double s) {
        double q2 = s * s - p * p;
        double q;
        if (q2 >= 0) {
            q = Math.sqrt(q2);
        } else {
            throw new PowerFactoryException(String.format("Unexpected apparent power Mva %.2f Mw %.2f", s, p));
        }

        return new PQ(p, q);
    }

    private static PQ calculatePQFromQandS(double q, double s) {
        double p2 = s * s - q * q;
        double p;
        if (p2 >= 0) {
            p = Math.sqrt(p2);
        } else {
            throw new PowerFactoryException(String.format("Unexpected apparent power Mva %.2f Mvar %.2f", s, q));
        }

        return new PQ(p, q);
    }

    private static PQ calculatePQFromPandPowerFactor(double p, double powerFactor) {
        double disc = 1.0 - powerFactor * powerFactor;
        double q;
        if (disc >= 0 && powerFactor != 0.0) {
            q = p * Math.sqrt(disc) / powerFactor;
        } else {
            throw new PowerFactoryException(String.format("Unexpected powerFactor %.2f Mw %.2f", powerFactor, p));
        }
        return new PQ(p, q);
    }

    private static PQ calculatePQFromQandPowerFactor(double q, double powerFactor) {
        double disc = 1.0 - powerFactor * powerFactor;
        double p;
        if (disc > 0) {
            p = q * powerFactor / Math.sqrt(disc);
        } else {
            throw new PowerFactoryException(String.format("Unexpected powerFactor %.2f Mvar %.2f", powerFactor, q));
        }
        return new PQ(p, q);
    }

    private static PQ calculatePQFromSandPowerFactor(double s, double powerFactor) {
        double disc = 1.0 - powerFactor * powerFactor;
        double p = s * powerFactor;
        double q;
        if (disc >= 0) {
            q = s * Math.sqrt(disc);
        } else {
            throw new PowerFactoryException(String.format("Unexpected powerFactor %.2f Mva %.2f", powerFactor, s));
        }
        return new PQ(p, q);
    }

    static PQ calculatePQSign(DataObject elmObject, String pSignAttribute, String qSignAttribute) {

        Optional<Float> pgini = elmObject.findFloatAttributeValue(pSignAttribute);
        Optional<Float> qgini = elmObject.findFloatAttributeValue(qSignAttribute);

        double signP = 1;
        if (pgini.isEmpty() && qgini.isPresent()) {
            signP = Math.signum(qgini.get());
        }
        double signQ = 1;
        if (qgini.isEmpty() && pgini.isPresent()) {
            signP = Math.signum(pgini.get());
        }

        return new PQ(signP, signQ);
    }

    static class PQ {
        final double p;
        final double q;

        PQ(double p, double q) {
            this.p = p;
            this.q = q;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverter.class);
}
