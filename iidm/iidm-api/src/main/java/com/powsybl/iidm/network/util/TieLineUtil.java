/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Sets;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.iidm.network.util.TieLineReports.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public final class TieLineUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TieLineUtil.class);

    public static final String NO_TIE_LINE_MESSAGE = "No tie line automatically created, tie lines must be created by hand.";

    private TieLineUtil() {
    }

    public static String buildMergedId(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + " + " + id2;
        }
        if (id1.compareTo(id2) > 0) {
            return id2 + " + " + id1;
        }
        return id1;
    }

    public static String buildMergedName(String id1, String id2, String name1, String name2) {
        if (name1 == null) {
            return name2;
        }
        if (name2 == null) {
            return name1;
        }
        if (name1.compareTo(name2) == 0) {
            return name1;
        }
        if (id1.compareTo(id2) < 0) {
            return name1 + " + " + name2;
        }
        if (id1.compareTo(id2) > 0) {
            return name2 + " + " + name1;
        }
        if (name1.compareTo(name2) < 0) {
            return name1 + " + " + name2;
        }
        return name2 + " + " + name1;
    }

    public static void mergeProperties(DanglingLine dl1, DanglingLine dl2, Properties properties) {
        mergeProperties(dl1, dl2, properties, ReportNode.NO_OP);
    }

    public static void mergeProperties(DanglingLine dl1, DanglingLine dl2, Properties properties, ReportNode reportNode) {
        Set<String> dl1Properties = dl1.getPropertyNames();
        Set<String> dl2Properties = dl2.getPropertyNames();
        Set<String> commonProperties = Sets.intersection(dl1Properties, dl2Properties);
        Sets.difference(dl1Properties, commonProperties).forEach(prop -> properties.setProperty(prop, dl1.getProperty(prop)));
        Sets.difference(dl2Properties, commonProperties).forEach(prop -> properties.setProperty(prop, dl2.getProperty(prop)));
        commonProperties.forEach(prop -> {
            if (dl1.getProperty(prop).equals(dl2.getProperty(prop))) {
                properties.setProperty(prop, dl1.getProperty(prop));
            } else if (dl1.getProperty(prop).isEmpty()) {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 1 is empty, keeping side 2 value '{}'", prop, dl2.getProperty(prop));
                propertyOnlyOnOneSide(reportNode, prop, dl2.getProperty(prop), 1, dl1.getId(), dl2.getId());
                properties.setProperty(prop, dl2.getProperty(prop));
            } else if (dl2.getProperty(prop).isEmpty()) {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'", prop, dl1.getProperty(prop));
                propertyOnlyOnOneSide(reportNode, prop, dl1.getProperty(prop), 2, dl1.getId(), dl2.getId());
                properties.setProperty(prop, dl1.getProperty(prop));
            } else {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line", prop, dl1.getProperty(prop), dl2.getProperty(prop));
                inconsistentPropertyValues(reportNode, prop, dl1.getProperty(prop), dl2.getProperty(prop), dl1.getId(), dl2.getId());
            }
        });
        dl1Properties.forEach(prop -> properties.setProperty(prop + "_1", dl1.getProperty(prop)));
        dl2Properties.forEach(prop -> properties.setProperty(prop + "_2", dl2.getProperty(prop)));
    }

    public static void mergeIdenticalAliases(DanglingLine dl1, DanglingLine dl2, Map<String, String> aliases) {
        mergeIdenticalAliases(dl1, dl2, aliases, ReportNode.NO_OP);
    }

    public static void mergeIdenticalAliases(DanglingLine dl1, DanglingLine dl2, Map<String, String> aliases, ReportNode reportNode) {
        for (String alias : dl1.getAliases()) {
            if (dl2.getAliases().contains(alias)) {
                LOGGER.debug("Alias '{}' is found in dangling lines '{}' and '{}'. It is moved to their new tie line.", alias, dl1.getId(), dl2.getId());
                moveCommonAliases(reportNode, alias, dl1.getId(), dl2.getId());
                String type1 = dl1.getAliasType(alias).orElse("");
                String type2 = dl2.getAliasType(alias).orElse("");
                if (type1.equals(type2)) {
                    aliases.put(alias, type1);
                } else {
                    LOGGER.warn("Inconsistencies found for alias '{}' type in dangling lines '{}' and '{}'. Type is lost.", alias, dl1.getId(), dl2.getId());
                    inconsistentAliasTypes(reportNode, alias, type1, type2, dl1.getId(), dl2.getId());
                    aliases.put(alias, "");
                }
            }
        }
        aliases.keySet().forEach(alias -> {
            dl1.removeAlias(alias);
            dl2.removeAlias(alias);
        });
    }

    public static void mergeDifferentAliases(DanglingLine dl1, DanglingLine dl2, Map<String, String> aliases, ReportNode reportNode) {
        for (String alias : dl1.getAliases()) {
            if (!dl2.getAliases().contains(alias)) {
                aliases.put(alias, dl1.getAliasType(alias).orElse(""));
            }
        }
        for (String alias : dl2.getAliases()) {
            if (!dl1.getAliases().contains(alias)) {
                String type = dl2.getAliasType(alias).orElse("");
                if (!type.isEmpty() && aliases.containsValue(type)) {
                    String tmpType = type;
                    String alias1 = aliases.entrySet().stream().filter(e -> tmpType.equals(e.getValue())).map(Map.Entry::getKey).findFirst().orElseThrow(IllegalStateException::new);
                    aliases.put(alias1, type + "_1");
                    LOGGER.warn("Inconsistencies found for alias type '{}'('{}' for '{}' and '{}' for '{}'). " +
                            "Types are respectively renamed as '{}_1' and '{}_2'.", type, alias1, dl1.getId(), alias, dl2.getId(), type, type);
                    inconsistentAliasValues(reportNode, alias1, alias, type, dl1.getId(), dl2.getId());
                    type += "_2";
                }
                aliases.put(alias, type);
            }
        }
        aliases.keySet().forEach(alias -> {
            if (dl1.getAliases().contains(alias)) {
                dl1.removeAlias(alias);
            }
            if (dl2.getAliases().contains(alias)) {
                dl2.removeAlias(alias);
            }
        });
    }

    /**
     * <b>Analyze a network and return its dangling lines which are candidate to become tie lines when merging the network with another.</b>
     * <b>Is candidate for a pairing key 'k':
     * <li>the only connected dangling line of pairing key 'k', if disconnected dangling lines of pairing key 'k' exist;</li>
     * <li>the only disconnected dangling line of pairing key 'k', if no connected dangling line of pairing key 'k' exists.</li>
     * <li>no dangling line at all</li>
     * </b>
     * @param network a network
     * @param logPairingKey a Predicate indicating if we want to log a warning when several dangling lines are found for the same pairing key.
     * @return The list of the dangling lines which are candidate to become tie lines (one or zero by pairing key)
     */
    public static List<DanglingLine> findCandidateDanglingLines(Network network, Predicate<String> logPairingKey) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(logPairingKey);

        List<DanglingLine> candidates = new ArrayList<>();
        Set<String> pairingKeys = new HashSet<>();
        Map<String, List<DanglingLine>> connectedByPairingKey = new HashMap<>();
        Map<String, List<DanglingLine>> disconnectedByPairingKey = new HashMap<>();

        network.getDanglingLines(DanglingLineFilter.UNPAIRED).forEach(dl -> {
            String pairingKey = dl.getPairingKey();
            Map<String, List<DanglingLine>> mapToUpdate = dl.getTerminal().isConnected() ? connectedByPairingKey : disconnectedByPairingKey;
            mapToUpdate.computeIfAbsent(pairingKey, k -> new ArrayList<>()).add(dl);
            pairingKeys.add(pairingKey);
        });

        for (String pairingKey : pairingKeys) {
            boolean doLog = logPairingKey.test(pairingKey);
            List<DanglingLine> connected = Optional.ofNullable(connectedByPairingKey.get(pairingKey)).orElse(Collections.emptyList());
            List<DanglingLine> disconnected = Optional.ofNullable(disconnectedByPairingKey.get(pairingKey)).orElse(Collections.emptyList());
            if (connected.isEmpty()) {
                DanglingLine dl = disconnected.get(0); // Cannot be empty here: we always have at least 1 connected or disconnected dangling line
                if (disconnected.size() == 1) {
                    candidates.add(dl);
                } else if (doLog) {
                    LOGGER.warn("Several disconnected dangling lines {} (and no connected one) of the same subnetwork are candidate for merging for pairing key '{}'. " + NO_TIE_LINE_MESSAGE,
                            disconnected.stream().map(DanglingLine::getId).toList(), pairingKey);
                }
            } else if (connected.size() == 1) {
                DanglingLine dl = connected.get(0);
                candidates.add(dl);
                if (!disconnected.isEmpty() && doLog) {
                    LOGGER.warn("Several dangling lines {} of the same subnetwork are candidate for merging for pairing key '{}'. " +
                                    "Only '{}' is considered (the only connected one)",
                            Stream.concat(Stream.of(dl.getId()), disconnected.stream().map(DanglingLine::getId)).collect(Collectors.toList()),
                            pairingKey, dl.getId());
                }
            } else if (doLog) {
                LOGGER.warn("Several connected dangling lines {} of the same subnetwork are candidate for merging for pairing key '{}'. " + NO_TIE_LINE_MESSAGE,
                        connected.stream().map(DanglingLine::getId).toList(), pairingKey);
            }
        }
        return candidates;
    }

    /**
     * If it exists, find the dangling line in the merging network that should be associated to a candidate dangling line in the network to be merged.
     * Two dangling lines in different IGM should be associated if:
     * - they have the same non-null pairing key and are the only dangling lines to have this pairing key in their respective networks
     * OR
     * - they have the same non-null pairing key and are the only connected dangling lines to have this pairing key in their respective networks
     *
     * @param candidateDanglingLine candidate dangling line in the network to be merged
     * @param getDanglingLinesByPairingKey function to retrieve dangling lines with a given pairing key in the merging network.
     * @param associateDanglingLines function associating two dangling lines
     */
    public static void findAndAssociateDanglingLines(DanglingLine candidateDanglingLine, Function<String, List<DanglingLine>> getDanglingLinesByPairingKey,
                                                     BiConsumer<DanglingLine, DanglingLine> associateDanglingLines) {
        //TODO This method is quite complicated. It would be better to call `findCandidateDanglingLines` on both networks to merge
        // and to only process the retrieved candidate lists together.
        Objects.requireNonNull(candidateDanglingLine);
        Objects.requireNonNull(getDanglingLinesByPairingKey);
        Objects.requireNonNull(associateDanglingLines);
        // mapping by pairing key
        if (candidateDanglingLine.getPairingKey() != null) { // if pairing key null: no associated dangling line
            // If we call this method on the results of "findCandidateDanglingLines", the following test is useless
            if (candidateDanglingLine.getNetwork().getDanglingLineStream(DanglingLineFilter.UNPAIRED)
                    .filter(d -> d != candidateDanglingLine)
                    .filter(d -> candidateDanglingLine.getPairingKey().equals(d.getPairingKey()))
                    .anyMatch(d -> d.getTerminal().isConnected())) { // check that there is no connected dangling line with same pairing key in the network to be merged
                return;                                         // in that case, do nothing
            }
            List<DanglingLine> dls = getDanglingLinesByPairingKey.apply(candidateDanglingLine.getPairingKey());
            if (dls != null) {
                if (dls.size() == 1) { // if there is exactly one dangling line in the merging network, merge it
                    associateDanglingLines.accept(dls.get(0), candidateDanglingLine);
                }
                if (dls.size() > 1) { // if more than one dangling line in the merging network, check how many are connected
                    List<DanglingLine> connectedDls = dls.stream().filter(dl -> dl.getTerminal().isConnected()).collect(Collectors.toList());
                    if (connectedDls.size() == 1) { // if there is exactly one connected dangling line in the merging network, merge it. Otherwise, do nothing
                        LOGGER.warn("Several dangling lines {} of the same subnetwork are candidate for merging for pairing key '{}'. " +
                                        "Tie line automatically created using the only connected one '{}'.",
                                dls.stream().map(DanglingLine::getId).collect(Collectors.toList()), connectedDls.get(0).getPairingKey(),
                                connectedDls.get(0).getId());
                        associateDanglingLines.accept(connectedDls.get(0), candidateDanglingLine);
                    } else {
                        String status = connectedDls.size() > 1 ? "connected" : "disconnected";
                        LOGGER.warn("Several {} dangling lines {} of the same subnetwork are candidate for merging for pairing key '{}'. " + NO_TIE_LINE_MESSAGE,
                                status, connectedDls.stream().map(DanglingLine::getId).collect(Collectors.toList()),
                                connectedDls.get(0).getPairingKey());
                    }
                }
            }
        }

    }

    public static double getR(DanglingLine dl1, DanglingLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        // Add 0.0 to avoid negative zero, tests where the R value is compared as text, fail
        return zeroImpedanceLine(adm) ? 0.0 : adm.y12().negate().reciprocal().getReal() + 0.0;
    }

    public static double getX(DanglingLine dl1, DanglingLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        // Add 0.0 to avoid negative zero, tests where the X value is compared as text, fail
        return zeroImpedanceLine(adm) ? 0.0 : adm.y12().negate().reciprocal().getImaginary() + 0.0;
    }

    public static double getG1(DanglingLine dl1, DanglingLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y11().add(adm.y12()).getReal();
    }

    public static double getB1(DanglingLine dl1, DanglingLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y11().add(adm.y12()).getImaginary();
    }

    public static double getG2(DanglingLine dl1, DanglingLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y22().add(adm.y21()).getReal();
    }

    public static double getB2(DanglingLine dl1, DanglingLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y22().add(adm.y21()).getImaginary();
    }

    public static double getBoundaryV(DanglingLine dl1, DanglingLine dl2) {
        Complex boundaryV = voltageAtTheBoundaryNode(dl1, dl2);
        return boundaryV.abs();
    }

    public static double getBoundaryAngle(DanglingLine dl1, DanglingLine dl2) {
        Complex boundaryV = voltageAtTheBoundaryNode(dl1, dl2);
        return Math.toDegrees(Math.atan2(boundaryV.getImaginary(), boundaryV.getReal()));
    }

    private static LinkData.BranchAdmittanceMatrix equivalentBranchAdmittanceMatrix(DanglingLine dl1,
        DanglingLine dl2) {
        // zero impedance dangling lines should be supported

        BranchAdmittanceMatrix adm1 = LinkData.calculateBranchAdmittance(dl1.getR(), dl1.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(dl1.getG(), dl1.getB()), new Complex(0.0, 0.0));
        BranchAdmittanceMatrix adm2 = LinkData.calculateBranchAdmittance(dl2.getR(), dl2.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(0.0, 0.0), new Complex(dl2.getG(), dl2.getB()));

        if (zeroImpedanceLine(adm1) && zeroImpedanceLine(adm2)) {
            return adm1;
        } else if (zeroImpedanceLine(adm1)) {
            return adm2;
        } else if (zeroImpedanceLine(adm2)) {
            return adm1;
        } else {
            return LinkData.kronChain(adm1, TwoSides.TWO, adm2, TwoSides.ONE);
        }
    }

    private static boolean zeroImpedanceLine(BranchAdmittanceMatrix adm) {
        if (adm.y12().getReal() == 0.0 && adm.y12().getImaginary() == 0.0) {
            return true;
        } else {
            return adm.y21().getReal() == 0.0 && adm.y22().getImaginary() == 0.0;
        }
    }

    private static Complex voltageAtTheBoundaryNode(DanglingLine dl1, DanglingLine dl2) {

        Complex v1 = ComplexUtils.polar2Complex(DanglingLineData.getV(dl1), DanglingLineData.getTheta(dl1));
        Complex v2 = ComplexUtils.polar2Complex(DanglingLineData.getV(dl2), DanglingLineData.getTheta(dl2));

        BranchAdmittanceMatrix adm1 = LinkData.calculateBranchAdmittance(dl1.getR(), dl1.getX(), 1.0, 0.0, 1.0, 0.0,
                new Complex(dl1.getG(), dl1.getB()), new Complex(0.0, 0.0));
        BranchAdmittanceMatrix adm2 = LinkData.calculateBranchAdmittance(dl2.getR(), dl2.getX(), 1.0, 0.0, 1.0, 0.0,
                new Complex(0.0, 0.0), new Complex(dl2.getG(), dl2.getB()));

        if (zeroImpedanceLine(adm1) && zeroImpedanceLine(adm2)) {
            return v1;
        } else if (zeroImpedanceLine(adm1)) {
            return v1;
        } else if (zeroImpedanceLine(adm2)) {
            return v2;
        } else {
            return adm1.y21().multiply(v1).add(adm2.y12().multiply(v2)).negate().divide(adm1.y22().add(adm2.y11()));
        }
    }

    /**
     * <p>Retrieve, if it exists, the dangling line paired to the given one.</p>
     *
     * @param danglingLine a dangling line
     * @return an Optional containing the dangling line paired to the given one
     */
    public static Optional<DanglingLine> getPairedDanglingLine(DanglingLine danglingLine) {
        return danglingLine.getTieLine().map(t ->
                t.getDanglingLine1() == danglingLine ? t.getDanglingLine2() : t.getDanglingLine1());
    }
}
