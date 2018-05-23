/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.storage;

import com.powsybl.afs.TaskEvent;
import com.powsybl.afs.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ClientEndpoint(decoders = {TaskEventDecoder.class})
public class TaskEventClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskEventClient.class);

    private final TaskListener listener;

    public TaskEventClient(TaskListener listener) {
        this.listener = Objects.requireNonNull(listener);
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.trace("Task event websocket session '{}' opened", session.getId());
    }

    @OnMessage
    public void onMessage(Session session, TaskEvent taskEvent) {
        LOGGER.trace("Task event websocket session '{}' received an event: {}",
                session.getId(), taskEvent);
        listener.onEvent(taskEvent);
    }

    @OnError
    public void onError(Throwable t) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(t.toString(), t);
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.trace("Task event websocket session '{}' closed", session.getId());
    }
}
