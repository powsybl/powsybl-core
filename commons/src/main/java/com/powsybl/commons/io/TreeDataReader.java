/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TreeDataReader extends AutoCloseable {

    @FunctionalInterface
    interface ChildNodeReader {

        /**
         * The implementations must read the full node corresponding to the given node name
         * (including the corresponding end node if any)
         * @param nodeName field name of the started child node
         */
        void onStartNode(String nodeName);
    }

    /**
     * Read the file header, among which the serialization versions
     * @return the {@link TreeDataHeader} containing the serialization versions
     */
    TreeDataHeader readHeader();

    double readDoubleAttribute(String name);

    double readDoubleAttribute(String name, double defaultValue);

    OptionalDouble readOptionalDoubleAttribute(String name);

    float readFloatAttribute(String name);

    float readFloatAttribute(String name, float defaultValue);

    String readStringAttribute(String name);

    int readIntAttribute(String name);

    OptionalInt readOptionalIntAttribute(String name);

    int readIntAttribute(String name, int defaultValue);

    boolean readBooleanAttribute(String name);

    boolean readBooleanAttribute(String name, boolean defaultValue);

    Optional<Boolean> readOptionalBooleanAttribute(String name);

    <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz);

    <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue);

    String readContent();

    List<Integer> readIntArrayAttribute(String name);

    List<String> readStringArrayAttribute(String name);

    /**
     * Skip everything contained in current node, from the current parser position to the end node (if any)
     */
    void skipNode();

    /**
     * Read the child nodes from the current parser position to the end node (if any).
     * Throws an {@link com.powsybl.commons.PowsyblException} if encountering a scalar attribute.
     */
    void readChildNodes(ChildNodeReader childNodeReader);

    /**
     * Read the end node at current parser position.
     * Throws an {@link com.powsybl.commons.PowsyblException} if encountering anything else than an end node.
     */
    void readEndNode();

    @Override
    void close();
}
