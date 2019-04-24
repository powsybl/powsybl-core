/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.update;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.IncrementalIidmFiles;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class SwitchUpdaterXml {
    public static void updateSwitchTopoValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.TOPO) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        boolean open = XmlUtil.readBoolAttribute(reader, "open");
        Switch sw = (Switch) network.getIdentifiable(id);
        sw.setOpen(open);
    }
}
