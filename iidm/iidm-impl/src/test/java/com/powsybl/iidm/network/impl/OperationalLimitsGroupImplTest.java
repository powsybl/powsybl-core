/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class OperationalLimitsGroupImplTest {

    // The purpose of this test is to check that it is possible to use a custom OperationalLimitsGroup
    // to add OperationalLimits which are not directly linked in a Network element. For instance,
    // it could be used to define limits in an extension without having to define custom limits implementation.
    @Test
    void customOperationalLimitsGroupTest() {
        Network network = EurostagTutorialExample1Factory.create();
        boolean[] updated = new boolean[1];
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void onUpdate(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
                assertEquals("NHV1", identifiable.getId());
                assertEquals("limits_CURRENT", attribute);
                assertNull(((OperationalLimitsGroupImpl.OperationalLimitsInfo) oldValue).value());
                assertEquals(1000., getPermanentLimit((OperationalLimitsGroupImpl.OperationalLimitsInfo) newValue));
                updated[0] = true;
            }

            private double getPermanentLimit(OperationalLimitsGroupImpl.OperationalLimitsInfo info) {
                return ((AbstractLoadingLimits<?>) info.value()).getPermanentLimit();
            }
        });
        assertFalse(updated[0]);
        Bus bus = network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1");
        Validable validable = () -> "Custom validable";
        CustomOperationalLimitsGroup customGroup = new CustomOperationalLimitsGroup("group1", bus, null,
                validable, "limits", "selected");
        customGroup.newCurrentLimits().setPermanentLimit(100.).add();
        assertFalse(updated[0]);

        customGroup = new CustomOperationalLimitsGroup("group1", bus, ((NetworkImpl) network).getListeners(),
                validable, "limits", "selected");
        customGroup.newCurrentLimits().setPermanentLimit(1000.).add();
        assertTrue(updated[0]);
        assertEquals("Custom validable", customGroup.getValidable().getMessageHeader());
    }

    static class CustomOperationalLimitsGroup extends OperationalLimitsGroupImpl {
        public CustomOperationalLimitsGroup(String id, Identifiable<?> identifiable, NetworkListenerList listeners,
                                            Validable validable, String attributeName, String selectedGroupId) {
            super(id, identifiable, listeners, validable, attributeName, selectedGroupId);
        }
    }
}
