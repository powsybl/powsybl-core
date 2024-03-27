/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.BusbarSectionTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class BusbarSectionContingency implements ContingencyElement {

    private final String busbarSectionId;

    public BusbarSectionContingency(String busbarSectionId) {
        this.busbarSectionId = Objects.requireNonNull(busbarSectionId);
    }

    @Override
    public String getId() {
        return busbarSectionId;
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BUSBAR_SECTION;
    }

    @Override
    public Tripping toModification() {
        return new BusbarSectionTripping(busbarSectionId);
    }

    @Override
    public int hashCode() {
        return busbarSectionId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BusbarSectionContingency other) {
            return busbarSectionId.equals(other.busbarSectionId);
        }
        return false;
    }
}
