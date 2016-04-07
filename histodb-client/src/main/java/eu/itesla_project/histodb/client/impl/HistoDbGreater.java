/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbGreater implements HistoDbFilter {

    private final float value;

    public HistoDbGreater(float value) {
        this.value = value;
    }

    @Override
    public void format(StringBuilder builder) {
        builder.append("[").append(value).append(",Infinity]");
    }

}
