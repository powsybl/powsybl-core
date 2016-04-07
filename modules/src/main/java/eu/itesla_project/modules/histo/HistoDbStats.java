/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbStats {

    private final Table<String, String, Float> table;

    public HistoDbStats() {
        this(HashBasedTable.create());
    }

    public HistoDbStats(Table<String, String, Float> table) {
        this.table = table;
    }

    public float getValue(HistoDbStatsType type, HistoDbAttributeId attributeId, float invalidValue) {
        Float value = table.get(type.toString(), attributeId.toString());
        return value != null && !Float.isNaN(value) ? value : invalidValue;
    }

    public void setValue(HistoDbStatsType type, HistoDbAttributeId attributeId, float value) {
        table.put(type.toString(), attributeId.toString(), value);
    }

    public boolean containsAttr(HistoDbAttributeId attributeId) {
        return table.columnKeySet().contains(attributeId.toString());
    }

}
