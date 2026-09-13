/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.systemconstraints;

import com.google.auto.service.AutoService;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.io.NcModelPostProcessor;
import com.powsybl.nc.model.io.NcQueryContext;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
@AutoService(NcModelPostProcessor.class)
public final class NcSystemConstraintsPostProcessor implements NcModelPostProcessor {
    @Override
    public String getName() {
        return NcSystemConstraints.NAME;
    }

    @Override
    public void process(NcModel model, NcQueryContext queryContext) {
        NcSystemConstraints extension = new NcSystemConstraintsImporter(
            new NcSystemConstraintsModel(queryContext)).importData();
        queryContext.addExtension(NcSystemConstraints.class, extension);
    }
}
