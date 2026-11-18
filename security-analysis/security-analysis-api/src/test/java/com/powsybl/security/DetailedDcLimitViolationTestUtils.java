/**
 * Copyright (c) 2026, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.iidm.network.DcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;

import java.util.List;

/**
 * Fixture and helpers shared by the detailed DC limit violation detection tests.
 *
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 */
public final class DetailedDcLimitViolationTestUtils {

    /**
     * AC voltage level of the fixture, subject of the AC violation written by {@link #setUpAcAndDetailedDcViolations}.
     */
    public static final String AC_SUBJECT = "VL-FR";

    /**
     * Violations written by {@link #setUpAcAndDetailedDcViolations} on detailed DC equipment, as given by {@link #subjects}.
     */
    public static final List<String> DETAILED_DC_SUBJECTS = List.of("VscFr/ACTIVE_POWER", "dcLinePos/CURRENT", "dcNodeFrPos/HIGH_VOLTAGE");

    private DetailedDcLimitViolationTestUtils() {
    }

    /**
     * Writes, on a network created by {@link DcDetailedNetworkFactory#createVscSymmetricalMonopole()}, one violation
     * on each kind of detailed DC equipment and one AC bus voltage violation.
     */
    public static void setUpAcAndDetailedDcViolations(Network network) {
        // AC: VL-FR has limits [380 kV, 420 kV]
        network.getBusBreakerView().getBus("BUS-FR").setV(430.);
        // DC node
        network.getDcNode(DcDetailedNetworkFactory.DC_NODE_FR_POS).setHighVoltageLimit(525.).setV(530.);
        // DC line
        DcLine line = network.getDcLine("dcLinePos").setPermanentCurrentLimit(1000.);
        line.getDcTerminal1().setI(1200.);
        line.getDcTerminal2().setI(-1200.);
        // Converter
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("VscFr").setMaxP(200.);
        vsc.getPccTerminal().setP(250.);
    }

    /**
     * @return the sorted "subjectId/limitType" of the violations
     */
    public static List<String> subjects(List<LimitViolation> violations) {
        return violations.stream().map(v -> v.getSubjectId() + "/" + v.getLimitType()).sorted().toList();
    }

    /**
     * @return a sorted description of every field of the violations, to compare two lists of violations
     */
    public static List<String> describeFully(List<LimitViolation> violations) {
        return violations.stream()
                .map(v -> String.join("|", v.getSubjectId(), String.valueOf(v.getSubjectName()), v.getLimitType().name(),
                        String.valueOf(v.getLimit()), String.valueOf(v.getValue()), String.valueOf(v.getLimitScaling()),
                        String.valueOf(v.getSide()), String.valueOf(v.getLimitName()), String.valueOf(v.getAcceptableDuration()),
                        String.valueOf(v.getOperationalLimitsGroupId()), String.valueOf(v.getViolationLocation())))
                .sorted()
                .toList();
    }
}
