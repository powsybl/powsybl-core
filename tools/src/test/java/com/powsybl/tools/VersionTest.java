/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Antoine Bouhours {@literal <antoine.bouhours at rte-france.com>}
 */
class VersionTest {
    @Test
    void testVersionMap() {
        String repositoryName = "Sample Repository";
        String mavenProjectVersion = "1.0.0";
        String gitVersion = "abc123";
        String gitBranch = "main";
        long buildTimestamp = 1707312507024L; // no formatting is done but the raw number value (milliseconds since January 1, 1970, 00:00:00 GMT) is used (https://www.mojohaus.org/buildnumber-maven-plugin/create-timestamp-mojo.html#timestampFormat)
        AbstractVersion version = new AbstractVersion(repositoryName, mavenProjectVersion, gitVersion, gitBranch, buildTimestamp) { };
        Map<String, String> versionMap = version.toMap();

        assertEquals(repositoryName, versionMap.get("repositoryName"));
        assertEquals(mavenProjectVersion, versionMap.get("mavenProjectVersion"));
        assertEquals(gitVersion, versionMap.get("gitVersion"));
        assertEquals(gitBranch, versionMap.get("gitBranch"));
        assertEquals("2024-02-07T13:28:27.024Z", versionMap.get("buildTimestamp"));
    }
}
