/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NetworkListenerList {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkListenerList.class);

    private final List<NetworkListener> listeners = new ArrayList<>();

    void add(NetworkListener listener) {
        listeners.add(listener);
    }

    void remove(NetworkListener listener) {
        listeners.remove(listener);
    }

    void notifyUpdate(Identifiable identifiable, Supplier<String> attribute, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyListeners(identifiable, attribute.get(), oldValue, newValue);
        }
    }

    void notifyUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyListeners(identifiable, attribute, oldValue, newValue);
        }
    }

    private void notifyListeners(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onUpdate(identifiable, attribute, oldValue, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyUpdate(Identifiable identifiable, Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyListeners(identifiable, attribute.get(), variantId, oldValue, newValue);
        }
    }

    void notifyUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyListeners(identifiable, attribute, variantId, oldValue, newValue);
        }
    }

    private void notifyListeners(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onUpdate(identifiable, attribute, variantId, oldValue, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyCreation(Identifiable identifiable) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onCreation(identifiable);
            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyBeforeRemoval(Identifiable identifiable) {
        for (NetworkListener listener : listeners) {
            try {
                listener.beforeRemoval(identifiable);
            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyAfterRemoval(String id) {
        for (NetworkListener listener : listeners) {
            try {
                listener.afterRemoval(id);
            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyElementAdded(Identifiable<?> identifiable, Supplier<String> attribute, Object newValue) {
        if (!listeners.isEmpty()) {
            notifyElementAdded(identifiable, attribute.get(), newValue);
        }
    }

    void notifyElementAdded(Identifiable<?> identifiable, String attribute, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onElementAdded(identifiable, attribute, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyElementReplaced(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyElementReplaced(identifiable, attribute.get(), oldValue, newValue);
        }
    }

    void notifyElementReplaced(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onElementReplaced(identifiable, attribute, oldValue, newValue);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    void notifyElementRemoved(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue) {
        if (!listeners.isEmpty()) {
            notifyElementRemoved(identifiable, attribute.get(), oldValue);
        }
    }

    void notifyElementRemoved(Identifiable<?> identifiable, String attribute, Object oldValue) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onElementRemoved(identifiable, attribute, oldValue);
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
