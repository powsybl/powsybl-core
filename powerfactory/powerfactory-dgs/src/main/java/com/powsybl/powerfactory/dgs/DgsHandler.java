/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface DgsHandler {

    void onTableHeader(String tableName);

    void onAttributeDescription(String attributeName, char attributeType);

    void onStringValue(String attributeName, String value);

    void onIntegerValue(String attributeName, int value);

    void onRealValue(String attributeName, float value);

    void onObjectValue(String attributeName, long id);
}
