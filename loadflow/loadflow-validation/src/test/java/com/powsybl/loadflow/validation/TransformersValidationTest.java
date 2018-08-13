/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
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

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TransformersValidationTest extends AbstractValidationTest {

    private final double rho = 1.034;
    private final double rhoPreviousStep = 1.043;
    private final double rhoNextStep = 1.024;
    private final int tapPosition = 8;
    private final int lowTapPosition = 0;
    private final int highTapPosition = 30;
    private final double targetV = 92.7781;
    private final Side regulatedSide = Side.ONE;
    private final double v = 92.8007;
    private final double lowV = 88.13;;
    private final double highV = 97.342;
    private final boolean connected = true;
    private final boolean mainComponent = true;

    private TwoWindingsTransformer transformer;
    private Bus bus;

    @Before
    public void setUp() {
        bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getV()).thenReturn(v);
        Mockito.when(bus.isInMainConnectedComponent()).thenReturn(mainComponent);

        BusView busView = Mockito.mock(BusView.class);
        Mockito.when(busView.getBus()).thenReturn(bus);
        Mockito.when(busView.getConnectableBus()).thenReturn(bus);

        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getBusView()).thenReturn(busView);

        RatioTapChangerStep currentStep = Mockito.mock(RatioTapChangerStep.class);
        Mockito.when(currentStep.getRho()).thenReturn(rho);
        RatioTapChangerStep previousStep = Mockito.mock(RatioTapChangerStep.class);
        Mockito.when(previousStep.getRho()).thenReturn(rhoPreviousStep);
        RatioTapChangerStep nextStep = Mockito.mock(RatioTapChangerStep.class);
        Mockito.when(nextStep.getRho()).thenReturn(rhoNextStep);

        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger.getRegulationTerminal()).thenReturn(terminal);
        Mockito.when(ratioTapChanger.getTapPosition()).thenReturn(tapPosition);
        Mockito.when(ratioTapChanger.getLowTapPosition()).thenReturn(lowTapPosition);
        Mockito.when(ratioTapChanger.getHighTapPosition()).thenReturn(highTapPosition);
        Mockito.when(ratioTapChanger.getCurrentStep()).thenReturn(currentStep);
        Mockito.when(ratioTapChanger.getStep(tapPosition - 1)).thenReturn(previousStep);
        Mockito.when(ratioTapChanger.getStep(tapPosition + 1)).thenReturn(nextStep);
        Mockito.when(ratioTapChanger.getTargetV()).thenReturn(targetV);

        transformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformer.getId()).thenReturn("transformer");
        Mockito.when(transformer.getTerminal1()).thenReturn(terminal);
        Mockito.when(transformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
    }

    @Test
    public void checkTwtsValues() {
        assertTrue(TransformersValidation.checkTransformer("test", rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                                            targetV, regulatedSide, v, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        // Error >= -Max(UpIncrement, DownIncrement)
        assertFalse(TransformersValidation.checkTransformer("test", rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                                             targetV, regulatedSide, lowV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(TransformersValidation.checkTransformer("test", rho, Float.NaN, rhoNextStep, lowTapPosition, lowTapPosition, highTapPosition,
                                                             targetV, regulatedSide, lowV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        // Error <= -Min(UpIncrement, DownIncrement)
        assertFalse(TransformersValidation.checkTransformer("test", rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                                             targetV, regulatedSide, highV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(TransformersValidation.checkTransformer("test", rho, rhoPreviousStep, Float.NaN, highTapPosition, lowTapPosition, highTapPosition,
                                                             targetV, regulatedSide, highV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        // check NaN vales
        assertFalse(TransformersValidation.checkTransformer("test", rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                                             Float.NaN, regulatedSide, v, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(TransformersValidation.checkTransformer("test", rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                                             targetV, regulatedSide, Float.NaN, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkTwts() {
        assertTrue(TransformersValidation.checkTransformer(transformer, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(bus.getV()).thenReturn(highV);
        assertFalse(TransformersValidation.checkTransformer(transformer, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkTwts() {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getTwoWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(transformer));
        assertTrue(TransformersValidation.checkTransformers(network, strictConfig, NullWriter.NULL_WRITER));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), strictConfig, NullWriter.NULL_WRITER, ValidationType.TWTS);
        assertTrue(ValidationType.TWTS.check(network, strictConfig, validationWriter));
    }

}
