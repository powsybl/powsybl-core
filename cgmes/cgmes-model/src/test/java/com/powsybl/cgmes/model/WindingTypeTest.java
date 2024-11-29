/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import com.powsybl.triplestore.api.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class WindingTypeTest {

    PropertyBag end1;
    PropertyBag end2;
    PropertyBag end3;
    PropertyBag end4;

    static final String WINDING_TYPE = "windingType";
    static final String END_NUMBER = "endNumber";

    @Test
    void cim14WindingTypeTest() {
        end1 = new PropertyBag(Collections.singletonList(WINDING_TYPE), true);
        end1.put(WINDING_TYPE, "http://iec.ch/TC57/2009/CIM-schema-cim14#WindingType.primary");
        assertEquals(WindingType.PRIMARY, WindingType.windingType(end1));
        assertEquals(1, WindingType.endNumber(end1));

        end2 = new PropertyBag(Collections.singletonList(WINDING_TYPE), true);
        end2.put(WINDING_TYPE, "http://iec.ch/TC57/2009/CIM-schema-cim14#WindingType.secondary");
        assertEquals(WindingType.SECONDARY, WindingType.windingType(end2));
        assertEquals(2, WindingType.endNumber(end2));

        end3 = new PropertyBag(Collections.singletonList(WINDING_TYPE), true);
        end3.put(WINDING_TYPE, "http://iec.ch/TC57/2009/CIM-schema-cim14#WindingType.tertiary");
        assertEquals(WindingType.TERTIARY, WindingType.windingType(end3));
        assertEquals(3, WindingType.endNumber(end3));

        end4 = new PropertyBag(Collections.singletonList(WINDING_TYPE), true);
        end4.put(WINDING_TYPE, "WindingType.quaternary");
        assertEquals(WindingType.PRIMARY, WindingType.windingType(end4));
        assertEquals(1, WindingType.endNumber(end4));
    }

    @Test
    void cim16WindingTypeTest() {
        end1 = new PropertyBag(Collections.singletonList(END_NUMBER), true);
        end1.put(END_NUMBER, "1");
        assertEquals(WindingType.PRIMARY, WindingType.windingType(end1));
        assertEquals(1, WindingType.endNumber(end1));

        end2 = new PropertyBag(Collections.singletonList(END_NUMBER), true);
        end2.put(END_NUMBER, "2");
        assertEquals(WindingType.SECONDARY, WindingType.windingType(end2));
        assertEquals(2, WindingType.endNumber(end2));

        end3 = new PropertyBag(Collections.singletonList(END_NUMBER), true);
        end3.put(END_NUMBER, "3");
        assertEquals(WindingType.TERTIARY, WindingType.windingType(end3));
        assertEquals(3, WindingType.endNumber(end3));

        end4 = new PropertyBag(Collections.singletonList(END_NUMBER), true);
        end4.put(END_NUMBER, "4");
        assertEquals(WindingType.PRIMARY, WindingType.windingType(end4));
        assertEquals(1, WindingType.endNumber(end4));
    }
}
