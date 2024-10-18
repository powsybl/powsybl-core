/**
 * Copyright (c) 2024, Artelys (http://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class GeneratorConversionTest {

    @Test
    void generatingUnitTypes() {
        Network network = Network.read(new ResourceDataSource("GeneratingUnitTypes", new ResourceSet("/", "GeneratingUnitTypes.xml")));
        assertEquals(EnergySource.OTHER, network.getGenerator("gu_sm").getEnergySource());
        assertEquals(EnergySource.THERMAL, network.getGenerator("tgu_sm").getEnergySource());
        assertEquals(EnergySource.HYDRO, network.getGenerator("hgu_sm").getEnergySource());
        assertEquals(EnergySource.NUCLEAR, network.getGenerator("ngu_sm").getEnergySource());
        assertEquals(EnergySource.WIND, network.getGenerator("offshore_wgu_sm").getEnergySource());
        assertEquals("offshore", network.getGenerator("offshore_wgu_sm").getProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE));
        assertEquals(EnergySource.WIND, network.getGenerator("onshore_wgu_sm").getEnergySource());
        assertEquals("onshore", network.getGenerator("onshore_wgu_sm").getProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE));
        assertEquals(EnergySource.SOLAR, network.getGenerator("sgu_sm").getEnergySource());
    }
}
