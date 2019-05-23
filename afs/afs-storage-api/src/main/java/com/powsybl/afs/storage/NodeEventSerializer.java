/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.util.Map;

import com.powsybl.afs.storage.events.NodeEvent;
import com.powsybl.commons.PowsyblException;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class NodeEventSerializer implements Serializer<NodeEvent> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Nothing to do
    }

    @Override
    public byte[] serialize(String topic, NodeEvent data) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(data).getBytes();
        } catch (Exception exception) {
            throw new PowsyblException("Exception thrown while serializing NodeEvent : " + data.getId());
        }
        return retVal;

    }

    @Override
    public void close() {
        // Nothing to do
    }

}
