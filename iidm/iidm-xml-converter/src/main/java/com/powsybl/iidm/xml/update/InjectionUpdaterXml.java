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

public final class InjectionUpdaterXml {

    private InjectionUpdaterXml() { }

    public static void updateInjectionTopoValues(XMLStreamReader reader, Network network, VoltageLevel[] vl, IncrementalIidmFiles targetFile) {
        if (targetFile == IncrementalIidmFiles.TOPO) {
            String id = reader.getAttributeValue(null, "id");
            String connectableBus = reader.getAttributeValue(null, "connectableBus");
            Injection inj = (Injection) network.getIdentifiable(id);
            if (vl[0].getTopologyKind() == TopologyKind.BUS_BREAKER) {
                inj.getTerminal().getBusBreakerView().setConnectableBus(connectableBus);
                inj.getTerminal().connect();
            }
        }
    }

    public static void updateInjectionStateValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile == IncrementalIidmFiles.STATE) {
            String id = reader.getAttributeValue(null, "id");
            double p = XmlUtil.readOptionalDoubleAttribute(reader, "p");
            double q = XmlUtil.readOptionalDoubleAttribute(reader, "q");
            Injection inj = (Injection) network.getIdentifiable(id);
            inj.getTerminal().setP(p).setQ(q);
        }
    }
}
