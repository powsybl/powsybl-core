/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractIdentifiableTypeTest {

    @Test
    public void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        assertSame(IdentifiableType.NETWORK, network.getType());

        assertSame(IdentifiableType.SUBSTATION, network.getSubstation("S1").getType());

        assertSame(IdentifiableType.VOLTAGE_LEVEL, network.getVoltageLevel("S1VL1").getType());

        assertSame(IdentifiableType.HVDC_LINE, network.getHvdcLine("HVDC1").getType());

        assertSame(IdentifiableType.SWITCH, network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR").getType());

        assertSame(IdentifiableType.BUSBAR_SECTION, network.getBusbarSection("S1VL1_BBS").getType());

        assertSame(IdentifiableType.LINE, network.getLine("LINE_S2S3").getType());

        assertSame(IdentifiableType.TWO_WINDINGS_TRANSFORMER, network.getTwoWindingsTransformer("TWT").getType());

        assertSame(IdentifiableType.GENERATOR, network.getGenerator("GH1").getType());

        assertSame(IdentifiableType.LOAD, network.getLoad("LD1").getType());

        assertSame(IdentifiableType.SHUNT_COMPENSATOR, network.getShuntCompensator("SHUNT").getType());

        assertSame(IdentifiableType.STATIC_VAR_COMPENSATOR, network.getStaticVarCompensator("SVC").getType());

        assertSame(IdentifiableType.HVDC_CONVERTER_STATION, network.getHvdcConverterStation("VSC1").getType());
    }
}
