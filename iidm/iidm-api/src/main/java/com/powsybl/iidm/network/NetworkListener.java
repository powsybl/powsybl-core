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

    default void onCreation(Identifiable<?> identifiable) {
        // empty default implementation
    }

    default void beforeRemoval(Identifiable<?> identifiable) {
        // empty default implementation
    }

    default void afterRemoval(String id) {
        // empty default implementation
    }

    default void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        // empty default implementation
    }

    default void onExtensionCreation(Extension<?> extension) {
        // empty default implementation
    }

    default void onExtensionUpdate(Extension<?> extension, String attribute, String variantId, Object oldValue, Object newValue) {
        // empty default implementation
    }

    default void onExtensionBeforeRemoval(Extension<?> extension) {
        // empty default implementation
    }

    default void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        // empty default implementation
    }

    default void onPropertyAdded(Identifiable<?> identifiable, String key, Object newValue) {
        // empty default implementation
    }

    default void onPropertyReplaced(Identifiable<?> identifiable, String key, Object oldValue, Object newValue) {
        // empty default implementation
    }

    default void onPropertyRemoved(Identifiable<?> identifiable, String key, Object oldValue) {
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
