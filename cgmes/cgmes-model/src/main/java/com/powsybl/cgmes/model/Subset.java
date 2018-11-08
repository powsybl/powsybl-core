/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public enum Subset {
    // TODO Each subset has a "main" profile URL plus additional optional profiles
    // DL http://entsoe.eu/CIM/DiagramLayout/3/1
    // DY http://entsoe.eu/CIM/Dynamics/3/1
    // EQ http://entsoe.eu/CIM/EquipmentCore/3/1
    // EQ http://entsoe.eu/CIM/EquipmentOperation/3/1 (node-breaker)
    // EQ http://entsoe.eu/CIM/EquipmentShortCircuit/3/1
    // EQ_BD http://entsoe.eu/CIM/EquipmentBoundary/3/1
    // EQ_BD http://entsoe.eu/CIM/EquipmentBoundaryOperation/3/1 (node-breaker)
    // GL http://entsoe.eu/CIM/GeographicalLocation/2/1
    // SV http://entsoe.eu/CIM/StateVariables/4/1
    // SSH http://entsoe.eu/CIM/SteadyStateHypothesis/1/1
    // TP http://entsoe.eu/CIM/Topology/4/1
    // TP_BD http://entsoe.eu/CIM/TopologyBoundary/3/1
    EQUIPMENT("EQ"),
    TOPOLOGY("TP"),
    STATE_VARIABLES("SV"),
    STEADY_STATE_HYPOTHESIS("SSH"),
    DYNAMIC("DY"),
    DIAGRAM_LAYOUT("DL"),
    GEOGRAPHICAL_LOCATION("GL");

    private final String identifier;

    Subset(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Get the identifier of a subset
     */
    public String getIdentifier() {
        return identifier;
    }

    public boolean isValidName(String contextName) {
        return contextName.contains("_" + identifier);
    }
}
