/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.BusRef;
import com.powsybl.iidm.network.IdBasedBusRef;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusVoltageTest {

    private static final String FUNCTION_ID = "Function ID";
    private static final String FUNCTION_NAME = "Function name";
    private static final BusRef BUS_REF = mock(BusRef.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullTerminalId() {
        exception.expect(NullPointerException.class);
        new BusVoltage(FUNCTION_ID, FUNCTION_NAME, null);
    }

    @Test
    public void getName() {
        BusVoltage busVoltage = new BusVoltage(FUNCTION_ID, FUNCTION_NAME, BUS_REF);
        Assert.assertEquals(FUNCTION_NAME, busVoltage.getName());
    }

    @Test
    public void getId() {
        BusVoltage busVoltage = new BusVoltage(FUNCTION_ID, FUNCTION_NAME, BUS_REF);
        Assert.assertEquals(FUNCTION_ID, busVoltage.getId());
    }

    @Test
    public void getBranchId() throws JsonProcessingException {
        IdBasedBusRef busRef = new IdBasedBusRef("branchId", Branch.Side.ONE);
        BusVoltage busVoltage = new BusVoltage(FUNCTION_ID, FUNCTION_NAME, busRef);
        assertEquals(busRef, busVoltage.getBusRef());
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busVoltage);
        String expectedJson = "{\"@c\":\".BusVoltage\",\"id\":\"Function ID\",\"name\":\"Function name\",\"busRef\":{\"@c\":\".IdBasedBusRef\",\"id\":\"branchId\",\"side\":\"ONE\"}}";
        assertEquals(expectedJson, json);
        final BusVoltage deserialized = objectMapper.readValue(expectedJson, BusVoltage.class);
        assertEquals(busVoltage, deserialized);
    }

}
