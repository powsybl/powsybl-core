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

public final class BranchUpdaterXml {

    public static void updateBranchTopoValues(XMLStreamReader reader, Network network, VoltageLevel[] vl, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.TOPO) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        String connectableBus1 = reader.getAttributeValue(null, "connectableBus1");
        String connectableBus2 = reader.getAttributeValue(null, "connectableBus2");
        Branch branch = (Branch) network.getIdentifiable(id);
        if (vl[0].getTopologyKind() == TopologyKind.BUS_BREAKER) {
            branch.getTerminal1().getBusBreakerView().setConnectableBus(connectableBus1);
            branch.getTerminal1().connect();
            branch.getTerminal2().getBusBreakerView().setConnectableBus(connectableBus2);
            branch.getTerminal2().connect();
        }
    }

    public static void updateBranchStateValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.STATE) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        double p1 = XmlUtil.readOptionalDoubleAttribute(reader, "p1");
        double q1 = XmlUtil.readOptionalDoubleAttribute(reader, "q1");
        double p2 = XmlUtil.readOptionalDoubleAttribute(reader, "p2");
        double q2 = XmlUtil.readOptionalDoubleAttribute(reader, "q2");
        Branch branch = (Branch) network.getIdentifiable(id);
        branch.getTerminal1().setP(p1).setQ(q1);
        branch.getTerminal2().setP(p2).setQ(q2);
    }
}
