/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.validation;

import com.google.auto.service.AutoService;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ValidationProvider.class)
public class DefaultValidationProvider implements ValidationProvider {

    private Validation cachedValidation;

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public Validation getValidation() {
        if (cachedValidation == null) {
            cachedValidation = new DefaultValidation();
        }
        return cachedValidation;
    }
}
