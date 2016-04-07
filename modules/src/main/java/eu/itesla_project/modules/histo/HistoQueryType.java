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
public enum HistoQueryType {
    data,
    forecastDiff("data/forecastsDiff"),
    stats;

    private final String str;

    private HistoQueryType(String str) {
        this.str = str;
    }

    private HistoQueryType() {
        this(null);
    }

    @Override
    public String toString() {
        return str != null ? str : super.toString();
    }

}
