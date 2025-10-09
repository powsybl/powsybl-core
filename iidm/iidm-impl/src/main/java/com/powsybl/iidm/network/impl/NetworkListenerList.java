/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkListenerList {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkListenerList.class);

    private final List<NetworkListener> listeners = new ArrayList<>();

    void add(NetworkListener listener) {
        listeners.add(listener);
    }

    void remove(NetworkListener listener) {
        listeners.remove(listener);
    }

    void notifyUpdate(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyUpdateListeners(identifiable, attribute.get(), null, oldValue, newValue);
        }
    }

    void notifyUpdate(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyUpdateListeners(identifiable, attribute, null, oldValue, newValue);
        }
    }

    void notifyUpdate(Identifiable<?> identifiable, Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyUpdateListeners(identifiable, attribute.get(), variantId, oldValue, newValue);
        }
    }

    void notifyUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyUpdateListeners(identifiable, attribute, variantId, oldValue, newValue);
        }
    }

    private void notifyUpdateListeners(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onUpdate(identifiable, attribute, variantId, oldValue, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    public void notifyExtensionCreation(Extension<?> extension) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onExtensionCreation(extension);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    public void notifyExtensionBeforeRemoval(Extension<?> extension) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onExtensionBeforeRemoval(extension);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    public void notifyExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onExtensionAfterRemoval(identifiable, extensionName);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    public void notifyExtensionUpdate(Extension<?> extension, String attribute, String variantId, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyExtensionUpdateListeners(extension, attribute, variantId, oldValue, newValue);
        }
    }

    private void notifyExtensionUpdateListeners(Extension<?> extension, String attribute, String variantId, Object oldValue, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onExtensionUpdate(extension, attribute, variantId, oldValue, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyCreation(Identifiable<?> identifiable) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onCreation(identifiable);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyBeforeRemoval(Identifiable<?> identifiable) {
        for (NetworkListener listener : listeners) {
            try {
                listener.beforeRemoval(identifiable);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyAfterRemoval(String id) {
        for (NetworkListener listener : listeners) {
            try {
                listener.afterRemoval(id);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyPropertyAdded(Identifiable<?> identifiable, Supplier<String> attribute, Object newValue) {
        if (!listeners.isEmpty()) {
            notifyPropertyAdded(identifiable, attribute.get(), newValue);
        }
    }

    void notifyPropertyAdded(Identifiable<?> identifiable, String attribute, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onPropertyAdded(identifiable, attribute, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyPropertyReplaced(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyPropertyReplaced(identifiable, attribute.get(), oldValue, newValue);
        }
    }

    void notifyPropertyReplaced(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onPropertyReplaced(identifiable, attribute, oldValue, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyPropertyRemoved(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue) {
        if (!listeners.isEmpty()) {
            notifyPropertyRemoved(identifiable, attribute.get(), oldValue);
        }
    }

    void notifyPropertyRemoved(Identifiable<?> identifiable, String attribute, Object oldValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onPropertyRemoved(identifiable, attribute, oldValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyVariantCreated(String sourceVariantId, String targetVariantId) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onVariantCreated(sourceVariantId, targetVariantId);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyVariantOverwritten(String sourceVariantId, String targetVariantId) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onVariantOverwritten(sourceVariantId, targetVariantId);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyVariantRemoved(String variantId) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onVariantRemoved(variantId);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }
}
