/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 * @author Thomas ADAM {@literal <tadam at silicom.fr>}
 */
public interface CoordinatedReactiveControl extends Extension<Generator> {

    String NAME = "coordinatedReactiveControl";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the percent of the coordinated reactive control that comes
     * from the generator to which this extension is linked.
     */
    double getQPercent();

    /**
     * Set the percent of the coordinated reactive control that comes
     * from the generator to which this extension is linked.
     */
    void setQPercent(double qPercent);
}
