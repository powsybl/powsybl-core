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
public abstract class HistoDbAttributeId implements Comparable<HistoDbAttributeId> {

    @Override
    public int compareTo(HistoDbAttributeId o) {
        if (this instanceof HistoDbNetworkAttributeId && o instanceof HistoDbNetworkAttributeId) {
            HistoDbNetworkAttributeId id1 = (HistoDbNetworkAttributeId) this;
            HistoDbNetworkAttributeId id2 = (HistoDbNetworkAttributeId) o;
            int c = id1.getEquipmentId().compareTo(id2.getEquipmentId());
            if (c == 0) {
                if (id1.getSide() == null && id2.getSide() == null) {
                    c = 0;
                } else if (id1.getSide() != null && id2.getSide() != null) {
                    c = id1.getSide().compareTo(id2.getSide());
                } else if (id1.getSide() == null && id2.getSide() != null) {
                    c = -1;
                } else if (id1.getSide() != null && id2.getSide() == null) {
                    c = 1;
                } else {
                    throw new AssertionError();
                }
                if (c == 0) {
                    c = id1.getAttributeType().compareTo(id2.getAttributeType());
                }
            }
            return c;
        } else if (this instanceof HistoDbMetaAttributeId && o instanceof HistoDbMetaAttributeId) {
            return ((HistoDbMetaAttributeId) this).getType().compareTo(((HistoDbMetaAttributeId) o).getType());
        } else if (this instanceof HistoDbNetworkAttributeId && o instanceof HistoDbMetaAttributeId) {
            return 1;
        } else if (this instanceof HistoDbMetaAttributeId && o instanceof HistoDbNetworkAttributeId) {
            return -1;
        } else {
            throw new AssertionError();
        }
    }

}
