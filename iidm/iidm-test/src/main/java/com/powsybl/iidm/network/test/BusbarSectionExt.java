/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.BusbarSection;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class BusbarSectionExt extends AbstractExtension<BusbarSection> {

    public BusbarSectionExt(BusbarSection busbarSection) {
        super(busbarSection);
    }

    @Override
    public String getName() {
        return "busbarSectionExt";
    }
}
