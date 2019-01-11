/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server.utils;

import com.powsybl.commons.config.PlatformConfig;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityConfig {

    private static final long DEFAULT_TOKEN_VALIDITY = 3600L; // minutes
    private static final boolean DEFAULT_SKIP_TOKEN_VALIDITY_CHECK = true;

    private long tokenValidity;

    private boolean skipTokenValidityCheck;

    private static long checkTokenValidity(long tokenValidity) {
        if (tokenValidity < 1) {
            throw new IllegalArgumentException("Invalid token validity");
        }
        return tokenValidity;
    }

    public static SecurityConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        return platformConfig.getOptionalModuleConfig("security")
                .map(securityConfig -> {
                    long tokenValidity = securityConfig.getOptionalLongProperty("token-validity")
                            .orElse(DEFAULT_TOKEN_VALIDITY);
                    boolean skipTokenValidityCheck = securityConfig.getOptionalBooleanProperty("skip-token-validity-check")
                            .orElse(DEFAULT_SKIP_TOKEN_VALIDITY_CHECK);
                    return new SecurityConfig(tokenValidity, skipTokenValidityCheck);
                })
                .orElseGet(() ->
                        new SecurityConfig(DEFAULT_TOKEN_VALIDITY, DEFAULT_SKIP_TOKEN_VALIDITY_CHECK)
                );
    }

    public SecurityConfig(long tokenValidity, boolean skipTokenValidityCheck) {
        this.tokenValidity = checkTokenValidity(tokenValidity);
        this.skipTokenValidityCheck = skipTokenValidityCheck;
    }

    public long getTokenValidity() {
        return tokenValidity;
    }

    public void setTokenValidity(long tokenValidity) {
        this.tokenValidity = checkTokenValidity(tokenValidity);
    }

    public boolean isSkipTokenValidityCheck() {
        return skipTokenValidityCheck;
    }

    public void setSkipTokenValidityCheck(boolean skipTokenValidityCheck) {
        this.skipTokenValidityCheck = skipTokenValidityCheck;
    }
}
