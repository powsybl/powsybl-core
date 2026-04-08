/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.dgs;

import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;

import com.powsybl.powerfactory.model.DataAttributeType;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface DgsHandler {

    void onGeneralAttribute(String descr, String val);

    void onObjectTableHeader(String tableName);

    void onAttributeDescription(String attributeName, DataAttributeType attributeType);

    void onID(long id);

    void onStringValue(String attributeName, String value);

    void onIntegerValue(String attributeName, int value);

    void onRealValue(String attributeName, float value);

    void onObjectValue(String attributeName, long id);

    void onDoubleMatrixValue(String attributeName, RealMatrix value);

    void onStringVectorValue(String attributeName, List<String> values);

    void onIntVectorValue(String attributeName, List<Integer> values);

    void onDoubleVectorValue(String attributeName, List<Double> values);

    void onObjectVectorValue(String attributeName, List<Long> ids);
}
