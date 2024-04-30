/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesGLImportPostProcessorTest {

    @Test
    void process() {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", "cgmesGLImport");
        ReadOnlyDataSource ds = new ResourceDataSource("importGL", new ResourceSet("/", "importGL_EQ.xml", "importGL_GL.xml"));
        Network network = Network.read(ds, properties);

        Substation substation1 = network.getSubstation("Substation1");
        SubstationPosition substation1Position = substation1.getExtension(SubstationPosition.class);
        assertEquals(48.7123755, substation1Position.getCoordinate().getLatitude(), 0);
        assertEquals(-1.8468419, substation1Position.getCoordinate().getLongitude(), 0);

        Substation substation2 = network.getSubstation("Substation2");
        SubstationPosition substation2Position = substation2.getExtension(SubstationPosition.class);
        assertEquals(49.224448, substation2Position.getCoordinate().getLatitude(), 0);
        assertEquals(-2.136596, substation2Position.getCoordinate().getLongitude(), 0);

        Line line = network.getLine("ACLine");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertEquals(48.7123755, linePosition.getCoordinates().get(0).getLatitude(), 0);
        assertEquals(-1.8468419, linePosition.getCoordinates().get(0).getLongitude(), 0);

        assertEquals(48.8725425, linePosition.getCoordinates().get(1).getLatitude(), 0);
        assertEquals(-1.8330389, linePosition.getCoordinates().get(1).getLongitude(), 0);

        assertEquals(49.1703925, linePosition.getCoordinates().get(2).getLatitude(), 0);
        assertEquals(-2.1263669, linePosition.getCoordinates().get(2).getLongitude(), 0);

        assertEquals(49.224448, linePosition.getCoordinates().get(3).getLatitude(), 0);
        assertEquals(-2.136596, linePosition.getCoordinates().get(3).getLongitude(), 0);

    }

}
