/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.events.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkEventRecorder implements NetworkListener {

    private final List<NetworkEvent> events = new ArrayList<>();

    public List<NetworkEvent> getEvents() {
        return events;
    }

    public void reset() {
        events.clear();
    }

    @Override
    public void onCreation(Identifiable<?> identifiable) {
        events.add(new CreationNetworkEvent(identifiable.getId()));
    }

    @Override
    public void beforeRemoval(Identifiable<?> identifiable) {
        events.add(new RemovalNetworkEvent(identifiable.getId(), false));
    }

    @Override
    public void afterRemoval(String id) {
        events.add(new RemovalNetworkEvent(id, true));
    }

    @Override
    public void onUpdate(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        events.add(new UpdateNetworkEvent(identifiable.getId(), attribute, null, oldValue, newValue));
    }

    @Override
    public void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        events.add(new UpdateNetworkEvent(identifiable.getId(), attribute, variantId, oldValue, newValue));
    }

    @Override
    public void onExtensionCreation(Extension<?> extension) {
        events.add(new ExtensionCreationNetworkEvent(((Identifiable<?>) extension.getExtendable()).getId(), extension.getName()));
    }

    @Override
    public void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        events.add(new ExtensionRemovalNetworkEvent(identifiable.getId(), extensionName, true));
    }

    @Override
    public void onExtensionBeforeRemoval(Extension<?> extension) {
        events.add(new ExtensionRemovalNetworkEvent(((Identifiable<?>) extension.getExtendable()).getId(), extension.getName(), false));
    }

    @Override
    public void onExtensionUpdate(Extension<?> extension, String attribute, String variantId, Object oldValue, Object newValue) {
        events.add(new ExtensionUpdateNetworkEvent(((Identifiable<?>) extension.getExtendable()).getId(), extension.getName(), attribute, variantId, oldValue, newValue));
    }

    @Override
    public void onElementAdded(Identifiable<?> identifiable, String attribute, Object newValue) {
        events.add(new PropertyUpdateNetworkEvent(identifiable.getId(), attribute, PropertyUpdateNetworkEvent.PropertyUpdateType.ADDED, null, newValue));
    }

    @Override
    public void onElementReplaced(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        events.add(new PropertyUpdateNetworkEvent(identifiable.getId(), attribute, PropertyUpdateNetworkEvent.PropertyUpdateType.REPLACED, oldValue, newValue));
    }

    @Override
    public void onElementRemoved(Identifiable<?> identifiable, String attribute, Object oldValue) {
        events.add(new PropertyUpdateNetworkEvent(identifiable.getId(), attribute, PropertyUpdateNetworkEvent.PropertyUpdateType.REMOVED, oldValue, null));
    }

    @Override
    public void onVariantCreated(String sourceVariantId, String targetVariantId) {
        events.add(new VariantNetworkEvent(sourceVariantId, targetVariantId, VariantNetworkEvent.VariantEventType.CREATED));
    }

    @Override
    public void onVariantOverwritten(String sourceVariantId, String targetVariantId) {
        events.add(new VariantNetworkEvent(sourceVariantId, targetVariantId, VariantNetworkEvent.VariantEventType.OVERWRITTEN));
    }

    @Override
    public void onVariantRemoved(String variantId) {
        events.add(new VariantNetworkEvent(variantId, null, VariantNetworkEvent.VariantEventType.REMOVED));
    }
}
