/**
 * Copyright (c) 2026, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LoadingLimitType;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import static com.powsybl.security.DetailedDcEquipmentLimitViolationTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Detailed DC limit violation detection through {@link LimitViolationDetector}.
 *
 * The detection scenarios and the content of the violations are tested in
 * {@code LimitViolationDetectionDetailedDcTest}. This class only checks that the detector entry points report the
 * detailed DC violations, and the behaviors specific to the detectors.
 *
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 */
class LimitViolationDetectorDetailedDcTest {

    private static final double DC_POWER_FACTOR = 0.95;
    private static final Contingency CONTINGENCY = new Contingency("contingency", List.of());

    private Network network;
    private List<LimitViolation> violations;

    /**
     * A contingency based detector that reports nothing on AC equipment.
     */
    static class SilentContingencyBasedDetector extends AbstractLimitViolationDetector {
        @Override
        public void checkCurrent(Contingency contingency, Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
        }

        @Override
        public void checkCurrent(Contingency contingency, ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
        }

        @Override
        public void checkVoltage(Contingency contingency, Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
        }

        @Override
        public void checkVoltageAngle(Contingency contingency, VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
        }

        @Override
        public void checkActivePower(Branch branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {
        }

        @Override
        public void checkActivePower(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        }

        @Override
        public void checkApparentPower(Branch branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {
        }

        @Override
        public void checkApparentPower(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        }
    }

    @BeforeEach
    void setUp() {
        network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        violations = new ArrayList<>();
    }

    // ================================================================================================================
    // Detector entry points
    // ================================================================================================================

    @Test
    void checkAllReportsAcAndDetailedDcViolations() {
        setUpAcAndDetailedDcEquipmentViolations(network);

        new DefaultLimitViolationDetector().checkAll(network, violations::add);

        assertEquals(List.of(AC_SUBJECT + "/HIGH_VOLTAGE", "VscFr/ACTIVE_POWER", "dcLinePos/CURRENT", "dcNodeFrPos/HIGH_VOLTAGE"),
                subjects(violations));
    }

    @Test
    void checkAllDcReportsDetailedDcViolations() {
        // AC voltages are not checked in DC (linearized) load flow mode
        setUpAcAndDetailedDcEquipmentViolations(network);

        new DefaultLimitViolationDetector().checkAllDc(network, DC_POWER_FACTOR, violations::add);

        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations));
    }

    @Test
    void checkAllDetailedDcReportsOnlyDetailedDcViolations() {
        setUpAcAndDetailedDcEquipmentViolations(network);

        new DefaultLimitViolationDetector().checkAllDetailedDcEquipment(network, violations::add);

        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations));
    }

    @Test
    void checkAllDetailedDcWithContingencyReportsOnlyDetailedDcViolations() {
        // The variant taking a contingency delegates to the variant without contingency.
        // This should be changed when DC contingencies are implemented.
        setUpAcAndDetailedDcEquipmentViolations(network);

        new DefaultLimitViolationDetector().checkAllDetailedDcEquipment(CONTINGENCY, network, violations::add);

        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations));
    }

    @Test
    void contingencyBasedDetectorReportsDetailedDcViolationsOnContingency() {
        // Tests the implementation of checkAll(Contingency, Network, Consumer) in AbstractLimitViolationDetector.
        // Unlike the default interface method, this override does not delegate to checkAll(Network, Consumer),
        // so it must call the detailed DC checks itself. The AC checks of the subclass are silent, so every reported
        // violation comes from the inherited implementation.
        setUpAcAndDetailedDcEquipmentViolations(network);

        new SilentContingencyBasedDetector().checkAll(CONTINGENCY, network, violations::add);

        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations));
    }

    // ================================================================================================================
    // Behaviors specific to the detectors
    // ================================================================================================================

    @Test
    void limitScalingOfTheDetectorIsIgnoredForDetailedDcEquipment() {
        // With a 0.5 scaling, these values would all be violations if limits were scaled.
        network.getDcNode(DcDetailedNetworkFactory.DC_NODE_FR_POS).setHighVoltageLimit(525.).setV(300.);
        DcLine line = network.getDcLine("dcLinePos").setPermanentCurrentLimit(1000.);
        line.getDcTerminal1().setI(600.);
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("VscFr").setMinP(-200.).setMaxP(200.);
        vsc.getPccTerminal().setP(150.);
        DefaultLimitViolationDetector detector = new DefaultLimitViolationDetector(0.5, EnumSet.allOf(LoadingLimitType.class));

        detector.checkAll(network, violations::add);
        detector.checkAllDc(network, DC_POWER_FACTOR, violations::add);
        detector.checkAllDetailedDcEquipment(network, violations::add);

        assertTrue(violations.isEmpty(), () -> subjects(violations).toString());
    }

    @Test
    void limitScalingOfDetailedDcViolationsIsOneWhateverTheDetectorScaling() {
        // Detailed DC violations have a limitScaling of 1, while the AC violations
        // of the same detector carry the detector scaling
        setUpAcAndDetailedDcEquipmentViolations(network);

        new DefaultLimitViolationDetector(0.5, EnumSet.allOf(LoadingLimitType.class)).checkAll(network, violations::add);

        assertEquals(List.of(AC_SUBJECT + "/HIGH_VOLTAGE", "VscFr/ACTIVE_POWER", "dcLinePos/CURRENT", "dcNodeFrPos/HIGH_VOLTAGE"),
                subjects(violations));
        violations.forEach(v -> assertEquals(AC_SUBJECT.equals(v.getSubjectId()) ? 0.5 : 1.0, v.getLimitScaling(), 0.0, v.getSubjectId()));
    }

    @Test
    void dcLineIsNotCheckedWhenDetectorExcludesPermanentLimitType() {
        setUpAcAndDetailedDcEquipmentViolations(network);

        new DefaultLimitViolationDetector(EnumSet.of(LoadingLimitType.TATL)).checkAllDetailedDcEquipment(network, violations::add);

        assertEquals(List.of("VscFr/ACTIVE_POWER", "dcNodeFrPos/HIGH_VOLTAGE"), subjects(violations));
    }

    @Test
    void dcLineIsCheckedWhenDetectorHasPermanentLimitTypeOnly() {
        setUpAcAndDetailedDcEquipmentViolations(network);

        new DefaultLimitViolationDetector(EnumSet.of(LoadingLimitType.PATL)).checkAllDetailedDcEquipment(network, violations::add);

        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations));
    }
}
