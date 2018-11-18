/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.io.output.NullWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import com.powsybl.iidm.network.VoltageLevel;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ShuntCompensatorsValidationTest extends AbstractValidationTest {

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

    @Before
    public void setUp() {
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

        shunt = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shunt.getId()).thenReturn("shunt");
        Mockito.when(shunt.getTerminal()).thenReturn(shuntTerminal);
        Mockito.when(shunt.getCurrentSectionCount()).thenReturn(currentSectionCount);
        Mockito.when(shunt.getMaximumSectionCount()).thenReturn(maximumSectionCount);
        Mockito.when(shunt.getbPerSection()).thenReturn(bPerSection);

        Properties shuntProperties = new Properties();
        shuntProperties.put("qMax", Double.toString(qMax));
        Mockito.when(shunt.getProperties()).thenReturn(shuntProperties);
    }

    @Test
    public void checkShuntsValues() {
        // “p” is always NaN
        assertTrue(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = 1;
        assertFalse(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = Float.NaN;

        // “q” = - bPerSection * currentSectionCount * v^2
        assertTrue(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        q = 170.52;
        assertFalse(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        q = 171.52;
        assertFalse(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        // check main component
        mainComponent = false;
        assertTrue(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        mainComponent = true;
        q = 170.50537;

        // check with NaN values
        assertFalse(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, Float.NaN, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, Float.NaN, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, Float.NaN, v, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(ShuntCompensatorsValidation.checkShunts("test", p, q, currentSectionCount, maximumSectionCount, bPerSection, Float.NaN, qMax, nominalV, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkShunts() {
        // “q” = - bPerSection * currentSectionCount * v^2
        assertTrue(ShuntCompensatorsValidation.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(shuntTerminal.getQ()).thenReturn(171.52);
        assertFalse(ShuntCompensatorsValidation.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));

        // if the shunt is disconnected then either “q” is not defined or “q” is 0
        Mockito.when(shuntBusView.getBus()).thenReturn(null);
        assertFalse(ShuntCompensatorsValidation.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(shuntTerminal.getQ()).thenReturn(Double.NaN);
        assertTrue(ShuntCompensatorsValidation.checkShunts(shunt, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkShunts() {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getShuntCompensatorStream()).thenAnswer(dummy -> Stream.of(shunt));

        assertTrue(ShuntCompensatorsValidation.checkShunts(network, strictConfig, NullWriter.NULL_WRITER));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), strictConfig, NullWriter.NULL_WRITER, ValidationType.SHUNTS);
        assertTrue(ValidationType.SHUNTS.check(network, strictConfig, validationWriter));
    }
}
