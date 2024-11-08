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
public class DefaultNetworkListener implements NetworkListener {
    @Override
    public void onCreation(Identifiable<?> identifiable) {
        // empty default implementation
    }

    @Override
    public void beforeRemoval(Identifiable<?> identifiable) {
        // empty default implementation
    }

    @Override
    public void afterRemoval(String identifiable) {
        // empty default implementation
    }

    @Override
    public void onUpdate(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        // empty default implementation
    }

    @Override
    public void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        // empty default implementation
    }

    @Override
    public void onExtensionCreation(Extension<?> extension) {
        // empty default implementation
    }

    @Override
    public void onExtensionUpdate(Extension<?> extension, String attribute, String variantId, Object oldValue, Object newValue) {
        // empty default implementation
    }

    @Override
    public void onExtensionBeforeRemoval(Extension<?> extension) {
        // empty default implementation
    }

    @Override
    public void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        // empty default implementation
    }
}
