/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class BranchTest {

    @Test
    public void testPQI() {
        Terminal t1 = mock(Terminal.class);
        Terminal t2 = mock(Terminal.class);
        Branch branch = new Branch() {
            @Override
            public Terminal getTerminal1() {
                return t1;
            }

            @Override
            public Terminal getTerminal2() {
                return t2;
            }

            @Override
            public Terminal getTerminal(Side side) {
                return null;
            }

            @Override
            public Terminal getTerminal(String voltageLevelId) {
                return null;
            }

            @Override
            public Side getSide(Terminal terminal) {
                return null;
            }

            @Override
            public CurrentLimits getCurrentLimits(Side side) {
                return null;
            }

            @Override
            public CurrentLimits getCurrentLimits1() {
                return null;
            }

            @Override
            public CurrentLimitsAdder newCurrentLimits1() {
                return null;
            }

            @Override
            public ActivePowerLimitsAdder newActivePowerLimits1() {
                return null;
            }

            @Override
            public ApparentPowerLimitsAdder newApparentPowerLimits1() {
                return null;
            }

            @Override
            public CurrentLimits getCurrentLimits2() {
                return null;
            }

            @Override
            public CurrentLimitsAdder newCurrentLimits2() {
                return null;
            }

            @Override
            public ActivePowerLimitsAdder newActivePowerLimits2() {
                return null;
            }

            @Override
            public ApparentPowerLimitsAdder newApparentPowerLimits2() {
                return null;
            }

            @Override
            public boolean isOverloaded() {
                return false;
            }

            @Override
            public boolean isOverloaded(float limitReduction) {
                return false;
            }

            @Override
            public int getOverloadDuration() {
                return 0;
            }

            @Override
            public boolean checkPermanentLimit(Side side, float limitReduction) {
                return false;
            }

            @Override
            public boolean checkPermanentLimit(Side side) {
                return false;
            }

            @Override
            public boolean checkPermanentLimit1(float limitReduction) {
                return false;
            }

            @Override
            public boolean checkPermanentLimit1() {
                return false;
            }

            @Override
            public boolean checkPermanentLimit2(float limitReduction) {
                return false;
            }

            @Override
            public boolean checkPermanentLimit2() {
                return false;
            }

            @Override
            public Overload checkTemporaryLimits(Side side, float limitReduction) {
                return null;
            }

            @Override
            public Overload checkTemporaryLimits(Side side) {
                return null;
            }

            @Override
            public Overload checkTemporaryLimits1(float limitReduction) {
                return null;
            }

            @Override
            public Overload checkTemporaryLimits1() {
                return null;
            }

            @Override
            public Overload checkTemporaryLimits2(float limitReduction) {
                return null;
            }

            @Override
            public Overload checkTemporaryLimits2() {
                return null;
            }

            @Override
            public ConnectableType getType() {
                return null;
            }

            @Override
            public List<? extends Terminal> getTerminals() {
                return null;
            }

            @Override
            public void remove() {

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
            public Set<String> getPropertyNames() {
                return null;
            }

            @Override
            public void addExtension(Class type, Extension extension) {

            }

            @Override
            public Extension getExtension(Class type) {
                return null;
            }

            @Override
            public Extension getExtensionByName(String name) {
                return null;
            }

            @Override
            public boolean removeExtension(Class type) {
                return false;
            }

            @Override
            public Collection getExtensions() {
                return null;
            }
        };

        when(t1.getP()).thenReturn(99.0);
        assertEquals(99.0, branch.getP1());
        t1.setP(99.1);
        verify(t1, times(1)).setP(99.1);

        when(t1.getQ()).thenReturn(88.0);
        assertEquals(88.0, branch.getQ1());
        t1.setQ(88.1);
        verify(t1, times(1)).setQ(88.1);

        when(t1.getI()).thenReturn(1.0);
        assertEquals(1.0, branch.getI1());

        when(t1.connect()).thenReturn(true);
        assertTrue(branch.connect1());
        when(t1.disconnect()).thenReturn(false);
        assertFalse(branch.disconnect1());
        when(t1.isConnected()).thenReturn(true);
        assertTrue(branch.isConnected1());

        when(t2.getP()).thenReturn(992.0);
        assertEquals(992.0, branch.getP2());
        t2.setP(99.12);
        verify(t2, times(1)).setP(99.12);

        when(t2.getQ()).thenReturn(882.0);
        assertEquals(882.0, branch.getQ2());
        t2.setQ(88.12);
        verify(t2, times(1)).setQ(88.12);

        when(t2.getI()).thenReturn(1.02);
        assertEquals(1.02, branch.getI2());

        when(t2.connect()).thenReturn(true);
        assertTrue(branch.connect2());
        when(t2.disconnect()).thenReturn(false);
        assertFalse(branch.disconnect2());
        when(t2.isConnected()).thenReturn(true);
        assertTrue(branch.isConnected2());
    }
}
