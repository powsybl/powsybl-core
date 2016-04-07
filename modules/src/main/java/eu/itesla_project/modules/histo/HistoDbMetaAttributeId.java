/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbMetaAttributeId extends HistoDbAttributeId {

    public static final HistoDbMetaAttributeId cimName = new HistoDbMetaAttributeId(HistoDbMetaAttributeType.cimName);
    public static final HistoDbMetaAttributeId datetime = new HistoDbMetaAttributeId(HistoDbMetaAttributeType.datetime);
    public static final HistoDbMetaAttributeId daytime = new HistoDbMetaAttributeId(HistoDbMetaAttributeType.daytime);
    public static final HistoDbMetaAttributeId month = new HistoDbMetaAttributeId(HistoDbMetaAttributeType.month);
    public static final HistoDbMetaAttributeId forecastTime = new HistoDbMetaAttributeId(HistoDbMetaAttributeType.forecastTime);
    public static final HistoDbMetaAttributeId horizon = new HistoDbMetaAttributeId(HistoDbMetaAttributeType.horizon);

    private final HistoDbMetaAttributeType type;

    public HistoDbMetaAttributeId(HistoDbMetaAttributeType type) {
        this.type = Objects.requireNonNull(type);
    }

    public HistoDbMetaAttributeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HistoDbMetaAttributeId) {
            return type.equals(((HistoDbMetaAttributeId) obj).getType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
