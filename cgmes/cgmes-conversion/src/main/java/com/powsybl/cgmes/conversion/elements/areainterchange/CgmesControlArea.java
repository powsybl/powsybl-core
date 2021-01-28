/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.elements.areainterchange;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesControlArea {
    private final String id;
    private final String name;
    private final String energyIdentCodeEic;
    private final Set<EquipmentEnd> equipmentEnds = new HashSet<>();
    private final double netInterchange;

    public CgmesControlArea(String id, String name, String energyIdentCodeEic, double netInterchange) {
        this.id = id;
        this.name = name;
        this.energyIdentCodeEic = energyIdentCodeEic;
        this.netInterchange = netInterchange;
    }

    public void addTerminal(String equipmentId, int end) {
        equipmentEnds.add(new EquipmentEnd(equipmentId, end));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEnergyIdentCodeEic() {
        return energyIdentCodeEic;
    }

    public Set<EquipmentEnd> getTerminals() {
        return equipmentEnds;
    }

    public double getNetInterchange() {
        return netInterchange;
    }

    public static class EquipmentEnd {
        EquipmentEnd(String equipmentId, int end) {
            this.equipmentId = Objects.requireNonNull(equipmentId);
            this.end = end;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public int getEnd() {
            return end;
        }

        private final String equipmentId;
        private final int end;
    }
}
