/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TreeDataReader {

    interface EventHandler {

        void onStartElement();
    }

    /**
     * An richer event handler which give element depth with each start event.
     */
    interface EventHandlerWithDepth {

        void onStartElement(int elementDepth);
    }

    double readDoubleAttribute(String name);

    double readDoubleAttribute(String name, double defaultValue);

    float readFloatAttribute(String name);

    float readFloatAttribute(String name, float defaultValue);

    String readStringAttribute(String name);

    Integer readIntAttribute(String name);

    int readIntAttribute(String name, int defaultValue);

    Boolean readBooleanAttribute(String name);

    boolean readBooleanAttribute(String name, boolean defaultValue);

    <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz);

    <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue);

    String getNodeName();

    String readContent();

    String readUntilEndNode(String endElementName, EventHandler eventHandler);

    String readUntilEndNodeWithDepth(String endElementName, EventHandlerWithDepth eventHandler);
}
