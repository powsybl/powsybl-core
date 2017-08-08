/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.Terminal.BusView;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.mock.LoadFlowFactoryMock;
import org.apache.commons.io.output.NullWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ValidationTest {

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

    private Terminal terminal1;
    private Terminal terminal2;
    private Line line1;
    private RatioTapChanger ratioTapChanger;
    private TwoWindingsTransformer transformer1;

    private ValidationConfig looseConfig;
    private ValidationConfig strictConfig;
    private ValidationConfig looseConfigSpecificCompatibility;
    private ValidationConfig strictConfigSpecificCompatibility;


    private float p = -39.5056f;
    private float q = 3.72344f;
    private float v = 380f;
    private float targetP = 39.5056f;
    private float targetQ = -3.72344f;
    private final float targetV = 380f;
    private boolean voltageRegulatorOn = true;
    private final float minQ = -10f;
    private final float maxQ = 0f;

    private BusView genBusView;
    private Terminal genTerminal;
    private Generator generator; 

    @Before
    public void setUp() {
        float p1 = 39.5056f;
        float q1 = -3.72344f;
        float p2 = -39.5122f;
        float q2 = 3.7746f;

        Bus bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getV()).thenReturn((float) u1);
        Mockito.when(bus1.getAngle()).thenReturn((float) Math.toDegrees(theta1));

        Bus bus2 = Mockito.mock(Bus.class);
        Mockito.when(bus2.getV()).thenReturn((float) u1);
        Mockito.when(bus2.getAngle()).thenReturn((float) Math.toDegrees(theta2));

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
        Mockito.when(transformer1.getR()).thenReturn((float) (r*(1-r/100)));
        Mockito.when(transformer1.getX()).thenReturn((float) (x*(1-x/100)));
        Mockito.when(transformer1.getG()).thenReturn((float) (g1*(1-g1/100)));
        Mockito.when(transformer1.getB()).thenReturn((float) (b1*2*(1-b1/100)));
        Mockito.when(transformer1.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer1.getRatedU1()).thenReturn((float) ratedU1);
        Mockito.when(transformer1.getRatedU2()).thenReturn((float) ratedU2);

        looseConfig = new ValidationConfig(0.1f, true, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                                           ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                                           ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters());
        strictConfig = new ValidationConfig(0.01f, false, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                                            ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                                            ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters());
        looseConfigSpecificCompatibility = new ValidationConfig(0.1f, true, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters().setSpecificCompatibility(true));
        strictConfigSpecificCompatibility = new ValidationConfig(0.01f, false, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters().setSpecificCompatibility(true));


        Bus genBus = Mockito.mock(Bus.class);
        Mockito.when(genBus.getV()).thenReturn(v);

        genBusView = Mockito.mock(BusView.class);
        Mockito.when(genBusView.getBus()).thenReturn(genBus);

        genTerminal = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal.getP()).thenReturn(p);
        Mockito.when(genTerminal.getQ()).thenReturn(q);
        Mockito.when(genTerminal.getBusView()).thenReturn(genBusView);

        ReactiveLimits genReactiveLimits = Mockito.mock(ReactiveLimits.class);
        Mockito.when(genReactiveLimits.getMinQ(Mockito.anyFloat())).thenReturn(minQ);
        Mockito.when(genReactiveLimits.getMaxQ(Mockito.anyFloat())).thenReturn(maxQ);

        generator =  Mockito.mock(Generator.class);
        Mockito.when(generator.getId()).thenReturn("gen");
        Mockito.when(generator.getTerminal()).thenReturn(genTerminal);
        Mockito.when(generator.getTargetP()).thenReturn(targetP);
        Mockito.when(generator.getTargetQ()).thenReturn(targetQ);
        Mockito.when(generator.getTargetV()).thenReturn(targetV);
        Mockito.when(generator.getReactiveLimits()).thenReturn(genReactiveLimits);
    }

    @Test
    public void checkFlows() {
        float p1 = 40.0744f;
        float q1 = 2.3124743f;
        float p2 = -40.073254f;
        float q2 = -2.3003194f;

        assertTrue(Validation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(Validation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, strictConfig, NullWriter.NULL_WRITER));

        double r = 0.04 / (rho2 * rho2);
        double x = 0.423 / (rho2 * rho2);
        double rho1 = 1 / rho2;
        double rho2 = 1;

        assertTrue(Validation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(Validation.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkLineFlows() {
        assertTrue(Validation.checkFlows(line1, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(Validation.checkFlows(line1, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkTransformerFlows() {
        assertTrue(Validation.checkFlows(transformer1, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(Validation.checkFlows(transformer1, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkTransformerFlowsSpecificCompatibility() {
        assertTrue(Validation.checkFlows(transformer1, looseConfigSpecificCompatibility, NullWriter.NULL_WRITER));
        assertFalse(Validation.checkFlows(transformer1, strictConfigSpecificCompatibility, NullWriter.NULL_WRITER));
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
        Mockito.when(transformer2.getR()).thenReturn((float) (r*(1-r/100)));
        Mockito.when(transformer2.getX()).thenReturn((float) (x*(1-x/100)));
        Mockito.when(transformer2.getG()).thenReturn((float) (g1*(1-g1/100)));
        Mockito.when(transformer2.getB()).thenReturn((float) (b1*2*(1-b1/100)));
        Mockito.when(transformer2.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer2.getRatedU1()).thenReturn((float) ratedU1);
        Mockito.when(transformer2.getRatedU2()).thenReturn((float) ratedU2);

        assertTrue(Validation.checkFlows(transformer2, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(Validation.checkFlows(transformer2, strictConfig, NullWriter.NULL_WRITER));

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getLineStream()).thenAnswer(dummy -> Stream.of(line2, line1));
        Mockito.when(network.getTwoWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(transformer2, transformer1));

        assertTrue(Validation.checkFlows(network, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(Validation.checkFlows(network, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkGeneratorsValues() {
        // active power should be equal to set point
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        p = -39.8f;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        p = -39.5056f;
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));

        //  if voltageRegulatorOn="false" then reactive power should be equal to set point
        voltageRegulatorOn = false;
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        q = 3.7f;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, looseConfig, NullWriter.NULL_WRITER));

        // if voltageRegulatorOn="true" then either V at the connected bus is equal to g.getTargetV()
        voltageRegulatorOn = true;
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        v = 400f;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));

        // if voltageRegulatorOn="true" then either q is equal to g.getReactiveLimits().getMinQ(p) and v is lower than targetV
        q = 10f;
        v = 360f;
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        v = 400f;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        q = 5f;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));

        // if voltageRegulatorOn="true" then either q is equal to g.getReactiveLimits().getMaxQ(p) and v is higher than targetV
        q = 0f;
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        v = 360f;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        q = 5f;
        v = 400f;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        // a validation error should be detected if there is both a voltage and a target but no p or q
        v = 380f;
        p = Float.NaN;
        q = Float.NaN;
        assertFalse(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        targetP = 0f;
        targetQ = 0f;
        assertTrue(Validation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkGenerators() {
        // active power should be equal to set point
        assertTrue(Validation.checkGenerators(generator, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(genTerminal.getP()).thenReturn(-39.8f);
        assertFalse(Validation.checkGenerators(generator, strictConfig, NullWriter.NULL_WRITER));

        // the unit is disconnected
        Mockito.when(genBusView.getBus()).thenReturn(null);
        assertTrue(Validation.checkGenerators(generator, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkGenerators() {
        Bus genBus1 = Mockito.mock(Bus.class);
        Mockito.when(genBus1.getV()).thenReturn(v);

        BusView genBusView1 = Mockito.mock(BusView.class);
        Mockito.when(genBusView1.getBus()).thenReturn(genBus1);

        Terminal genTerminal1 = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal1.getP()).thenReturn(p);
        Mockito.when(genTerminal1.getQ()).thenReturn(q);
        Mockito.when(genTerminal1.getBusView()).thenReturn(genBusView1);

        ReactiveLimits genReactiveLimits1 = Mockito.mock(ReactiveLimits.class);
        Mockito.when(genReactiveLimits1.getMinQ(Mockito.anyFloat())).thenReturn(minQ);
        Mockito.when(genReactiveLimits1.getMaxQ(Mockito.anyFloat())).thenReturn(maxQ);

        Generator generator1 =  Mockito.mock(Generator.class);
        Mockito.when(generator1.getId()).thenReturn("gen");
        Mockito.when(generator1.getTerminal()).thenReturn(genTerminal1);
        Mockito.when(generator1.getTargetP()).thenReturn(targetP);
        Mockito.when(generator1.getTargetQ()).thenReturn(targetQ);
        Mockito.when(generator1.getTargetV()).thenReturn(targetV);
        Mockito.when(generator1.getReactiveLimits()).thenReturn(genReactiveLimits1);

        assertTrue(Validation.checkGenerators(generator1, strictConfig, NullWriter.NULL_WRITER));
        
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getGeneratorStream()).thenAnswer(dummy -> Stream.of(generator, generator1));

        assertTrue(Validation.checkGenerators(network, looseConfig, NullWriter.NULL_WRITER));
    }
}
