/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.apache.commons.io.output.NullWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FlowsValidationTest extends AbstractValidationTest {

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

    private ValidationConfig looseConfigSpecificCompatibility;
    private ValidationConfig strictConfigSpecificCompatibility;

    @Before
    public void setUp() {
        float p1 = 39.5056f;
        float q1 = -3.72344f;
        float p2 = -39.5122f;
        float q2 = 3.7746f;

        bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getV()).thenReturn((float) u1);
        Mockito.when(bus1.getAngle()).thenReturn((float) Math.toDegrees(theta1));
        Mockito.when(bus1.isInMainConnectedComponent()).thenReturn(true);

        bus2 = Mockito.mock(Bus.class);
        Mockito.when(bus2.getV()).thenReturn((float) u1);
        Mockito.when(bus2.getAngle()).thenReturn((float) Math.toDegrees(theta2));
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
        Mockito.when(line1.getR()).thenReturn((float) r);
        Mockito.when(line1.getX()).thenReturn((float) x);
        Mockito.when(line1.getG1()).thenReturn((float) g1);
        Mockito.when(line1.getG2()).thenReturn((float) g2);
        Mockito.when(line1.getB1()).thenReturn((float) b1);
        Mockito.when(line1.getB2()).thenReturn((float) b2);

        RatioTapChangerStep step = Mockito.mock(RatioTapChangerStep.class);
        Mockito.when(step.getR()).thenReturn((float) r);
        Mockito.when(step.getX()).thenReturn((float) x);
        Mockito.when(step.getG()).thenReturn((float) g1);
        Mockito.when(step.getB()).thenReturn((float) b1);
        Mockito.when(step.getRho()).thenReturn((float) rho2);

        ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.getCurrentStep()).thenReturn(step);

        transformer1 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformer1.getId()).thenReturn("transformer1");
        Mockito.when(transformer1.getTerminal1()).thenReturn(terminal1);
        Mockito.when(transformer1.getTerminal2()).thenReturn(terminal2);
        Mockito.when(transformer1.getR()).thenReturn((float) (r * (1 - r / 100)));
        Mockito.when(transformer1.getX()).thenReturn((float) (x * (1 - x / 100)));
        Mockito.when(transformer1.getG()).thenReturn((float) (g1 * (1 - g1 / 100)));
        Mockito.when(transformer1.getB()).thenReturn((float) (b1 * 2 * (1 - b1 / 100)));
        Mockito.when(transformer1.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer1.getRatedU1()).thenReturn((float) ratedU1);
        Mockito.when(transformer1.getRatedU2()).thenReturn((float) ratedU2);

        looseConfigSpecificCompatibility = new ValidationConfig(0.1f, true, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters().setSpecificCompatibility(true), ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
        strictConfigSpecificCompatibility = new ValidationConfig(0.01f, false, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters().setSpecificCompatibility(true), ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
    }

    @Test
    public void checkFlows() {
        float p1 = 40.0744f;
        float q1 = 2.3124743f;
        float p2 = -40.073254f;
        float q2 = -2.3003194f;

        assertTrue(FlowsValidation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                              mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                               mainComponent1, mainComponent2, strictConfig, NullWriter.NULL_WRITER));

        double r = 0.04 / (rho2 * rho2);
        double x = 0.423 / (rho2 * rho2);
        double rho1 = 1 / rho2;
        double rho2 = 1;

        assertTrue(FlowsValidation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                              mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                               mainComponent1, mainComponent2, strictConfig, NullWriter.NULL_WRITER));

        // check disconnected on one end
        assertTrue(FlowsValidation.checkFlows("test", r, x, rho1, rho2, Float.NaN, u2, Float.NaN, theta2, alpha1, alpha2, g1, g2, b1, b2, Float.NaN, Float.NaN, 0f, 0f, false, connected2,
                                            mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows("test", r, x, rho1, rho2, Float.NaN, u2, Float.NaN, theta2, alpha1, alpha2, g1, g2, b1, b2, Float.NaN, Float.NaN, 0.2f, 0f, false, connected2,
                                               mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));

        // check disconnected on both end
        assertTrue(FlowsValidation.checkFlows("test", r, x, rho1, rho2, Float.NaN, Float.NaN, Float.NaN, Float.NaN, alpha1, alpha2, g1, g2, b1, b2, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                                              false, false, mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows("test", r, x, rho1, rho2, Float.NaN, Float.NaN, Float.NaN, Float.NaN, alpha1, alpha2, g1, g2, b1, b2, p1, q2, Float.NaN, Float.NaN,
                                              false, false, mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows("test", r, x, rho1, rho2, Float.NaN, Float.NaN, Float.NaN, Float.NaN, alpha1, alpha2, g1, g2, b1, b2, Float.NaN, Float.NaN, p2, q2,
                                              false, false, mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));

        // check with NaN values
        assertFalse(FlowsValidation.checkFlows("test", r, x, Double.NaN, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                               mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(true);
        assertTrue(FlowsValidation.checkFlows("test", r, x, Double.NaN, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                                              mainComponent1, mainComponent2, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(false);
    }

    @Test
    public void checkLineFlows() {
        assertTrue(FlowsValidation.checkFlows(line1, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows(line1, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(bus1.isInMainConnectedComponent()).thenReturn(false);
        Mockito.when(bus2.isInMainConnectedComponent()).thenReturn(false);
        assertTrue(FlowsValidation.checkFlows(line1, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkTransformerFlows() {
        assertTrue(FlowsValidation.checkFlows(transformer1, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows(transformer1, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(bus1.isInMainConnectedComponent()).thenReturn(false);
        Mockito.when(bus2.isInMainConnectedComponent()).thenReturn(false);
        assertTrue(FlowsValidation.checkFlows(transformer1, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkTransformerFlowsSpecificCompatibility() {
        assertTrue(FlowsValidation.checkFlows(transformer1, looseConfigSpecificCompatibility, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows(transformer1, strictConfigSpecificCompatibility, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkFlows() {
        Line line2 = Mockito.mock(Line.class);
        Mockito.when(line2.getId()).thenReturn("line2");
        Mockito.when(line2.getTerminal1()).thenReturn(terminal1);
        Mockito.when(line2.getTerminal2()).thenReturn(terminal2);
        Mockito.when(line2.getR()).thenReturn((float) r);
        Mockito.when(line2.getX()).thenReturn((float) x);
        Mockito.when(line2.getG1()).thenReturn((float) g1);
        Mockito.when(line2.getG2()).thenReturn((float) g2);
        Mockito.when(line2.getB1()).thenReturn((float) b1);
        Mockito.when(line2.getB2()).thenReturn((float) b2);

        TwoWindingsTransformer transformer2 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformer2.getId()).thenReturn("transformer2");
        Mockito.when(transformer2.getTerminal1()).thenReturn(terminal1);
        Mockito.when(transformer2.getTerminal2()).thenReturn(terminal2);
        Mockito.when(transformer2.getR()).thenReturn((float) (r * (1 - r / 100)));
        Mockito.when(transformer2.getX()).thenReturn((float) (x * (1 - x / 100)));
        Mockito.when(transformer2.getG()).thenReturn((float) (g1 * (1 - g1 / 100)));
        Mockito.when(transformer2.getB()).thenReturn((float) (b1 * 2 * (1 - b1 / 100)));
        Mockito.when(transformer2.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer2.getRatedU1()).thenReturn((float) ratedU1);
        Mockito.when(transformer2.getRatedU2()).thenReturn((float) ratedU2);

        assertTrue(FlowsValidation.checkFlows(transformer2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows(transformer2, strictConfig, NullWriter.NULL_WRITER));

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getLineStream()).thenAnswer(dummy -> Stream.of(line2, line1));
        Mockito.when(network.getTwoWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(transformer2, transformer1));

        assertTrue(FlowsValidation.checkFlows(network, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(FlowsValidation.checkFlows(network, strictConfig, NullWriter.NULL_WRITER));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.NULL_WRITER, ValidationType.FLOWS);
        assertTrue(ValidationType.FLOWS.check(network, looseConfig, validationWriter));
    }

}
