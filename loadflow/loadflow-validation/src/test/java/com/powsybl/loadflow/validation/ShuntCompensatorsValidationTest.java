/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.stream.Stream;

import com.powsybl.iidm.network.*;
import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class ShuntCompensatorsValidationTest extends AbstractValidationTest {

    private double q = 170.50537;
    private double p = Float.NaN;
    private int currentSectionCount = 1;
    private final int maximumSectionCount = 1;
    private final double bPerSection = -0.0010387811;
    private final double v = 405.14175;
    private final double qMax = -150;
    private final double nominalV = 380;
    private final boolean connected = true;
    private boolean mainComponent = true;

    private ShuntCompensator shunt;
    private Terminal shuntTerminal;
    private BusView shuntBusView;

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();

        Bus shuntBus = Mockito.mock(Bus.class);
        Mockito.when(shuntBus.getV()).thenReturn(v);
        Mockito.when(shuntBus.isInMainConnectedComponent()).thenReturn(mainComponent);

        shuntBusView = Mockito.mock(BusView.class);
        Mockito.when(shuntBusView.getBus()).thenReturn(shuntBus);
        Mockito.when(shuntBusView.getConnectableBus()).thenReturn(shuntBus);

        VoltageLevel shuntVoltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(shuntVoltageLevel.getNominalV()).thenReturn(nominalV);

        shuntTerminal = Mockito.mock(Terminal.class);
        Mockito.when(shuntTerminal.getP()).thenReturn(p);
        Mockito.when(shuntTerminal.getQ()).thenReturn(q);
        Mockito.when(shuntTerminal.getBusView()).thenReturn(shuntBusView);
        Mockito.when(shuntTerminal.getVoltageLevel()).thenReturn(shuntVoltageLevel);

        ShuntCompensatorLinearModel shuntModel = Mockito.mock(ShuntCompensatorLinearModel.class);
        Mockito.when(shuntModel.getBPerSection()).thenReturn(bPerSection);

        shunt = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shunt.getId()).thenReturn("shunt");
        Mockito.when(shunt.getTerminal()).thenReturn(shuntTerminal);
        Mockito.when(shunt.getSectionCount()).thenReturn(currentSectionCount);
        Mockito.when(shunt.getMaximumSectionCount()).thenReturn(maximumSectionCount);
        Mockito.when(shunt.getProperty("qMax")).thenReturn(Double.toString(qMax));
        Mockito.when(shunt.getModelType()).thenReturn(ShuntCompensatorModelType.LINEAR);
        Mockito.when(shunt.getModel()).thenReturn(shuntModel);
        Mockito.when(shunt.getModel(ShuntCompensatorLinearModel.class)).thenReturn(shuntModel);
    }

    @Test
    void checkShuntsValues() {
        // “p” is always NaN
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = 1;
        assertFalse(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = Float.NaN;

        // “q” = - bPerSection * currentSectionCount * v^2
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        q = 170.52;
        assertFalse(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        q = 171.52;
        assertFalse(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        // check main component
        mainComponent = false;
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        mainComponent = true;
        q = 170.50537;

        // check with NaN values
        assertFalse(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, Float.NaN, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, Float.NaN, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, Float.NaN, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, Float.NaN, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    void checkShunts() {
        // “q” = - bPerSection * currentSectionCount * v^2
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(shuntTerminal.getQ()).thenReturn(171.52);
        assertFalse(ShuntCompensatorsValidation.INSTANCE.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));

        // if the shunt is disconnected then either “q” is not defined or “q” is 0
        Mockito.when(shuntBusView.getBus()).thenReturn(null);
        assertFalse(ShuntCompensatorsValidation.INSTANCE.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(shuntTerminal.getQ()).thenReturn(Double.NaN);
        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    void checkNetworkShunts() throws IOException {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getShuntCompensatorStream()).thenAnswer(dummy -> Stream.of(shunt));

        assertTrue(ShuntCompensatorsValidation.INSTANCE.checkShunts(network, strictConfig, data));

        assertTrue(ValidationType.SHUNTS.check(network, strictConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), strictConfig, NullWriter.NULL_WRITER, ValidationType.SHUNTS);
        assertTrue(ValidationType.SHUNTS.check(network, strictConfig, validationWriter));
    }
}
