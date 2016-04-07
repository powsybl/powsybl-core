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
public class HistoDbNetworkAttributeId extends HistoDbAttributeId {

    public static final String SIDE_SEPARATOR = "__TO__";

    private final String equipmentId;

    private final String side;

    private final HistoDbAttr attributeType;

    public HistoDbNetworkAttributeId(String equipmentId, HistoDbAttr attributeType) {
        this(equipmentId, null, attributeType);
    }

    public HistoDbNetworkAttributeId(String equipmentId, String side, HistoDbAttr attributeType) {
        Objects.requireNonNull(equipmentId);
        Objects.requireNonNull(attributeType);
        this.equipmentId = equipmentId;
        this.side = side;
        this.attributeType = attributeType;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getSide() {
        return side;
    }

    public HistoDbAttr getAttributeType() {
        return attributeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentId, side, attributeType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HistoDbNetworkAttributeId) {
            HistoDbNetworkAttributeId other = (HistoDbNetworkAttributeId) obj;
            return other.equipmentId.equals(equipmentId)
                    && Objects.equals(other.side, side)
                    && other.attributeType == attributeType;
        }
        return false;
    }

    @Override
    public String toString() {
        return equipmentId + (side != null ? SIDE_SEPARATOR + side : "") + "_" + attributeType;
    }
}
