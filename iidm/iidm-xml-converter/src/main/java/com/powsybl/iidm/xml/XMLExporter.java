/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Exporter;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * XML export of an IIDM model.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(Exporter.class)
public class XMLExporter extends AbstractTreeDataExporter {

    public XMLExporter() {
        this(PlatformConfig.defaultConfig());
    }

    public XMLExporter(PlatformConfig platformConfig) {
        super(platformConfig);
    }

    @Override
    public String getFormat() {
        return "XIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM XML v" + CURRENT_IIDM_XML_VERSION.toString(".") + " exporter";
    }

    @Override
    protected TreeDataFormat getTreeDataFormat() {
        return TreeDataFormat.XML;
    }

    @Override
    protected String getExtension() {
        return "xiidm";
    }
}
