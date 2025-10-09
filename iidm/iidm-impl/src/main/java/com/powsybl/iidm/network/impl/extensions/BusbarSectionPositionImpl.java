/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BusbarSectionPositionImpl extends AbstractExtension<BusbarSection> implements BusbarSectionPosition {

    private int busbarIndex;

    private int sectionIndex;

    private static int checkIndex(int index, BusbarSection busbarSection) {
        if (index < 0) {
            throw new IllegalArgumentException(
                String.format("Busbar index (%s) has to be greater or equals to zero for busbar section %s",
                    index,
                    busbarSection.getId()));
        }
        return index;
    }

    public BusbarSectionPositionImpl(BusbarSection busbarSection, int busbarIndex, int sectionIndex) {
        super(busbarSection);
        this.busbarIndex = checkIndex(busbarIndex, busbarSection);
        this.sectionIndex = checkIndex(sectionIndex, busbarSection);
    }

    public int getBusbarIndex() {
        return busbarIndex;
    }

    public BusbarSectionPositionImpl setBusbarIndex(int busbarIndex) {
        this.busbarIndex = checkIndex(busbarIndex, this.getExtendable());
        return this;
    }

    public int getSectionIndex() {
        return sectionIndex;
    }

    public BusbarSectionPositionImpl setSectionIndex(int sectionIndex) {
        this.sectionIndex = checkIndex(sectionIndex, this.getExtendable());
        return this;
    }
}
