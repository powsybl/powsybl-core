/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.net;

import com.google.common.testing.EqualsTester;
import com.powsybl.commons.json.JsonUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class UserProfileTest {

    @Test
    void test() throws IOException {
        UserProfile profile = new UserProfile("Peter", "Parker");
        assertEquals("Peter", profile.getFirstName());
        assertEquals("Parker", profile.getLastName());
        assertEquals("UserProfile(firstName=Peter, lastName=Parker)", profile.toString());
        new EqualsTester()
                .addEqualityGroup(new UserProfile("a", "b"), new UserProfile("a", "b"))
                .addEqualityGroup(new UserProfile("c", "d"), new UserProfile("c", "d"))
                .testEquals();
        String json = JsonUtil.createObjectMapper().writeValueAsString(profile);
        assertEquals("{\"firstName\":\"Peter\",\"lastName\":\"Parker\"}", json);
    }
}
