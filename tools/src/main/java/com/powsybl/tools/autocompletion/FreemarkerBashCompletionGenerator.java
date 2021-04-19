/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator based on freemarker
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class FreemarkerBashCompletionGenerator implements BashCompletionGenerator {

    private final Configuration config;

    public FreemarkerBashCompletionGenerator() {
        config = new Configuration(Configuration.VERSION_2_3_31);
        config.setClassForTemplateLoading(getClass(), "");
        config.setDefaultEncoding("UTF-8");
        config.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        config.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
    }

    public void test(Writer writer) throws IOException, TemplateException {
        BashOption option = new BashOption("opt", "ARG", OptionType.FILE);
        Template template = config.getTemplate("options.sh.ftl");
        Map<String, Object> context =  new HashMap<>();
        BashCommand command = new BashCommand("cmd", option, option);
        context.put("commands", ImmutableList.of(command, command));
        context.put("toolName", "itools");
        template.process(context, writer);
    }

    @Override
    public void generateCommands(String toolName, List<BashCommand> commands, Writer writer) {
        try {
            Template template = config.getTemplate("options.sh.ftl");
            Map<String, Object> context =  new HashMap<>();
            context.put("commands", commands);
            context.put("toolName", "itools");
            context.put("util", new TemplateUtil());
            template.process(context, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (TemplateException e) {
            throw new PowsyblException(e);
        }
    }
}
