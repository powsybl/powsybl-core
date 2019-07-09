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
            for (NetworkListener listener : listeners) {
                try {
                    listener.onUpdate(identifiable, attribute.get(), oldValue, newValue);
                } catch (Exception t) {
                    LOGGER.error(t.toString(), t);
                }
            }
        }
    }

    void notifyUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            for (NetworkListener listener : listeners) {
                try {
                    listener.onUpdate(identifiable, attribute, oldValue, newValue);
                } catch (Throwable t) {
                    LOGGER.error(t.toString(), t);
                }
            }
        }
    }

    void notifyUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        if (!listeners.isEmpty() && !Objects.equals(oldValue, newValue)) {
            for (NetworkListener listener : listeners) {
                try {
                    listener.onUpdate(identifiable, attribute, variantId, oldValue, newValue);
                } catch (Exception t) {
                    LOGGER.error(t.toString(), t);
                }
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

    void notifyRemoval(Identifiable identifiable) {
        for (NetworkListener listener : listeners) {
            try {
                listener.onRemoval(identifiable);
            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

}
