package com.powsybl.afs.ws.server;

import com.powsybl.afs.ws.server.utils.UserAuthenticator;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UserAuthenticatorMock implements UserAuthenticator {

    @Override
    public void check(String login, String password) {
    }

}
