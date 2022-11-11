/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.BusbarSection;

/**
 * Position information about the BusbarSection
 * <ul>
 *   <li> within the corresponding busbar ({@link BusbarSectionPosition#getSectionIndex})</li>
 *   <li> relative to other busbars of the voltage level ({@link BusbarSectionPosition#getBusbarIndex})</li>
 * </ul><p>
 * Note that a busbar is a set of BusbarSections.
 * Hence, the BusbarSections of a same busbar should have the same busbar index.
 * The busbar indices induce an order of busbars within the voltage level, which usually reflects the busbars physical relative positions.
 * <p>
 * Similarly, the section indices induce an order of BusbarSections of a same busbar, which usually reflects their physical relative position.
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public interface BusbarSectionPosition extends Extension<BusbarSection> {

    String NAME = "busbarSectionPosition";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Return the index of the corresponding busbar among the busbars of the voltage level
     */
    int getBusbarIndex();

    BusbarSectionPosition setBusbarIndex(int busbarIndex);

    /**
     * Return the index of the busbar section within the corresponding busbar
     */
    int getSectionIndex();

    BusbarSectionPosition setSectionIndex(int sectionIndex);

}
