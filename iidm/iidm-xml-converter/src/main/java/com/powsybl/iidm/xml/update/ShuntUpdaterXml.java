/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.update;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.xml.IncrementalIidmFiles;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class ShuntUpdaterXml {

    private ShuntUpdaterXml() { }

    public static void updateShuntControlValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.CONTROL) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        double currentSectionCount = XmlUtil.readOptionalDoubleAttribute(reader, "currentSectionCount");
        ShuntCompensator sc = (ShuntCompensator) network.getIdentifiable(id);
        sc.setCurrentSectionCount((int) currentSectionCount);
    }
}
