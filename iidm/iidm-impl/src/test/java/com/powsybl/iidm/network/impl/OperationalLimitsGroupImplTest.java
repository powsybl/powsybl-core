/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

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
                assertEquals(100., getPermanentLimit((OperationalLimitsGroupImpl.OperationalLimitsInfo) oldValue));
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
        CustomOperationalLimitsGroup customGroup = new CustomOperationalLimitsGroup("group1", (AbstractBus) bus, validable,
                "limits", "selected");
        customGroup.newCurrentLimits().setPermanentLimit(100.).add();
        assertFalse(updated[0]);
        customGroup.enableNotification(true);
        customGroup.newCurrentLimits().setPermanentLimit(1000.).add();
        assertTrue(updated[0]);
        assertEquals("Custom validable", customGroup.getValidable().getMessageHeader());
    }

    @Test
    void invalidCustomOperationalLimitsGroupTest() {
        assertThrows(IllegalArgumentException.class, () -> new CustomOperationalLimitsGroup("group1", new CustomIdentifiable(), null,
                "limits", "selected"));
    }

    static class CustomOperationalLimitsGroup extends OperationalLimitsGroupImpl {
        private boolean notificationEnabled = false;

        public CustomOperationalLimitsGroup(String id, Identifiable<?> identifiable, Validable validable,
                                            String attributeName, String selectedGroupId) {
            super(id, identifiable, validable, attributeName, selectedGroupId);
        }

        public void enableNotification(boolean enabled) {
            notificationEnabled = enabled;
        }

        @Override
        protected boolean notificationEnabled() {
            return notificationEnabled;
        }
    }

    static class CustomIdentifiable implements Identifiable<CustomIdentifiable> {
        @Override
        public <E extends Extension<CustomIdentifiable>> void addExtension(Class<? super E> type, E extension) {

        }

        @Override
        public <E extends Extension<CustomIdentifiable>> E getExtension(Class<? super E> type) {
            return null;
        }

        @Override
        public <E extends Extension<CustomIdentifiable>> E getExtensionByName(String name) {
            return null;
        }

        @Override
        public <E extends Extension<CustomIdentifiable>> boolean removeExtension(Class<E> type) {
            return false;
        }

        @Override
        public <E extends Extension<CustomIdentifiable>> Collection<E> getExtensions() {
            return null;
        }

        @Override
        public Network getNetwork() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public boolean hasProperty() {
            return false;
        }

        @Override
        public boolean hasProperty(String key) {
            return false;
        }

        @Override
        public String getProperty(String key) {
            return null;
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return null;
        }

        @Override
        public String setProperty(String key, String value) {
            return null;
        }

        @Override
        public boolean removeProperty(String key) {
            return false;
        }

        @Override
        public Set<String> getPropertyNames() {
            return null;
        }

        @Override
        public IdentifiableType getType() {
            return null;
        }
    }
}
