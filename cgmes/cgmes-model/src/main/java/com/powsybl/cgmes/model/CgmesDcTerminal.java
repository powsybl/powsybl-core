/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class CgmesDcTerminal {

    public CgmesDcTerminal(PropertyBag t) {
        Objects.requireNonNull(t);

        this.id = t.getId(CgmesNames.DC_TERMINAL);
        this.name = t.get("name");
        this.dcConductingEquipment = t.getId("DCConductingEquipment");
        this.dcConductingEquipmentType = t.getLocal("dcConductingEquipmentType");
        this.connected = t.asBoolean("connected", false);
        this.dcNode = t.getId("DCNode");
        this.dcTopologicalNode = t.getId("DCTopologicalNode");
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String dcConductingEquipment() {
        return dcConductingEquipment;
    }

    public String dcConductingEquipmentType() {
        return dcConductingEquipmentType;
    }

    public boolean connected() {
        return connected;
    }

    public String dcNode() {
        return dcNode;
    }

    public String dcTopologicalNode() {
        return dcTopologicalNode;
    }

    private final String id;
    private final String name;
    private final String dcConductingEquipment;
    private final String dcConductingEquipmentType;
    private final boolean connected;
    private final String dcNode;
    private final String dcTopologicalNode;
}
