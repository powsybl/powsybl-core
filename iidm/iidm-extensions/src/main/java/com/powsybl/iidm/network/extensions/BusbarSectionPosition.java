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
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public interface BusbarSectionPosition extends Extension<BusbarSection> {

    String NAME = "busbarSectionPosition";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * The position of the busbar section relative to other busbar sections on the voltage level.
     */
    int getBusbarIndex();

    BusbarSectionPosition setBusbarIndex(int busbarIndex);

    /**
     * The position of the busbar section in its set relative to the other busbar section sets of the voltage
     * level seperated from them by switches.
     */
    int getSectionIndex();

    BusbarSectionPosition setSectionIndex(int sectionIndex);

}
