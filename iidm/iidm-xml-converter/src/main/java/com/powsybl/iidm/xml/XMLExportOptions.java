/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.TopologyLevel;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 *
 */
public class XMLExportOptions extends ExportOptions {

    private boolean indent;

    public XMLExportOptions() {
    }

    public XMLExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel) {
        super(withBranchSV, onlyMainCc, topologyLevel);
        this.indent = indent;
    }

    public boolean isIndent() {
        return indent;
    }

    public XMLExportOptions setIndent(boolean indent) {
        this.indent = indent;
        return this;
    }
}
