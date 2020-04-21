/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

import com.powsybl.commons.datastore.DataFormat;
import com.powsybl.commons.datastore.DataResolver;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class XmlDataFormat implements DataFormat {

    @Override
    public String getId() {
        return "XIIDM";
    }

    @Override
    public String getDescription() {
        return "IIDM XML v " + CURRENT_IIDM_XML_VERSION.toString(".") + " format";

    }

    @Override
    public DataResolver getDataResolver() {
        return new XmlDataResolver();
    }

}
