/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.update;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.IncrementalIidmFiles;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class BusbarSectionUpdaterXml {

    private BusbarSectionUpdaterXml() { }

    public static void updateBusbarSectionStateValues(XMLStreamReader reader, VoltageLevel[] vl, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.STATE) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        double v = XmlUtil.readDoubleAttribute(reader, "v");
        double angle = XmlUtil.readDoubleAttribute(reader, "angle");
        BusbarSection bbs = vl[0].getNodeBreakerView().getBusbarSection(id);
        Bus b = bbs.getTerminal().getBusView().getBus();
        if (b != null) {
            b.setAngle(angle).setV(v > 0 ? v : Double.NaN);
        }
    }
}
