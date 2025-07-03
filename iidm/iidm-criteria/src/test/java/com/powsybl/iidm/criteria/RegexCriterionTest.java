/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.criteria;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class RegexCriterionTest {

    @Test
    void polynomialRegexTest() {
        String regex = "(.*a){1000}";
        RegexCriterion criterion = new RegexCriterion(regex);
        MaliciousIdentifiable malicious = new MaliciousIdentifiable();

        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicBoolean result = new AtomicBoolean(true);
        Runnable runnable = () -> {
            result.set(criterion.filter(malicious, malicious.getType()));
            finished.set(true);
        };
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(runnable);

        await("Quick processing")
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(200, TimeUnit.MILLISECONDS)
            .untilTrue(finished);
        assertFalse(result.get());
    }

    private static class MaliciousIdentifiable implements Identifiable<MaliciousIdentifiable> {
        @Override
        public String getId() {
            return "a".repeat(100) + "!";
        }

        @Override
        public IdentifiableType getType() {
            return IdentifiableType.BUS;
        }

        @Override
        public Network getNetwork() {
            return null;
        }

        @Override
        public boolean hasProperty() {
            return false;
        }

        @Override
        public boolean hasProperty(String key) {
            return false;
        }

        @Override
        public String getProperty(String key) {
            return null;
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return defaultValue;
        }

        @Override
        public String setProperty(String key, String value) {
            return null;
        }

        @Override
        public boolean removeProperty(String key) {
            return false;
        }

        @Override
        public Set<String> getPropertyNames() {
            return Collections.emptySet();
        }

        @Override
        public <E extends Extension<MaliciousIdentifiable>> void addExtension(Class<? super E> type, E extension) {
            // Default
        }

        @Override
        public <E extends Extension<MaliciousIdentifiable>> E getExtension(Class<? super E> type) {
            return null;
        }

        @Override
        public <E extends Extension<MaliciousIdentifiable>> E getExtensionByName(String name) {
            return null;
        }

        @Override
        public <E extends Extension<MaliciousIdentifiable>> boolean removeExtension(Class<E> type) {
            return false;
        }

        @Override
        public <E extends Extension<MaliciousIdentifiable>> Collection<E> getExtensions() {
            return Collections.emptyList();
        }
    }
}
