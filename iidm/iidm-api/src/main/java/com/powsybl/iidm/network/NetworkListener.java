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

    void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue);

    void onExtensionCreation(Extension<?> extension);

    void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName);

    void onExtensionBeforeRemoval(Extension<?> extension);

    void onExtensionUpdate(Extension<?> extendable, String attribute, String variantId, Object oldValue, Object newValue);

    void onPropertyAdded(Identifiable<?> identifiable, String attribute, Object newValue);

    void onPropertyReplaced(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue);

    void onPropertyRemoved(Identifiable<?> identifiable, String attribute, Object oldValue);

    void onVariantCreated(String sourceVariantId, String targetVariantId);

    void onVariantOverwritten(String sourceVariantId, String targetVariantId);

    void onVariantRemoved(String variantId);
}
