/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public enum CgmesSubset {
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
    EQUIPMENT("EQ") {
        @Override
        public boolean isValidName(String contextName) {
            return super.isValidName(contextName) && !isBoundary(contextName);
        }
    },
    TOPOLOGY("TP") {
        @Override
        public boolean isValidName(String contextName) {
            return super.isValidName(contextName) && !isBoundary(contextName);
        }
    },
    STATE_VARIABLES("SV"),
    STEADY_STATE_HYPOTHESIS("SSH"),
    DYNAMIC("DY"),
    DIAGRAM_LAYOUT("DL"),
    GEOGRAPHICAL_LOCATION("GL"),
    EQUIPMENT_BOUNDARY("EQ_BD") {
        @Override
        public boolean isValidName(String contextName) {
            return super.isValidName(contextName)
                    || EQUIPMENT.isValidName(contextName) && isBoundary(contextName);
        }
    },
    TOPOLOGY_BOUNDARY("TP_BD") {
        @Override
        public boolean isValidName(String contextName) {
            return super.isValidName(contextName)
                    || TOPOLOGY.isValidName(contextName) && isBoundary(contextName);
        }
    }, UNKNOWN("unknown") {
        @Override
        public boolean isValidName(String contextName) {
            return false;
        }
    };

    CgmesSubset(String identifier) {
        this.identifier = identifier;
        this.validName0 = "_" + identifier + "_";
        this.validName1 = "_" + identifier + ".";
    }

    /**
     * Get the identifier of a subset
     */
    public String getIdentifier() {
        return identifier;
    }

    public String getProfile() {
        return PROFILE.get(identifier);
    }

    public boolean isValidName(String contextName) {
        return contextName.contains(validName0) || contextName.contains(validName1);
    }

    private static boolean isBoundary(String contextName) {
        return contextName.contains("_BD") || contextName.contains("BOUNDARY");
    }

    private final String identifier;
    private final String validName0;
    private final String validName1;
    private static final Map<String, String> PROFILE = Map.of(
        "SV", "StateVariables",
        "EQ", "EquipmentCore",
        "SSH", "SteadyStateHypothesis",
        "TP", "Topology");

}
