/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import java.util.Map;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public interface ExtensionSeriesSerializer<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E> {

    int STRING_SERIES_TYPE = 0;
    int DOUBLE_SERIES_TYPE = 1;
    int INT_SERIES_TYPE = 2;
    int BOOLEAN_SERIES_TYPE = 3;

    /**
     * @param builder the extensions holder to write into
     */
    void serialize(ExtensionSeriesBuilder<?, T> builder);

    /**
     * Update extension double value
     * @param element the element has the extension to update
     * @param name field name of extension
     * @param value
     */
    void deserialize(T element, String name, double value);

    /**
     * Update extension int value
     * @param element the element has the extension to update
     * @param name field name of extension
     * @param value
     */
    void deserialize(T element, String name, int value);

    /**
     * Update extension string value
     * @param element the element has the extension to update
     * @param name field name of extension
     * @param value
     */
    void deserialize(T element, String name, String value);

    /**
     * extension filed name as key, and value type as value
     * @return
     */
    Map<String, Integer> getTypeMap();

}
