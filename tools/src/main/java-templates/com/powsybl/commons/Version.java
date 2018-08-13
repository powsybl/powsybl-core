/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons;

import com.google.common.collect.ImmutableMap;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import java.util.Map;
import org.joda.time.DateTime;

/**
 * @deprecated Use {@link com.powsybl.tools.Version} instead.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Deprecated
public class Version {

    public static final Version VERSION = new Version();

    private final String projectVersion;

    private final String gitVersion;

    private final String gitBranch;

    private final long buildTimestamp;

    public Version() {
        this("${project.version}", "${buildNumber}", "${scmBranch}", Long.parseLong("${timestamp}"));
    }

    public Version(String projectVersion, String gitVersion, String gitBranch, long buildTimestamp) {
        this.projectVersion = Objects.requireNonNull(projectVersion);
        this.gitVersion = Objects.requireNonNull(gitVersion);
        this.gitBranch = Objects.requireNonNull(gitBranch);
        this.buildTimestamp = buildTimestamp;
    }

    public String getGitVersion() {
        return gitVersion;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public long getBuildTimestamp() {
        return buildTimestamp;
    }

    public Map<String, String> toMap() {
        return ImmutableMap.of("projectVersion", projectVersion,
                               "gitVersion", gitVersion,
                               "gitBranch", gitBranch,
                               "buildTimestamp", new DateTime(buildTimestamp).toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Version) {
            Version other = (Version) obj;
            return projectVersion.equals(other.projectVersion)
                    && gitVersion.equals(other.gitVersion)
                    && gitBranch.equals(other.gitBranch)
                    && buildTimestamp == other.buildTimestamp;
        }
        return false;
    }

    @Override
    public String toString() {
        return toMap().toString();
    }

}
