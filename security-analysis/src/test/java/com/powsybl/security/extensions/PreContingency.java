/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.LimitViolation;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class PreContingency extends AbstractExtension<LimitViolation> {

    private final float value;

    public PreContingency(float value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "PreContingency";
    }

    public float getPreContingencyValue() {
        return value;
    }
}
