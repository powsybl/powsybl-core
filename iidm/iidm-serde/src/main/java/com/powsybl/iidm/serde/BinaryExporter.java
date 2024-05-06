/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Exporter;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * Binary export of an IIDM model.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(Exporter.class)
public class BinaryExporter extends AbstractTreeDataExporter {

    public BinaryExporter() {
        this(PlatformConfig.defaultConfig());
    }

    public BinaryExporter(PlatformConfig platformConfig) {
        super(platformConfig);
    }

    @Override
    public String getFormat() {
        return "BIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM binary v" + CURRENT_IIDM_VERSION.toString(".") + " exporter";
    }

    @Override
    protected TreeDataFormat getTreeDataFormat() {
        return TreeDataFormat.BIN;
    }

    @Override
    protected String getExtension() {
        return "biidm";
    }
}
