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

    public static void mergeProperties(BoundaryLine dl1, BoundaryLine dl2, Properties properties) {
        mergeProperties(dl1, dl2, properties, ReportNode.NO_OP);
    }

    public static void mergeProperties(BoundaryLine dl1, BoundaryLine dl2, Properties properties, ReportNode reportNode) {
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
                NetworkReports.propertyOnlyOnOneSide(reportNode, prop, dl2.getProperty(prop), 1, dl1.getId(), dl2.getId());
                properties.setProperty(prop, dl2.getProperty(prop));
            } else if (dl2.getProperty(prop).isEmpty()) {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'", prop, dl1.getProperty(prop));
                NetworkReports.propertyOnlyOnOneSide(reportNode, prop, dl1.getProperty(prop), 2, dl1.getId(), dl2.getId());
                properties.setProperty(prop, dl1.getProperty(prop));
            } else {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line", prop, dl1.getProperty(prop), dl2.getProperty(prop));
                NetworkReports.inconsistentPropertyValues(reportNode, prop, dl1.getProperty(prop), dl2.getProperty(prop), dl1.getId(), dl2.getId());
            }
        });
        dl1Properties.forEach(prop -> properties.setProperty(prop + "_1", dl1.getProperty(prop)));
        dl2Properties.forEach(prop -> properties.setProperty(prop + "_2", dl2.getProperty(prop)));
    }

    public static void mergeIdenticalAliases(BoundaryLine dl1, BoundaryLine dl2, Map<String, String> aliases) {
        mergeIdenticalAliases(dl1, dl2, aliases, ReportNode.NO_OP);
    }

    public static void mergeIdenticalAliases(BoundaryLine dl1, BoundaryLine dl2, Map<String, String> aliases, ReportNode reportNode) {
        for (String alias : dl1.getAliases()) {
            if (dl2.getAliases().contains(alias)) {
                LOGGER.debug("Alias '{}' is found in boundary lines '{}' and '{}'. It is moved to their new tie line.", alias, dl1.getId(), dl2.getId());
                NetworkReports.moveCommonAliases(reportNode, alias, dl1.getId(), dl2.getId());
                String type1 = dl1.getAliasType(alias).orElse("");
                String type2 = dl2.getAliasType(alias).orElse("");
                if (type1.equals(type2)) {
                    aliases.put(alias, type1);
                } else {
                    LOGGER.warn("Inconsistencies found for alias '{}' type in boundary lines '{}' and '{}'. Type is lost.", alias, dl1.getId(), dl2.getId());
                    NetworkReports.inconsistentAliasTypes(reportNode, alias, type1, type2, dl1.getId(), dl2.getId());
                    aliases.put(alias, "");
                }
            }
        }
        aliases.keySet().forEach(alias -> {
            dl1.removeAlias(alias);
            dl2.removeAlias(alias);
        });
    }

    public static void mergeDifferentAliases(BoundaryLine dl1, BoundaryLine dl2, Map<String, String> aliases, ReportNode reportNode) {
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
                    NetworkReports.inconsistentAliasValues(reportNode, alias1, alias, type, dl1.getId(), dl2.getId());
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
     * <b>Analyze a network and return its boundary lines which are candidate to become tie lines when merging the network with another.</b>
     * <b>Is candidate for a pairing key 'k':
     * <li>the only connected boundary line of pairing key 'k', if disconnected boundary lines of pairing key 'k' exist;</li>
     * <li>the only disconnected boundary line of pairing key 'k', if no connected boundary line of pairing key 'k' exists.</li>
     * <li>no boundary line at all</li>
     * </b>
     * <p>Candidates are selected independently for each pairing side: two boundary lines sharing a pairing key but
     * located on different pairing sides are the two halves of a tie line, not concurrent candidates.</p>
     * @param network a network
     * @param logPairingKey a Predicate indicating if we want to log a warning when several boundary lines are found for the same pairing key.
     * @return The list of the boundary lines which are candidate to become tie lines (one or zero per pairing key and per pairing side)
     */
    public static List<BoundaryLine> findCandidateBoundaryLines(Network network, Predicate<String> logPairingKey) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(logPairingKey);

        List<BoundaryLine> candidates = new ArrayList<>();
        Set<String> pairingKeys = new HashSet<>();

        Map<String, Map<PairingSide, List<BoundaryLine>>> connectedByKeyAndSide = new HashMap<>();
        Map<String, Map<PairingSide, List<BoundaryLine>>> disconnectedByKeyAndSide = new HashMap<>();

        network.getBoundaryLines(BoundaryLineFilter.UNPAIRED).forEach(bl -> {
            String pairingKey = bl.getPairingKey();
            Map<String, Map<PairingSide, List<BoundaryLine>>> mapToUpdate = bl.getTerminal().isConnected() ? connectedByKeyAndSide : disconnectedByKeyAndSide;
            mapToUpdate.computeIfAbsent(pairingKey, k -> new HashMap<>())
                    .computeIfAbsent(bl.getPairingSide(), s -> new ArrayList<>())
                    .add(bl);
            pairingKeys.add(pairingKey);
        });

        for (String pairingKey : pairingKeys) {
            boolean doLog = logPairingKey.test(pairingKey);
            Map<PairingSide, List<BoundaryLine>> connectedBySide = connectedByKeyAndSide.getOrDefault(pairingKey, Collections.emptyMap());
            Map<PairingSide, List<BoundaryLine>> disconnectedBySide = disconnectedByKeyAndSide.getOrDefault(pairingKey, Collections.emptyMap());
            Set<PairingSide> sides = new HashSet<>(connectedBySide.keySet());
            sides.addAll(disconnectedBySide.keySet());
            for (PairingSide side : sides) {
                BoundaryLine bl = chooseBoundaryLine(pairingKey,
                        connectedBySide.getOrDefault(side, Collections.emptyList()),
                        disconnectedBySide.getOrDefault(side, Collections.emptyList()),
                        doLog);
                if (bl != null) {
                    candidates.add(bl);
                }
            }
        }
        return candidates;
    }

    /**
     * Choose at most one candidate boundary line among the connected and disconnected boundary lines sharing the same
     * pairing key <b>and</b> the same pairing side, following the rule:
     * <li>exactly one connected: the connected one is selected (even if disconnected siblings exist);</li>
     * <li>no connected and exactly one disconnected: the disconnected one is selected;</li>
     * <li>otherwise (several connected, or no connected and several disconnected): no candidate (ambiguity).</li>
     */
    private static BoundaryLine chooseBoundaryLine(String pairingKey, List<BoundaryLine> connected, List<BoundaryLine> disconnected, boolean doLog) {
        // Exactly one connected wins, even if disconnected siblings exist on the same side
        if (connected.size() == 1) {
            if (!disconnected.isEmpty() && doLog) {
                LOGGER.warn("Several boundary lines {} are candidate for pairing key '{}'. " +
                                "Only '{}' is considered (the only connected one).",
                        Stream.concat(connected.stream(), disconnected.stream()).map(BoundaryLine::getId).toList(),
                        pairingKey, connected.getFirst().getId());
            }
            return connected.getFirst();
        }
        // No connected, exactly one disconnected
        if (connected.isEmpty() && disconnected.size() == 1) {
            return disconnected.getFirst();
        }
        // Real ambiguity on this pairing side: several connected, or no connected and several disconnected
        if (doLog && !(connected.isEmpty() && disconnected.isEmpty())) {
            String status = connected.size() > 1 ? "connected" : "disconnected";
            List<String> ambiguousIds = (connected.size() > 1 ? connected : disconnected).stream().map(BoundaryLine::getId).toList();
            LOGGER.warn("Several {} boundary lines {} are candidate for pairing key '{}'. " + NO_TIE_LINE_MESSAGE,
                    status, ambiguousIds, pairingKey);
        }
        return null;
    }

    /**
     * If it exists, find the boundary line in the merging network that should be associated to a candidate boundary line in the network to be merged.
     * Two boundary lines in different IGM should be associated if:
     * - they have the same non-null pairing key and are the only boundary lines to have this pairing key in their respective networks
     * OR
     * - they have the same non-null pairing key and are the only connected boundary lines to have this pairing key in their respective networks
     *
     * @param candidateBoundaryLine candidate boundary line in the network to be merged
     * @param getBoundaryLinesByPairingKey function to retrieve boundary lines with a given pairing key in the merging network.
     * @param associateBoundaryLines function associating two boundary lines
     */
    public static void findAndAssociateBoundaryLines(BoundaryLine candidateBoundaryLine, Function<String, List<BoundaryLine>> getBoundaryLinesByPairingKey,
                                                     BiConsumer<BoundaryLine, BoundaryLine> associateBoundaryLines) {
        //TODO This method is quite complicated. It would be better to call `findCandidateBoundaryLines` on both networks to merge
        // and to only process the retrieved candidate lists together.
        Objects.requireNonNull(candidateBoundaryLine);
        Objects.requireNonNull(getBoundaryLinesByPairingKey);
        Objects.requireNonNull(associateBoundaryLines);
        // mapping by pairing key
        if (candidateBoundaryLine.getPairingKey() != null) { // if pairing key null: no associated boundary line
            PairingSide candidateSide = candidateBoundaryLine.getPairingSide();
            // If we call this method on the results of "findCandidateBoundaryLines", the following test is useless.
            if (candidateBoundaryLine.getNetwork().getBoundaryLineStream(BoundaryLineFilter.UNPAIRED)
                    .filter(b -> b != candidateBoundaryLine)
                    .filter(b -> candidateBoundaryLine.getPairingKey().equals(b.getPairingKey()))
                    .filter(b -> Objects.equals(candidateSide, b.getPairingSide()))
                    .anyMatch(b -> b.getTerminal().isConnected())) { // check that there is no connected boundary line with same pairing key and side in the network to be merged
                return;                                         // in that case, do nothing
            }
            List<BoundaryLine> bls = getBoundaryLinesByPairingKey.apply(candidateBoundaryLine.getPairingKey());
            if (bls != null) {
                List<BoundaryLine> compatibleBls = bls.stream()
                        .filter(b -> arePairingSidesCompatible(candidateSide, b.getPairingSide()))
                        .toList();
                if (compatibleBls.size() == 1) { // if there is exactly one boundary line in the merging network, merge it
                    associateBoundaryLines.accept(compatibleBls.getFirst(), candidateBoundaryLine);
                }
                if (compatibleBls.size() > 1) { // if more than one boundary line in the merging network, check how many are connected
                    associateConnectedBoundaryLine(candidateBoundaryLine, compatibleBls, associateBoundaryLines);
                }
            }
        }

    }

    private static boolean arePairingSidesCompatible(PairingSide side1, PairingSide side2) {
        return side2 == null || side1 != side2;
    }

    private static void associateConnectedBoundaryLine(BoundaryLine candidateBoundaryLine, List<BoundaryLine> bls,
                                                       BiConsumer<BoundaryLine, BoundaryLine> associateBoundaryLines) {
        // Connected BoundaryLines
        List<BoundaryLine> connectedBls = bls.stream().filter(bl -> bl.getTerminal().isConnected()).toList();

        // If there is exactly one connected boundary line in the merging network, merge it. Otherwise, do nothing
        if (connectedBls.size() == 1) {
            LOGGER.warn("Several boundary lines {} of the same subnetwork are candidate for merging for pairing key '{}'. " +
                    "Tie line automatically created using the only connected one '{}'.",
                bls.stream().map(BoundaryLine::getId).toList(), connectedBls.getFirst().getPairingKey(),
                connectedBls.getFirst().getId());
            associateBoundaryLines.accept(connectedBls.getFirst(), candidateBoundaryLine);
        } else {
            String status = connectedBls.size() > 1 ? "connected" : "disconnected";
            LOGGER.warn("Several {} boundary lines {} of the same subnetwork are candidate for merging for pairing key '{}'. " + NO_TIE_LINE_MESSAGE,
                status, connectedBls.stream().map(BoundaryLine::getId).toList(),
                connectedBls.getFirst().getPairingKey());
        }
    }

    public static double getR(BoundaryLine dl1, BoundaryLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        // Add 0.0 to avoid negative zero, tests where the R value is compared as text, fail
        return zeroImpedanceLine(adm) ? 0.0 : adm.y12().negate().reciprocal().getReal() + 0.0;
    }

    public static double getX(BoundaryLine dl1, BoundaryLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        // Add 0.0 to avoid negative zero, tests where the X value is compared as text, fail
        return zeroImpedanceLine(adm) ? 0.0 : adm.y12().negate().reciprocal().getImaginary() + 0.0;
    }

    public static double getG1(BoundaryLine dl1, BoundaryLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y11().add(adm.y12()).getReal();
    }

    public static double getB1(BoundaryLine dl1, BoundaryLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y11().add(adm.y12()).getImaginary();
    }

    public static double getG2(BoundaryLine dl1, BoundaryLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y22().add(adm.y21()).getReal();
    }

    public static double getB2(BoundaryLine dl1, BoundaryLine dl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(dl1, dl2);
        return adm.y22().add(adm.y21()).getImaginary();
    }

    public static double getBoundaryV(BoundaryLine dl1, BoundaryLine dl2) {
        Complex boundaryV = voltageAtTheBoundaryNode(dl1, dl2);
        return boundaryV.abs();
    }

    public static double getBoundaryAngle(BoundaryLine dl1, BoundaryLine dl2) {
        Complex boundaryV = voltageAtTheBoundaryNode(dl1, dl2);
        return Math.toDegrees(Math.atan2(boundaryV.getImaginary(), boundaryV.getReal()));
    }

    private static LinkData.BranchAdmittanceMatrix equivalentBranchAdmittanceMatrix(BoundaryLine dl1,
                                                                                    BoundaryLine dl2) {
        // zero impedance boundary lines should be supported

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

    private static Complex voltageAtTheBoundaryNode(BoundaryLine dl1, BoundaryLine dl2) {

        Complex v1 = ComplexUtils.polar2Complex(BoundaryLineData.getV(dl1), BoundaryLineData.getTheta(dl1));
        Complex v2 = ComplexUtils.polar2Complex(BoundaryLineData.getV(dl2), BoundaryLineData.getTheta(dl2));

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
     * <p>Retrieve, if it exists, the boundary line paired to the given one.</p>
     *
     * @param boundaryLine a boundary line
     * @return an Optional containing the boundary line paired to the given one
     */
    public static Optional<BoundaryLine> getPairedBoundaryLine(BoundaryLine boundaryLine) {
        return boundaryLine.getTieLine().map(t ->
                t.getBoundaryLine1() == boundaryLine ? t.getBoundaryLine2() : t.getBoundaryLine1());
    }

    /**
     * <p>Find the boundary line that should be automatically paired with the given one to create a tie line, if any.
     * <p>A boundary line to pair with is returned only if:</p>
     * <ul>
     *     <li>the given boundary line has a non-null pairing key and pairing side and is not already paired;</li>
     *     <li>the given boundary line is the selected candidate of its own pairing side (same selection rule as the
     *     merge: the only connected one, otherwise the only disconnected one);</li>
     *     <li>there is exactly one selected candidate on the opposite pairing side.</li>
     * </ul>
     *
     * @param boundaryLine the boundary line to pair
     * @return an Optional containing the boundary line to pair with, or an empty Optional if no unambiguous pairing exists
     */
    public static Optional<BoundaryLine> findBoundaryLineToPair(BoundaryLine boundaryLine) {
        Objects.requireNonNull(boundaryLine);
        String pairingKey = boundaryLine.getPairingKey();
        PairingSide side = boundaryLine.getPairingSide();
        if (pairingKey == null || side == null || boundaryLine.isPaired()) {
            return Optional.empty();
        }
        Map<PairingSide, BoundaryLine> candidateBySide = findCandidateBoundaryLines(boundaryLine.getNetwork(), k -> false).stream()
                .filter(bl -> pairingKey.equals(bl.getPairingKey()) && bl.getPairingSide() != null)
                .collect(Collectors.toMap(BoundaryLine::getPairingSide, Function.identity()));
        // the given boundary line must be the selected candidate of its own side
        if (candidateBySide.get(side) != boundaryLine) {
            return Optional.empty();
        }
        return Optional.ofNullable(candidateBySide.get(side.getOppositeSide()));
    }
}
