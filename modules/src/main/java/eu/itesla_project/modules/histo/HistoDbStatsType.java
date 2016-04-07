/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum HistoDbStatsType {
    MAX,
    P99,
    P0_1("P0.1"),
    VAR,
    P90,
    P95,
    MIN,
    P1,
    MEAN,
    P99_9("P99.9"),
    P50,
    COUNT;

    private final String label;

    private HistoDbStatsType(String label) {
        this.label = label;
    }

    private HistoDbStatsType() {
        this(null);
    }

    @Override
    public String toString() {
        return label != null ? label : super.toString();
    }
}
