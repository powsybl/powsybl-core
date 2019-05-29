/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.tools;

import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface ConversionOption {

    void addImportOptions(Options options);

    void addExportOptions(Options options, boolean required);

    ImportConfig createImportConfig(CommandLine line);

    Network read(CommandLine line, ToolRunningContext context) throws IOException;

    Network read(String commandOption, CommandLine line, ToolRunningContext context) throws IOException;

    void write(Network network, CommandLine line, ToolRunningContext context) throws IOException;
}
