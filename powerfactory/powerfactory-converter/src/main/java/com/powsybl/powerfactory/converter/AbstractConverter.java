/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import java.util.List;
import java.util.Objects;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.NodeRef;
import com.powsybl.powerfactory.model.DataObject;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractConverter {

    private final ImportContext importContext;

    private final Network network;

    AbstractConverter(ImportContext importContext, Network network) {
        this.importContext = Objects.requireNonNull(importContext);
        this.network = Objects.requireNonNull(network);
    }

    ImportContext getImportContext() {
        return importContext;
    }

    Network getNetwork() {
        return network;
    }

    static double microFaradToSiemens(double frnom, double capacitance) {
        return 2 * Math.PI * frnom * capacitance * 1.0e-6;
    }

    static double microSiemensToSiemens(double susceptance) {
        return susceptance * 1.0e-6;
    }

    TwoNodeRefs checkAndGetTwoNodeRefs(DataObject obj) {
        List<NodeRef> nodeRefs = importContext.objIdToNode.get(obj.getId());
        if (nodeRefs == null || nodeRefs.size() != 2) {
            throw new PowsyblException("Inconsistent number (" + (nodeRefs != null ? nodeRefs.size() : 0)
                    + ") of connection for '" + obj + "'");
        }
        return new TwoNodeRefs(nodeRefs.get(0), nodeRefs.get(1));
    }

    List<NodeRef> checkAndGetNodes(DataObject obj, int connections) {
        List<NodeRef> nodeRefs = importContext.objIdToNode.get(obj.getId());
        if (nodeRefs == null || nodeRefs.size() != connections) {
            throw new PowsyblException("Inconsistent number (" + (nodeRefs != null ? nodeRefs.size() : 0)
                    + ") of connection for '" + obj + "'");
        }
        return nodeRefs;
    }

    static class TwoNodeRefs {
        private final NodeRef end1;
        private final NodeRef end2;

        TwoNodeRefs(NodeRef end1, NodeRef end2) {
            this.end1 = end1;
            this.end2 = end2;
        }

        NodeRef getEnd1() {
            return end1;
        }

        NodeRef getEnd2() {
            return end2;
        }
    }
}
