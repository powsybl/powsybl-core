/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter.version;

/**
 * @author Nicolas Pierre <nicolas.pierre at artelys.com>
 */
public enum AmplExportVersion {
    /**
     * Legacy export
     */
    V1(BasicAmplExporter.getFactory());

    private final AmplColumnsExporter.Factory factory;

    AmplExportVersion(AmplColumnsExporter.Factory factory) {
        this.factory = factory;
    }

    public AmplColumnsExporter.Factory getColumnsExporter() {
        return factory;
    }
}
