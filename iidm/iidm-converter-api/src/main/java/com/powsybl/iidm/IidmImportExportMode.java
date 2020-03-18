/**
 * Copyright (c) 2019, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm;

/***
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */

public enum IidmImportExportMode {
    ONE_SEPARATED_FILE_PER_EXTENSION_TYPE,
    EXTENSIONS_IN_ONE_SEPARATED_FILE,

    /**
     * @deprecated Not used anymore: this mode is only used for multi-files XML import/export
     */
    @Deprecated
    UNIQUE_FILE
}


