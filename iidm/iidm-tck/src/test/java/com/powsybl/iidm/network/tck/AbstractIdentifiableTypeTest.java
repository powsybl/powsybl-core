/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractIdentifiableTypeTest {

    @Test
    public void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        assertSame(IdentifiableType.NETWORK, network.getIdentifiableType());
        assertFalse(network.getIdentifiableType().getConnectableType().isPresent());

        assertSame(IdentifiableType.SUBSTATION, network.getSubstation("S1").getIdentifiableType());
        assertFalse(network.getSubstation("S1").getIdentifiableType().getConnectableType().isPresent());

        assertSame(IdentifiableType.VOLTAGE_LEVEL, network.getVoltageLevel("S1VL1").getIdentifiableType());
        assertFalse(network.getVoltageLevel("S1VL1").getIdentifiableType().getConnectableType().isPresent());

        assertSame(IdentifiableType.HVDC_LINE, network.getHvdcLine("HVDC1").getIdentifiableType());
        assertFalse(network.getHvdcLine("HVDC1").getIdentifiableType().getConnectableType().isPresent());

        assertSame(IdentifiableType.SWITCH, network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR").getIdentifiableType());
        assertFalse(network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR").getIdentifiableType().getConnectableType().isPresent());

        assertSame(IdentifiableType.BUSBAR_SECTION, network.getBusbarSection("S1VL1_BBS").getIdentifiableType());
        assertSame(ConnectableType.BUSBAR_SECTION, network.getBusbarSection("S1VL1_BBS").getIdentifiableType().getConnectableType().orElseThrow());

        assertSame(IdentifiableType.LINE, network.getLine("LINE_S2S3").getIdentifiableType());
        assertSame(ConnectableType.LINE, network.getLine("LINE_S2S3").getIdentifiableType().getConnectableType().orElseThrow());

        assertSame(IdentifiableType.TWO_WINDINGS_TRANSFORMER, network.getTwoWindingsTransformer("TWT").getIdentifiableType());
        assertSame(ConnectableType.TWO_WINDINGS_TRANSFORMER, network.getTwoWindingsTransformer("TWT").getIdentifiableType().getConnectableType().orElseThrow());

        assertSame(IdentifiableType.GENERATOR, network.getGenerator("GH1").getIdentifiableType());
        assertSame(ConnectableType.GENERATOR, network.getGenerator("GH1").getIdentifiableType().getConnectableType().orElseThrow());

        assertSame(IdentifiableType.LOAD, network.getLoad("LD1").getIdentifiableType());
        assertSame(ConnectableType.LOAD, network.getLoad("LD1").getIdentifiableType().getConnectableType().orElseThrow());

        assertSame(IdentifiableType.SHUNT_COMPENSATOR, network.getShuntCompensator("SHUNT").getIdentifiableType());
        assertSame(ConnectableType.SHUNT_COMPENSATOR, network.getShuntCompensator("SHUNT").getIdentifiableType().getConnectableType().orElseThrow());

        assertSame(IdentifiableType.STATIC_VAR_COMPENSATOR, network.getStaticVarCompensator("SVC").getIdentifiableType());
        assertSame(ConnectableType.STATIC_VAR_COMPENSATOR, network.getStaticVarCompensator("SVC").getIdentifiableType().getConnectableType().orElseThrow());

        assertSame(IdentifiableType.HVDC_CONVERTER_STATION, network.getHvdcConverterStation("VSC1").getIdentifiableType());
        assertSame(ConnectableType.HVDC_CONVERTER_STATION, network.getHvdcConverterStation("VSC1").getIdentifiableType().getConnectableType().orElseThrow());
    }
}
