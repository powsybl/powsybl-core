/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.table.AsciiTableFormatter;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Version {

    static List<Version> list() {
        return new ServiceLoaderCache<>(Version.class).getServices();
    }

    static String getTableString() {
        return getTableString(PlatformConfig.defaultConfig());
    }

    static String getTableString(PlatformConfig platformConfig) {
        try (StringWriter writer = new StringWriter()) {
            try (TableFormatter formatter = new AsciiTableFormatter(writer,
                                                                    "Powsybl versions", TableFormatterConfig.load(platformConfig),
                                                                    new Column("Repository name"),
                                                                    new Column("Maven project version"),
                                                                    new Column("Git branch"),
                                                                    new Column("Git version"),
                                                                    new Column("Build timestamp"))) {
                list().forEach(version -> {
                    try {
                        formatter.writeCell(version.getRepositoryName())
                                 .writeCell(version.getMavenProjectVersion())
                                 .writeCell(version.getGitBranch())
                                 .writeCell(version.getGitVersion())
                                 .writeCell(new DateTime(version.getBuildTimestamp()).toString());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String getRepositoryName();

    String getGitVersion();

    String getMavenProjectVersion();

    String getGitBranch();

    long getBuildTimestamp();
}
