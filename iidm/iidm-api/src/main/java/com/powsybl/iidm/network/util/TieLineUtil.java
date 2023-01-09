/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static void mergeProperties(DanglingLine dl1, DanglingLine dl2, Properties properties) {
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
                properties.setProperty(prop, dl2.getProperty(prop));
            } else if (dl2.getProperty(prop).isEmpty()) {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'", prop, dl1.getProperty(prop));
                properties.setProperty(prop, dl1.getProperty(prop));
            } else {
                LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line", prop, dl1.getProperty(prop), dl2.getProperty(prop));
            }
        });
        dl1Properties.forEach(prop -> properties.setProperty(prop + "_1", dl1.getProperty(prop)));
        dl2Properties.forEach(prop -> properties.setProperty(prop + "_2", dl2.getProperty(prop)));
    }

    /**
     * If it exists, find the dangling line in the merging network that should be associated to a candidate dangling line in the network to be merged.
     * Two dangling lines in different IGM should be associated if:
     * - they have the same ID and at least one has a non-null X-node code
     * OR
     * - they have the same non-null X-node code and are the only dangling lines to have this X-node code in their respective networks
     * OR
     * - they have the same non-null X-node code and are the only connected dangling lines to have this X-node code in their respective networks
     *
     * @param candidateDanglingLine candidate dangling line in the network to be merged
     * @param danglingLine dangling line in the merging network with same ID as the candidate dangling line. Can be null.
     * @param getDanglingLinesByXnodeCode function to retrieve dangling lines with a given X-node code in the merging network.
     * @param associateDanglingLines function associating two dangling lines
     */
    public static void findAndAssociateDanglingLines(DanglingLine candidateDanglingLine, DanglingLine danglingLine,
                                                     Function<String, List<DanglingLine>> getDanglingLinesByXnodeCode,
                                                     BiConsumer<DanglingLine, DanglingLine> associateDanglingLines) {
        Objects.requireNonNull(candidateDanglingLine);
        Objects.requireNonNull(getDanglingLinesByXnodeCode);
        Objects.requireNonNull(associateDanglingLines);
        if (danglingLine == null) { // if dangling line with same ID not present, find dangling line(s) with same X-node code in merging network if present
            // mapping by ucte xnode code
            if (candidateDanglingLine.getUcteXnodeCode() != null) { // if X-node code null: no associated dangling line
                if (candidateDanglingLine.getNetwork().getDanglingLineStream()
                        .filter(d -> d != candidateDanglingLine)
                        .filter(d -> candidateDanglingLine.getUcteXnodeCode().equals(d.getUcteXnodeCode()))
                        .anyMatch(d -> d.getTerminal().isConnected())) { // check that there is no connected dangling line with same X-node code in the network to be merged
                    return;                                         // in that case, do nothing
                }
                List<DanglingLine> dls = getDanglingLinesByXnodeCode.apply(candidateDanglingLine.getUcteXnodeCode());
                if (dls != null) {
                    if (dls.size() == 1) { // if there is exactly one dangling line in the merging network, merge it
                        associateDanglingLines.accept(dls.get(0), candidateDanglingLine);
                    }
                    if (dls.size() > 1) { // if more than one dangling line in the merging network, check how many are connected
                        List<DanglingLine> connectedDls = dls.stream().filter(dl -> dl.getTerminal().isConnected()).collect(Collectors.toList());
                        if (connectedDls.size() == 1) { // if there is exactly one connected dangling line in the merging network, merge it. Otherwise, do nothing
                            associateDanglingLines.accept(connectedDls.get(0), candidateDanglingLine);
                        }
                    }
                }
            }
        } else {
            // if dangling line with same ID present, there is only one: they are associated if the X-node code is identical (if not: throw exception)
            if ((danglingLine.getUcteXnodeCode() != null && candidateDanglingLine.getUcteXnodeCode() != null
                    && !danglingLine.getUcteXnodeCode().equals(candidateDanglingLine.getUcteXnodeCode())) || (danglingLine.getUcteXnodeCode() == null && candidateDanglingLine.getUcteXnodeCode() == null)) {
                throw new PowsyblException("Dangling line couple " + danglingLine.getId()
                        + " have inconsistent Xnodes (" + danglingLine.getUcteXnodeCode()
                        + "!=" + candidateDanglingLine.getUcteXnodeCode() + ")");
            }
            String code = Optional.ofNullable(danglingLine.getUcteXnodeCode()).orElseGet(candidateDanglingLine::getUcteXnodeCode);
            List<DanglingLine> dls = getDanglingLinesByXnodeCode.apply(code);
            if (dls != null && dls.size() > 1) {
                throw new PowsyblException("Should not have any dangling lines other than " + danglingLine.getId() + " linked to " + code);
            }
            associateDanglingLines.accept(danglingLine, candidateDanglingLine);
        }
    }

    public static double getR(DanglingLine half1, DanglingLine half2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(half1, half2);
        // Add 0.0 to avoid negative zero, tests where the R value is compared as text, fail
        return adm.y12().negate().reciprocal().getReal() + 0.0;
    }

    public static double getX(DanglingLine half1, DanglingLine half2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(half1, half2);
        // Add 0.0 to avoid negative zero, tests where the X value is compared as text, fail
        return adm.y12().negate().reciprocal().getImaginary() + 0.0;
    }

    public static double getG1(DanglingLine half1, DanglingLine half2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y11().add(adm.y12()).getReal();
    }

    public static double getB1(DanglingLine half1, DanglingLine half2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y11().add(adm.y12()).getImaginary();
    }

    public static double getG2(DanglingLine half1, DanglingLine half2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y22().add(adm.y21()).getReal();
    }

    public static double getB2(DanglingLine half1, DanglingLine half2) {
        LinkData.BranchAdmittanceMatrix adm = TieLineUtil.equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y22().add(adm.y21()).getImaginary();
    }

    private static LinkData.BranchAdmittanceMatrix equivalentBranchAdmittanceMatrix(DanglingLine half1,
        DanglingLine half2) {
        // zero impedance half lines should be supported

        BranchAdmittanceMatrix adm1 = LinkData.calculateBranchAdmittance(half1.getR(), half1.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(half1.getG(), half1.getB()), new Complex(0.0, 0.0));
        BranchAdmittanceMatrix adm2 = LinkData.calculateBranchAdmittance(half2.getR(), half2.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(half2.getG(), half2.getB()), new Complex(0.0, 0.0));

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
