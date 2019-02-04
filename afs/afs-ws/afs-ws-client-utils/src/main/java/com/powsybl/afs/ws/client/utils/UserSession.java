/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client.utils;

import com.powsybl.commons.net.UserProfile;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UserSession {

    private UserProfile profile;

    private final String token;

    public UserSession(UserProfile profile, String token) {
        this.profile = Objects.requireNonNull(profile);
        this.token = token;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public String getToken() {
        return token;
    }
}
