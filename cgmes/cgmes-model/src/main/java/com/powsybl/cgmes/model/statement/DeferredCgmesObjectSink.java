/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model.statement;

import javax.xml.stream.XMLStreamException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A sink that collects the CGMES objects and sends them later to another sink.
 * It keeps one object for each subject: when an object arrives for a subject that it already has,
 * the new object replaces the previous one, and the subject keeps its first position.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class DeferredCgmesObjectSink implements CgmesObjectSink {

    private final Map<String, CgmesObject> objects = new LinkedHashMap<>();

    @Override
    public void accept(CgmesObject cgmesObject) {
        objects.put(cgmesObject.subject(), cgmesObject);
    }

    /**
     * The collected objects, one for each subject, in the order of their first arrival.
     */
    public Collection<CgmesObject> getObjects() {
        return objects.values();
    }

    /**
     * Send all the collected objects to the given sink, in the order of their first arrival.
     */
    public void sendAll(CgmesObjectSink sink) throws XMLStreamException {
        for (CgmesObject cgmesObject : objects.values()) {
            sink.accept(cgmesObject);
        }
    }
}
