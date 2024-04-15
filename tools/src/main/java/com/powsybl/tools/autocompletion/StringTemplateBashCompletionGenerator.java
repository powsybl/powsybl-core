/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */package com.powsybl.tools.autocompletion;

import org.apache.commons.io.IOUtils;
import org.stringtemplate.v4.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Generator based on stringtemplate lib
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class StringTemplateBashCompletionGenerator implements BashCompletionGenerator {

    public StringTemplateBashCompletionGenerator() {
        // Empty body
    }

    private STGroup createTemplateGroup() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("completion.sh.stg")) {
            return new STGroupString(IOUtils.toString(is, StandardCharsets.UTF_8));
        }
    }

    @Override
    public void generateCommands(String toolName, List<BashCommand> commands, Writer writer) {
        try {
            STGroup group = createTemplateGroup();
            ST template = group.getInstanceOf("completionScript");
            template.add("toolName", toolName);
            template.add("commands", commands);
            template.write(new AutoIndentWriter(writer));
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }
}
