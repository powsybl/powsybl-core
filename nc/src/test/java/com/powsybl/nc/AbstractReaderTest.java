/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.nc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class AbstractReaderTest {

    protected TestAppender appender;
    protected QueryManager queryManager;
    protected static final Network NETWORK = Network.read("16Nodes.zip", AbstractReaderTest.class.getResourceAsStream("/16Nodes.zip"));

    protected static class TestAppender extends AppenderBase<ILoggingEvent> {
        private final List<ILoggingEvent> events = new ArrayList<>();

        @Override
        protected void append(ILoggingEvent eventObject) {
            events.add(eventObject);
        }

        public List<ILoggingEvent> getEvents() {
            return events;
        }
    }

    @BeforeEach
    void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(NcConverter.class);
        appender = new TestAppender();
        appender.setContext((ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory());
        appender.start();
        logger.addAppender(appender);
        queryManager = new QueryManager();
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(NcConverter.class);
        logger.detachAppender(appender);
    }

}
