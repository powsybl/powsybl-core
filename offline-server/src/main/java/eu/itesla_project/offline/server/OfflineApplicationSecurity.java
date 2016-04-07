/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.slf4j.LoggerFactory;

/**
 * This class use the jboss-web-policy for the authentication
 * Use this command to add the user to wildfly:
 * bin/add-user.sh -u user -p password -a -up standalone/configuration/application-users.properties -gp standalone/configuration/application-roles.properties
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineApplicationSecurity {

    public static final String USERNAME_HTTP_PARAMETER = "Username";
    public static final String PASSWORD_HTTP_PARAMETER = "Password";

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OfflineApplicationSecurity.class);

    private OfflineApplicationSecurity() {
    }

    public static boolean check(String username, String password) {
        try {
            LoginContext lc = new LoginContext("Dummy", new LoginHandler(username, password));
            lc.login();
            return true;
        } catch (LoginException ex) {
            LOGGER.error(ex.getMessage());
        }
        return false;
    }

    private static class LoginHandler implements CallbackHandler {

        private final String username, password;

        public LoginHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    // prompt the user for a username
                    NameCallback nameCallback = (NameCallback) callback;
                    nameCallback.setName(username);
                } else if (callback instanceof PasswordCallback) {
                    // prompt the user for a password
                    PasswordCallback passwordCallback = (PasswordCallback) callback;
                    passwordCallback.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                }
            }
        }

    }
}
