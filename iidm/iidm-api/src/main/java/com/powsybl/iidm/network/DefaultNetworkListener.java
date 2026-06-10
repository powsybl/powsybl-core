/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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
public interface DefaultNetworkListener extends NetworkListener {
    @Override
    default void onCreation(Identifiable<?> identifiable) {
        // empty default implementation
    }

    @Override
    default void beforeRemoval(Identifiable<?> identifiable) {
        // empty default implementation
    }

    @Override
    default void afterRemoval(String identifiable) {
        // empty default implementation
    }

    @Override
    default void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        // empty default implementation
    }

    @Override
    default void onExtensionCreation(Extension<?> extension) {
        // empty default implementation
    }

    @Override
    default void onExtensionUpdate(Extension<?> extension, String attribute, String variantId, Object oldValue, Object newValue) {
        // empty default implementation
    }

    @Override
    default void onExtensionBeforeRemoval(Extension<?> extension) {
        // empty default implementation
    }

    @Override
    default void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        // empty default implementation
    }

    @Override
    default void onPropertyAdded(Identifiable<?> identifiable, String key, Object newValue) {
        // empty default implementation
    }

    @Override
    default void onPropertyReplaced(Identifiable<?> identifiable, String key, Object oldValue, Object newValue) {
        // empty default implementation
    }

    @Override
    default void onPropertyRemoved(Identifiable<?> identifiable, String key, Object oldValue) {
        // empty default implementation
    }

    @Override
    default void onVariantCreated(String sourceVariantId, String targetVariantId) {
        // empty default implementation
    }

    @Override
    default void onVariantOverwritten(String sourceVariantId, String targetVariantId) {
        // empty default implementation
    }

    @Override
    default void onVariantRemoved(String variantId) {
        // empty default implementation
    }
}
