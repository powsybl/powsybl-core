/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server;

import com.powsybl.afs.ws.server.utils.UserAuthenticator;
import com.powsybl.commons.net.UserProfile;

import javax.inject.Named;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Named
public class UserAuthenticatorMock implements UserAuthenticator {

    @Override
    public UserProfile check(String login, String password) {
        return new UserProfile("bat", "man");
    }
}
