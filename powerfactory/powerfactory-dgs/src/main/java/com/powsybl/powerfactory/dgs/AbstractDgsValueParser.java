/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.DataAttributeType;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
abstract class AbstractDgsValueParser implements DgsValueParser {

    protected final String attributeName;
    protected final DataAttributeType attributeType;
    protected final int indexField;

    protected AbstractDgsValueParser(String attributeName, DataAttributeType attributeType, int indexField) {
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.indexField = indexField;
    }

    protected static boolean isValidField(String[] fields, int index) {
        return fields.length > index && !fields[index].isEmpty();
    }
}
