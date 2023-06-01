/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.BoundaryLineFilter;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.util.TieLineReports.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

public final class TieLineUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TieLineUtil.class);

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

    public static void mergeProperties(BoundaryLine bl1, BoundaryLine bl2, Properties properties) {
        mergeProperties(bl1, bl2, properties, Reporter.NO_OP);
    }

    public static void mergeProperties(BoundaryLine bl1, BoundaryLine bl2, Properties properties, Reporter reporter) {
        Set<String> bl1Properties = bl1.getPropertyNames();
        Set<String> bl2Properties = bl2.getPropertyNames();
        Set<String> commonProperties = Sets.intersection(bl1Properties, bl2Properties);
        Sets.difference(bl1Properties, commonProperties).forEach(prop -> properties.setProperty(prop, bl1.getProperty(prop)));
        Sets.difference(bl2Properties, commonProperties).forEach(prop -> properties.setProperty(prop, bl2.getProperty(prop)));
        commonProperties.forEach(prop -> {
            if (bl1.getProperty(prop).equals(bl2.getProperty(prop))) {
                properties.setProperty(prop, bl1.getProperty(prop));
            } else if (bl1.getProperty(prop).isEmpty()) {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 1 is empty, keeping side 2 value '{}'", prop, bl2.getProperty(prop));
                propertyOnlyOnOneSide(reporter, prop, bl2.getProperty(prop), 1, bl1.getId(), bl2.getId());
                properties.setProperty(prop, bl2.getProperty(prop));
            } else if (bl2.getProperty(prop).isEmpty()) {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'", prop, bl1.getProperty(prop));
                propertyOnlyOnOneSide(reporter, prop, bl1.getProperty(prop), 2, bl1.getId(), bl2.getId());
                properties.setProperty(prop, bl1.getProperty(prop));
            } else {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line", prop, bl1.getProperty(prop), bl2.getProperty(prop));
                inconsistentPropertyValues(reporter, prop, bl1.getProperty(prop), bl2.getProperty(prop), bl1.getId(), bl2.getId());
            }
        });
        bl1Properties.forEach(prop -> properties.setProperty(prop + "_1", bl1.getProperty(prop)));
        bl2Properties.forEach(prop -> properties.setProperty(prop + "_2", bl2.getProperty(prop)));
    }

    public static void mergeIdenticalAliases(BoundaryLine bl1, BoundaryLine bl2, Map<String, String> aliases) {
        mergeIdenticalAliases(bl1, bl2, aliases, Reporter.NO_OP);
    }

    public static void mergeIdenticalAliases(BoundaryLine bl1, BoundaryLine bl2, Map<String, String> aliases, Reporter reporter) {
        for (String alias : bl1.getAliases()) {
            if (bl2.getAliases().contains(alias)) {
                LOGGER.debug("Alias '{}' is found in boundary lines '{}' and '{}'. It is moved to their new tie line.", alias, bl1.getId(), bl2.getId());
                moveCommonAliases(reporter, alias, bl1.getId(), bl2.getId());
                String type1 = bl1.getAliasType(alias).orElse("");
                String type2 = bl2.getAliasType(alias).orElse("");
                if (type1.equals(type2)) {
                    aliases.put(alias, type1);
                } else {
                    LOGGER.warn("Inconsistencies found for alias '{}' type in boundary lines '{}' and '{}'. Type is lost.", alias, bl1.getId(), bl2.getId());
                    inconsistentAliasTypes(reporter, alias, type1, type2, bl1.getId(), bl2.getId());
                    aliases.put(alias, "");
                }
            }
        }
        aliases.keySet().forEach(alias -> {
            bl1.removeAlias(alias);
            bl2.removeAlias(alias);
        });
    }

    public static void mergeDifferentAliases(BoundaryLine bl1, BoundaryLine bl2, Map<String, String> aliases, Reporter reporter) {
        for (String alias : bl1.getAliases()) {
            if (!bl2.getAliases().contains(alias)) {
                aliases.put(alias, bl1.getAliasType(alias).orElse(""));
            }
        }
        for (String alias : bl2.getAliases()) {
            if (!bl1.getAliases().contains(alias)) {
                String type = bl2.getAliasType(alias).orElse("");
                if (!type.isEmpty() && aliases.containsValue(type)) {
                    String tmpType = type;
                    String alias1 = aliases.entrySet().stream().filter(e -> tmpType.equals(e.getValue())).map(Map.Entry::getKey).findFirst().orElseThrow(IllegalStateException::new);
                    aliases.put(alias1, type + "_1");
                    LOGGER.warn("Inconsistencies found for alias type '{}'('{}' for '{}' and '{}' for '{}'). " +
                            "Types are respectively renamed as '{}_1' and '{}_2'.", type, alias1, bl1.getId(), alias, bl2.getId(), type, type);
                    inconsistentAliasValues(reporter, alias1, alias, type, bl1.getId(), bl2.getId());
                    type += "_2";
                }
                aliases.put(alias, type);
            }
        }
        aliases.keySet().forEach(alias -> {
            if (bl1.getAliases().contains(alias)) {
                bl1.removeAlias(alias);
            }
            if (bl2.getAliases().contains(alias)) {
                bl2.removeAlias(alias);
            }
        });
    }

    /**
     * If it exists, find the boundary line in the merging network that should be associated to a candidate boundary line in the network to be merged.
     * Two boundary lines in different IGM should be associated if:
     * - they have the same ID and at least one has a non-null X-node code
     * OR
     * - they have the same non-null X-node code and are the only boundary lines to have this X-node code in their respective networks
     * OR
     * - they have the same non-null X-node code and are the only connected boundary lines to have this X-node code in their respective networks
     *
     * @param candidateBoundaryLine candidate boundary line in the network to be merged
     * @param boundaryLine boundary line in the merging network with same ID as the candidate boundary line. Can be null.
     * @param getBoundaryLinesByXnodeCode function to retrieve boundary lines with a given X-node code in the merging network.
     * @param associateBoundaryLines function associating two boundary lines
     */
    public static void findAndAssociateBoundaryLines(BoundaryLine candidateBoundaryLine, BoundaryLine boundaryLine,
                                                     Function<String, List<BoundaryLine>> getBoundaryLinesByXnodeCode,
                                                     BiConsumer<BoundaryLine, BoundaryLine> associateBoundaryLines) {
        Objects.requireNonNull(candidateBoundaryLine);
        Objects.requireNonNull(getBoundaryLinesByXnodeCode);
        Objects.requireNonNull(associateBoundaryLines);
        if (boundaryLine == null) { // if boundary line with same ID not present, find boundary line(s) with same X-node code in merging network if present
            // mapping by ucte xnode code
            if (candidateBoundaryLine.getUcteXnodeCode() != null) { // if X-node code null: no associated boundary line
                if (candidateBoundaryLine.getNetwork().getBoundaryLineStream(BoundaryLineFilter.UNPAIRED)
                        .filter(d -> d != candidateBoundaryLine)
                        .filter(d -> candidateBoundaryLine.getUcteXnodeCode().equals(d.getUcteXnodeCode()))
                        .anyMatch(d -> d.getTerminal().isConnected())) { // check that there is no connected boundary line with same X-node code in the network to be merged
                    return;                                         // in that case, do nothing
                }
                List<BoundaryLine> bls = getBoundaryLinesByXnodeCode.apply(candidateBoundaryLine.getUcteXnodeCode());
                if (bls != null) {
                    if (bls.size() == 1) { // if there is exactly one boundary line in the merging network, merge it
                        associateBoundaryLines.accept(bls.get(0), candidateBoundaryLine);
                    }
                    if (bls.size() > 1) { // if more than one boundary line in the merging network, check how many are connected
                        List<BoundaryLine> connectedDls = bls.stream().filter(bl -> bl.getTerminal().isConnected()).collect(Collectors.toList());
                        if (connectedDls.size() == 1) { // if there is exactly one connected boundary line in the merging network, merge it. Otherwise, do nothing
                            associateBoundaryLines.accept(connectedDls.get(0), candidateBoundaryLine);
                        }
                    }
                }
            }
        } else {
            // if boundary line with same ID present, there is only one: they are associated if the X-node code is identical (if not: throw exception)
            boolean differentUcteXnodeCode = boundaryLine.getUcteXnodeCode() != null && candidateBoundaryLine.getUcteXnodeCode() != null
                    && !boundaryLine.getUcteXnodeCode().equals(candidateBoundaryLine.getUcteXnodeCode());
            boolean noUcteXnodeCode = boundaryLine.getUcteXnodeCode() == null && candidateBoundaryLine.getUcteXnodeCode() == null;
            if (differentUcteXnodeCode || noUcteXnodeCode) {
                throw new PowsyblException("Boundary line couple " + boundaryLine.getId()
                        + " have inconsistent Xnodes (" + boundaryLine.getUcteXnodeCode()
                        + "!=" + candidateBoundaryLine.getUcteXnodeCode() + ")");
            }
            String code = Optional.ofNullable(boundaryLine.getUcteXnodeCode()).orElseGet(candidateBoundaryLine::getUcteXnodeCode);
            List<BoundaryLine> dls = getBoundaryLinesByXnodeCode.apply(code);
            if (dls != null && dls.size() > 1) {
                throw new PowsyblException("Should not have any boundary lines other than " + boundaryLine.getId() + " linked to " + code);
            }
            associateBoundaryLines.accept(boundaryLine, candidateBoundaryLine);
        }
    }

    public static double getR(BoundaryLine bl1, BoundaryLine bl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(bl1, bl2);
        // Add 0.0 to avoid negative zero, tests where the R value is compared as text, fail
        return adm.y12().negate().reciprocal().getReal() + 0.0;
    }

    public static double getX(BoundaryLine bl1, BoundaryLine bl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(bl1, bl2);
        // Add 0.0 to avoid negative zero, tests where the X value is compared as text, fail
        return adm.y12().negate().reciprocal().getImaginary() + 0.0;
    }

    public static double getG1(BoundaryLine bl1, BoundaryLine bl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(bl1, bl2);
        return adm.y11().add(adm.y12()).getReal();
    }

    public static double getB1(BoundaryLine bl1, BoundaryLine bl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(bl1, bl2);
        return adm.y11().add(adm.y12()).getImaginary();
    }

    public static double getG2(BoundaryLine bl1, BoundaryLine bl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(bl1, bl2);
        return adm.y22().add(adm.y21()).getReal();
    }

    public static double getB2(BoundaryLine bl1, BoundaryLine bl2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(bl1, bl2);
        return adm.y22().add(adm.y21()).getImaginary();
    }

    private static LinkData.BranchAdmittanceMatrix equivalentBranchAdmittanceMatrix(BoundaryLine bl1,
                                                                                    BoundaryLine bl2) {
        // zero impedance half lines should be supported

        BranchAdmittanceMatrix adm1 = LinkData.calculateBranchAdmittance(bl1.getR(), bl1.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(bl1.getG(), bl1.getB()), new Complex(0.0, 0.0));
        BranchAdmittanceMatrix adm2 = LinkData.calculateBranchAdmittance(bl2.getR(), bl2.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(0.0, 0.0), new Complex(bl2.getG(), bl2.getB()));

        if (zeroImpedanceLine(adm1)) {
            return adm2;
        } else if (zeroImpedanceLine(adm2)) {
            return adm1;
        } else {
            return LinkData.kronChain(adm1, Branch.Side.TWO, adm2, Branch.Side.ONE);
        }
    }

    private static boolean zeroImpedanceLine(BranchAdmittanceMatrix adm) {
        if (adm.y12().getReal() == 0.0 && adm.y12().getImaginary() == 0.0) {
            return true;
        } else {
            return adm.y21().getReal() == 0.0 && adm.y22().getImaginary() == 0.0;
        }
    }
}
