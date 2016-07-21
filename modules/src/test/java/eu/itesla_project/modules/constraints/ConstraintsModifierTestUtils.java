/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.constraints;

import java.util.ArrayList;
import java.util.List;

import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.NetworkFactory;
import eu.itesla_project.iidm.network.Substation;
import eu.itesla_project.iidm.network.TopologyKind;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.security.LimitViolationType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ConstraintsModifierTestUtils {

    public static final String VOLTAGE_LEVEL_1_ID = "vl1";
    public static final float HIGH_VOLTAGE_LIMIT = 300f;
    public static final float NEW_HIGH_VOLTAGE_LIMIT = 381f;
    public static final String VOLTAGE_LEVEL_2_ID = "vl2";
    public static final float LOW_VOLTAGE_LIMIT = 420f;
    public static final float NEW_LOW_VOLTAGE_LIMIT = 378f;
    public static final String LINE_ID = "line1";
    public static final float CURRENT_LIMIT = 100f;
    public static final float NEW_CURRENT_LIMIT = 120f;
    private static final float CURRENT_VALUE = 119.25632f;
    private static final float V = 380f;
    private static final float Q = 55f;
    private static final float P = 56f;
    private static final Country COUNTRY = Country.FR;

    public static Network getNetwork() {
        Network n = NetworkFactory.create("test1", "test");
        Substation s1 = n.newSubstation()
                .setId("s1")
                .setCountry(COUNTRY)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_1_ID)
                .setNominalV(V)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setHighVoltageLimit(HIGH_VOLTAGE_LIMIT)
                .setLowVoltageLimit(200f)
                .add();
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        b1.setV(V);
        Substation s2 = n.newSubstation()
                .setId("s2")
                .setCountry(COUNTRY)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_2_ID)
                .setNominalV(V)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setHighVoltageLimit(550f)
                .setLowVoltageLimit(LOW_VOLTAGE_LIMIT)
                .add();
        Bus b2 = vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        b2.setV(V);
        Line l1 = n.newLine()
                .setId(LINE_ID)
                .setVoltageLevel1(VOLTAGE_LEVEL_1_ID)
                .setBus1("b1")
                .setConnectableBus1("b1")
                .setVoltageLevel2(VOLTAGE_LEVEL_2_ID)
                .setBus2("b2")
                .setConnectableBus2("b2")
                .setR(3)
                .setX(33)
                .setG1(0)
                .setB1(386E-6f / 2)
                .setG2(0f)
                .setB2(386E-6f / 2)
                .add();
        l1.newCurrentLimits1()
        .setPermanentLimit(CURRENT_LIMIT)
        .add();
        l1.newCurrentLimits2()
        .setPermanentLimit(CURRENT_LIMIT)
        .add();
        l1.getTerminal1().setP(P);
        l1.getTerminal1().setQ(Q);
        l1.getTerminal2().setP(P);
        l1.getTerminal2().setQ(Q);
        return n;
    }

    public static List<LimitViolation> getViolations(Network network) {
        List<LimitViolation> violations = new ArrayList<LimitViolation>();
        Line line = network.getLine(LINE_ID);
        violations.add(new LimitViolation(line, LimitViolationType.CURRENT, CURRENT_LIMIT, 1, CURRENT_VALUE, COUNTRY, Float.NaN));
        VoltageLevel voltageLevel = network.getVoltageLevel(VOLTAGE_LEVEL_1_ID);
        violations.add(new LimitViolation(voltageLevel, LimitViolationType.HIGH_VOLTAGE, HIGH_VOLTAGE_LIMIT, 1, V, COUNTRY, Float.NaN));
        VoltageLevel voltageLevel2 = network.getVoltageLevel(VOLTAGE_LEVEL_2_ID);
        violations.add(new LimitViolation(voltageLevel2, LimitViolationType.LOW_VOLTAGE, LOW_VOLTAGE_LIMIT, 1, V, COUNTRY, Float.NaN));
        return violations;
    }

}
