/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractVersion implements Version {

    protected final String repositoryName;

    protected final String mavenProjectVersion;

    protected final String gitVersion;

    protected final String gitBranch;

    protected final long buildTimestamp;

    protected AbstractVersion(String repositoryName, String mavenProjectVersion, String gitVersion, String gitBranch, long buildTimestamp) {
        this.repositoryName = Objects.requireNonNull(repositoryName);
        this.mavenProjectVersion = Objects.requireNonNull(mavenProjectVersion);
        this.gitVersion = Objects.requireNonNull(gitVersion);
        this.gitBranch = Objects.requireNonNull(gitBranch);
        this.buildTimestamp = buildTimestamp;
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public String getGitVersion() {
        return gitVersion;
    }

    @Override
    public String getMavenProjectVersion() {
        return mavenProjectVersion;
    }

    @Override
    public String getGitBranch() {
        return gitBranch;
    }

    @Override
    public long getBuildTimestamp() {
        return buildTimestamp;
    }

    public Map<String, String> toMap() {
        return ImmutableMap.of("repositoryName", repositoryName,
                               "mavenProjectVersion", mavenProjectVersion,
                               "gitVersion", gitVersion,
                               "gitBranch", gitBranch,
                               "buildTimestamp", new DateTime(buildTimestamp).toString());
    }

    @Override
    public String toString() {
        return toMap().toString();
    }
}
