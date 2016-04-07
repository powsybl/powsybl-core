/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo.parser;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopoBus {

    private final Set<String> equipments;

    private final String substation;

    public TopoBus(Set<String> equipments, String substation) {
        this.equipments = equipments;
        this.substation = substation;
    }

    public Set<String> getEquipments() {
        return equipments;
    }

    public String getSubstation() {
        return substation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopoBus topoBus = (TopoBus) o;

        if (!equipments.equals(topoBus.equipments)) return false;
        if (!substation.equals(topoBus.substation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = equipments.hashCode();
        result = 31 * result + substation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return equipments.toString();
    }
}