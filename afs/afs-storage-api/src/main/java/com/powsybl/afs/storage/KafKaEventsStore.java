/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.NodeEvent;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class KafKaEventsStore implements EventsStore {

    private Producer<String, NodeEvent> producer;

    public KafKaEventsStore() {
        producer = ProducerFactory.create();
    }

    @Override
    public void pushEvent(NodeEvent event, String fileSystem, String topic) {
        ProducerRecord<String, NodeEvent> record = new ProducerRecord<>(topic, event);
        producer.send(record);
    }
}
