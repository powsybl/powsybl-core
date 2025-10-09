/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.powsybl.triplestore.api.PropertyBag;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class CgmesTerminal {

    public static String topologicalNode(PropertyBag t) {
        String tp = t.getId("TopologicalNodeTerminal");
        if (tp == null) {
            tp = t.getId("TopologicalNodeConnectivityNode");
        }
        return tp;
    }

    public CgmesTerminal(PropertyBag t) {
        Objects.requireNonNull(t);

        this.id = t.getId(CgmesNames.TERMINAL);
        this.name = t.get("name");
        this.conductingEquipment = t.getId("ConductingEquipment");
        this.conductingEquipmentType = t.getLocal("conductingEquipmentType");
        this.connectivityNode = t.getId("ConnectivityNode");
        this.topologicalNode = topologicalNode(t);

        // A TopologicalNode can be obtained from a Terminal using
        // relationship Terminal.TopologicalNode or
        // Terminal.ConnectivityNode.TopologicalNode

        // In some cases the two TopologicalNode's obtained through these
        // two relationships may be different
        // (TODO(Luma): missing reference here)

        this.connected = t.asBoolean("connected", true);
        this.flow = new PowerFlow(t, "p", "q");

        this.sequenceNumber = t.asInt("sequenceNumber", 0);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String conductingEquipment() {
        return conductingEquipment;
    }

    public String conductingEquipmentType() {
        return conductingEquipmentType;
    }

    public String connectivityNode() {
        return connectivityNode;
    }

    public String topologicalNode() {
        return topologicalNode;
    }

    public boolean connected() {
        return connected;
    }

    public PowerFlow flow() {
        return flow;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    private final String id;
    private final String name;
    private final String conductingEquipment;
    private final String conductingEquipmentType;
    private final boolean connected;
    private final PowerFlow flow;
    private final String connectivityNode;
    private final String topologicalNode;
    private final int sequenceNumber;
}
