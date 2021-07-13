/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.validation;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.validation.Validation;
import com.powsybl.iidm.network.validation.ValidationProvider;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ValidationProvider.class)
public class ReportValidationProvider implements ValidationProvider {

    @Override
    public String getName() {
        return "Report";
    }

    @Override
    public Validation getValidation() {
        return new ReportValidation();
    }
}
