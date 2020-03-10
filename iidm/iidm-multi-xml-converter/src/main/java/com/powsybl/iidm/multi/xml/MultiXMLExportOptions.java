/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.multi.xml;

import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.export.ExportOptions;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class MultiXMLExportOptions extends ExportOptions {

    private IidmImportExportMode mode = IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE;

    public MultiXMLExportOptions setMode(IidmImportExportMode mode) {
        this.mode = mode;
        return this;
    }

    public IidmImportExportMode getMode() {
        return mode;
    }
}
