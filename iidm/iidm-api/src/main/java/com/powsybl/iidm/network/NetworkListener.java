/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.extensions.Extension;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface NetworkListener {

    void onCreation(Identifiable<?> identifiable);

    void beforeRemoval(Identifiable<?> identifiable);

    void afterRemoval(String id);

    void onUpdate(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue);

    void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue);

    void onExtensionCreation(Extension<?> extension);

    void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName);

    void onExtensionBeforeRemoval(Extension<?> extension);

    void onExtensionUpdate(Extension<?> extendable, String attribute, Object oldValue, Object newValue);

    default void onElementAdded(Identifiable<?> identifiable, String attribute, Object newValue) {
        // empty default implementation
    }

    default void onElementReplaced(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        // empty default implementation
    }

    default void onElementRemoved(Identifiable<?> identifiable, String attribute, Object oldValue) {
        // empty default implementation
    }

    default void onVariantCreated(String sourceVariantId, String targetVariantId) {
        // empty default implementation
    }

    default void onVariantOverwritten(String sourceVariantId, String targetVariantId) {
        // empty default implementation
    }

    default void onVariantRemoved(String variantId) {
        // empty default implementation
    }
}
