/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class FlowsValidationTest extends AbstractValidationTest {

    private final double r = 0.04;
    private final double x = 0.423;
    private final double g1 = 0.0;
    private final double g2 = 0.0;
    private final double b1 = 0.0;
    private final double b2 = 0.0;
    private final double rho1 = 1;
    private final double rho2 = 11.249999728;
    private final double alpha1 = 0.0;
    private final double alpha2 = 0.0;
    private final double u1 = 236.80258178710938;
    private final double ratedU1 = 225.0;
    private final double u2 = 21.04814910888672;
    private final double ratedU2 = 20.0;
    private final double theta1 = 0.1257718437996544;
    private final double theta2 = 0.12547118123496284;
    private final boolean connected1 = true;
    private final boolean connected2 = true;
    private final boolean mainComponent1 = true;
    private final boolean mainComponent2 = true;

    private Bus bus1;
    private Bus bus2;
    private Terminal terminal1;
    private Terminal terminal2;
    private Line line1;
    private RatioTapChanger ratioTapChanger;
    private TwoWindingsTransformer transformer1;
    private TieLine tieLine1;

    private ValidationConfig looseConfigSpecificCompatibility;
    private ValidationConfig strictConfigSpecificCompatibility;

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();

        double p1 = 39.5056;
        double q1 = -3.72344;
        double p2 = -39.5122;
        double q2 = 3.7746;

        bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getV()).thenReturn(u1);
        Mockito.when(bus1.getAngle()).thenReturn(Math.toDegrees(theta1));
        Mockito.when(bus1.isInMainConnectedComponent()).thenReturn(true);

        bus2 = Mockito.mock(Bus.class);
        Mockito.when(bus2.getV()).thenReturn(u1);
        Mockito.when(bus2.getAngle()).thenReturn(Math.toDegrees(theta2));
        Mockito.when(bus2.isInMainConnectedComponent()).thenReturn(true);

        BusView busView1 = Mockito.mock(BusView.class);
        Mockito.when(busView1.getBus()).thenReturn(bus1);

        BusView busView2 = Mockito.mock(BusView.class);
        Mockito.when(busView2.getBus()).thenReturn(bus2);

        terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getP()).thenReturn(p1);
        Mockito.when(terminal1.getQ()).thenReturn(q1);
        Mockito.when(terminal1.getBusView()).thenReturn(busView1);

        terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getP()).thenReturn(p2);
        Mockito.when(terminal2.getQ()).thenReturn(q2);
        Mockito.when(terminal2.getBusView()).thenReturn(busView2);

        line1 = Mockito.mock(Line.class);
        Mockito.when(line1.getId()).thenReturn("line1");
        Mockito.when(line1.getTerminal1()).thenReturn(terminal1);
        Mockito.when(line1.getTerminal2()).thenReturn(terminal2);
        Mockito.when(line1.getR()).thenReturn(r);
        Mockito.when(line1.getX()).thenReturn(x);
        Mockito.when(line1.getG1()).thenReturn(g1);
        Mockito.when(line1.getG2()).thenReturn(g2);
        Mockito.when(line1.getB1()).thenReturn(b1);
        Mockito.when(line1.getB2()).thenReturn(b2);

        RatioTapChangerStep step = Mockito.mock(RatioTapChangerStep.class);
        Mockito.when(step.getR()).thenReturn(r);
        Mockito.when(step.getX()).thenReturn(x);
        Mockito.when(step.getG()).thenReturn(g1);
        Mockito.when(step.getB()).thenReturn(b1);
        Mockito.when(step.getRho()).thenReturn(rho2);

        ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.getCurrentStep()).thenReturn(step);

        transformer1 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformer1.getId()).thenReturn("transformer1");
        Mockito.when(transformer1.getTerminal1()).thenReturn(terminal1);
        Mockito.when(transformer1.getTerminal2()).thenReturn(terminal2);
        Mockito.when(transformer1.getR()).thenReturn(r * (1 - r / 100));
        Mockito.when(transformer1.getX()).thenReturn(x * (1 - x / 100));
        Mockito.when(transformer1.getG()).thenReturn(g1 * (1 - g1 / 100));
        Mockito.when(transformer1.getB()).thenReturn(b1 * 2 * (1 - b1 / 100));
        Mockito.when(transformer1.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer1.getOptionalRatioTapChanger()).thenReturn(Optional.of(ratioTapChanger));
        Mockito.when(transformer1.hasRatioTapChanger()).thenReturn(true);
        Mockito.when(transformer1.getRatedU1()).thenReturn(ratedU1);
        Mockito.when(transformer1.getRatedU2()).thenReturn(ratedU2);

        tieLine1 = Mockito.mock(TieLine.class);
        Mockito.when(tieLine1.getId()).thenReturn("tieLine1");
        DanglingLine dl1 = Mockito.mock(DanglingLine.class);
        Mockito.when(tieLine1.getDanglingLine1()).thenReturn(dl1);
        Mockito.when(dl1.getTerminal()).thenReturn(terminal1);

        DanglingLine dl2 = Mockito.mock(DanglingLine.class);
        Mockito.when(tieLine1.getDanglingLine2()).thenReturn(dl2);
        Mockito.when(dl2.getTerminal()).thenReturn(terminal2);

        Mockito.when(tieLine1.getR()).thenReturn(r);
        Mockito.when(tieLine1.getX()).thenReturn(x);
        Mockito.when(tieLine1.getG1()).thenReturn(g1);
        Mockito.when(tieLine1.getG2()).thenReturn(g2);
        Mockito.when(tieLine1.getB1()).thenReturn(b1);
        Mockito.when(tieLine1.getB2()).thenReturn(b2);

        looseConfigSpecificCompatibility = new ValidationConfig(0.1, true, "LoadFlowMock", ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters().setTwtSplitShuntAdmittance(true), ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
        strictConfigSpecificCompatibility = new ValidationConfig(0.01, false, "LoadFlowMock", ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters().setTwtSplitShuntAdmittance(true), ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
    }

    @Test
    void checkFlows() {
        double p1 = 40.0744;
        double q1 = 2.3124743;
        double p2 = -40.073254;
        double q2 = -2.3003194;

        assertTrue(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                              mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                               mainComponent1, mainComponent2, strictConfig.getEpsilonX(), strictConfig.applyReactanceCorrection()), strictConfig, NullWriter.INSTANCE));

        double r = 0.04 / (rho2 * rho2);
        double x = 0.423 / (rho2 * rho2);
        double rho1 = 1 / rho2;
        double rho2 = 1;

        assertTrue(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                              mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                               mainComponent1, mainComponent2, strictConfig.getEpsilonX(), strictConfig.applyReactanceCorrection()), strictConfig, NullWriter.INSTANCE));

        // check disconnected at one end
        assertTrue(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, Double.NaN, u2, Double.NaN, theta2, alpha1, alpha2, g1, g2, b1, b2, Double.NaN, Double.NaN, 0f, 0f, false, connected2,
                                            mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, new PrintWriter(System.err)));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, Double.NaN, u2, Double.NaN, theta2, alpha1, alpha2, g1, g2, b1, b2, Double.NaN, Double.NaN, 0.2f, 0f, false, connected2,
                                               mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));

        // check disconnected at both ends
        assertTrue(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, Double.NaN, Double.NaN, Double.NaN, Double.NaN, alpha1, alpha2, g1, g2, b1, b2, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                                              false, false, mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, Double.NaN, Double.NaN, Double.NaN, Double.NaN, alpha1, alpha2, g1, g2, b1, b2, p1, q2, Double.NaN, Double.NaN,
                                              false, false, mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, rho1, rho2, Double.NaN, Double.NaN, Double.NaN, Double.NaN, alpha1, alpha2, g1, g2, b1, b2, Double.NaN, Double.NaN, p2, q2,
                                              false, false, mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));

        // check with NaN values
        assertFalse(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, Double.NaN, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                               mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(true);
        assertTrue(FlowsValidation.INSTANCE.checkFlows(new BranchData("test", r, x, Double.NaN, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                              mainComponent1, mainComponent2, looseConfig.getEpsilonX(), looseConfig.applyReactanceCorrection()), looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(false);
    }

    @Test
    void checkLineFlows() {
        assertTrue(FlowsValidation.INSTANCE.checkFlows(line1, looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(line1, strictConfig, NullWriter.INSTANCE));
        Mockito.when(bus1.isInMainConnectedComponent()).thenReturn(false);
        Mockito.when(bus2.isInMainConnectedComponent()).thenReturn(false);
        assertTrue(FlowsValidation.INSTANCE.checkFlows(line1, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkTransformerFlows() {
        assertTrue(FlowsValidation.INSTANCE.checkFlows(transformer1, looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(transformer1, strictConfig, NullWriter.INSTANCE));
        Mockito.when(bus1.isInMainConnectedComponent()).thenReturn(false);
        Mockito.when(bus2.isInMainConnectedComponent()).thenReturn(false);
        assertTrue(FlowsValidation.INSTANCE.checkFlows(transformer1, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkTieLineFlows() {
        assertTrue(FlowsValidation.INSTANCE.checkFlows(tieLine1, looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(tieLine1, strictConfig, NullWriter.INSTANCE));
        Mockito.when(bus1.isInMainConnectedComponent()).thenReturn(false);
        Mockito.when(bus2.isInMainConnectedComponent()).thenReturn(false);
        assertTrue(FlowsValidation.INSTANCE.checkFlows(tieLine1, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkTransformerFlowsSpecificCompatibility() {
        assertTrue(FlowsValidation.INSTANCE.checkFlows(transformer1, looseConfigSpecificCompatibility, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(transformer1, strictConfigSpecificCompatibility, NullWriter.INSTANCE));
    }

    @Test
    void checkNetworkFlows() throws IOException {
        Line line2 = Mockito.mock(Line.class);
        Mockito.when(line2.getId()).thenReturn("line2");
        Mockito.when(line2.getTerminal1()).thenReturn(terminal1);
        Mockito.when(line2.getTerminal2()).thenReturn(terminal2);
        Mockito.when(line2.getR()).thenReturn(r);
        Mockito.when(line2.getX()).thenReturn(x);
        Mockito.when(line2.getG1()).thenReturn(g1);
        Mockito.when(line2.getG2()).thenReturn(g2);
        Mockito.when(line2.getB1()).thenReturn(b1);
        Mockito.when(line2.getB2()).thenReturn(b2);

        TwoWindingsTransformer transformer2 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformer2.getId()).thenReturn("transformer2");
        Mockito.when(transformer2.getTerminal1()).thenReturn(terminal1);
        Mockito.when(transformer2.getTerminal2()).thenReturn(terminal2);
        Mockito.when(transformer2.getR()).thenReturn(r * (1 - r / 100));
        Mockito.when(transformer2.getX()).thenReturn(x * (1 - x / 100));
        Mockito.when(transformer2.getG()).thenReturn(g1 * (1 - g1 / 100));
        Mockito.when(transformer2.getB()).thenReturn(b1 * 2 * (1 - b1 / 100));
        Mockito.when(transformer2.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer2.getOptionalRatioTapChanger()).thenReturn(Optional.ofNullable(ratioTapChanger));
        Mockito.when(transformer2.hasRatioTapChanger()).thenReturn(true);
        Mockito.when(transformer2.getRatedU1()).thenReturn(ratedU1);
        Mockito.when(transformer2.getRatedU2()).thenReturn(ratedU2);

        assertTrue(FlowsValidation.INSTANCE.checkFlows(transformer2, looseConfig, NullWriter.INSTANCE));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(transformer2, strictConfig, NullWriter.INSTANCE));

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getLineStream()).thenAnswer(dummy -> Stream.of(line2, line1));
        Mockito.when(network.getTwoWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(transformer2, transformer1));

        assertTrue(FlowsValidation.INSTANCE.checkFlows(network, looseConfig, data));
        assertFalse(FlowsValidation.INSTANCE.checkFlows(network, strictConfig, data));

        assertTrue(ValidationType.FLOWS.check(network, looseConfig, tmpDir));
        assertFalse(ValidationType.FLOWS.check(network, strictConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.INSTANCE, ValidationType.FLOWS);
        assertTrue(ValidationType.FLOWS.check(network, looseConfig, validationWriter));
    }
}
