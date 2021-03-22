/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public enum MarkerImpl implements Marker {

    PERFORMANCE(LogLevel.INFO),
    DEFAULT(LogLevel.INFO),
    TRACE(LogLevel.TRACE), DEBUG(LogLevel.DEBUG), INFO(LogLevel.INFO), WARN(LogLevel.WARN), ERROR(LogLevel.ERROR);

    private final LogLevel logLevel;

    MarkerImpl(LogLevel level) {
        this.logLevel = level;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public String getName() {
        return name();
    }
}
