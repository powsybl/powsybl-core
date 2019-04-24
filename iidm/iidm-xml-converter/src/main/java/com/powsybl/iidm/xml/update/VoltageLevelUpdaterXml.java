/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.update;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class VoltageLevelUpdaterXml {

    private VoltageLevelUpdaterXml() { }

    public static void updateVoltageLevel(XMLStreamReader reader, Network network, VoltageLevel[] vl) {
        String id = reader.getAttributeValue(null, "id");
        vl[0] = network.getVoltageLevel(id);
        if (vl[0] == null) {
            throw new PowsyblException("Voltage level '" + id + "' not found");
        }
    }
}
