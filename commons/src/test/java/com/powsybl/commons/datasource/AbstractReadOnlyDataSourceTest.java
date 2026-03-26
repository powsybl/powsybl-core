/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
abstract class AbstractReadOnlyDataSourceTest {

    protected abstract ReadOnlyDataSource createDataSourceForPolynomialRegexTest() throws IOException;

    @Test
    void polynomialRegexTest() throws IOException {
        ReadOnlyDataSource dataSource = createDataSourceForPolynomialRegexTest();

        AtomicBoolean finished = new AtomicBoolean(false);
        Runnable runnable = () -> {
            try {
                dataSource.listNames("(.*a){1000}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finished.set(true);
        };
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(runnable);

        await("Quick processing")
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(200, TimeUnit.MILLISECONDS)
            .untilTrue(finished);
    }
}
