/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Substation;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class SubstationShutdownExtension extends AbstractExtension<Substation> {

    @Override
    public String getName() {
        return "subShutdownExt";
    }

    public void shutdown() {
        System.out.println("shut down");
    }
}
