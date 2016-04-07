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
public class HistoDbAttributeIdParser {

    private HistoDbAttributeIdParser() {
    }

    public static HistoDbAttributeId parse(String txt) {
        try {
            return new HistoDbMetaAttributeId(HistoDbMetaAttributeType.valueOf(txt));
        } catch (IllegalArgumentException e) {
            for (HistoDbAttr type : HistoDbAttr.values()) {
                if (txt.endsWith("_" + type.name())) {
                    String terminalId = txt.substring(0, txt.length() - 1 - type.name().length());
                    String equipmentId;
                    String side = null;
                    int pos = terminalId.indexOf(HistoDbNetworkAttributeId.SIDE_SEPARATOR);
                    if (pos == -1) {
                        equipmentId = terminalId;
                    } else {
                        equipmentId = terminalId.substring(0, pos);
                        side = terminalId.substring(pos + HistoDbNetworkAttributeId.SIDE_SEPARATOR.length());
                    }
                    return new HistoDbNetworkAttributeId(equipmentId, side, type);
                }
            }
            throw new IllegalArgumentException("Invalid attribute id " + txt);
        }
    }

}
