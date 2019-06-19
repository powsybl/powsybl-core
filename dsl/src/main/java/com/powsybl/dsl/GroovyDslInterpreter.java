/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dsl;

import com.google.common.io.CharSource;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * An object with the ability to interpret a groovy script,
 * based on a language defined through an instance of {@link GroovyDsl}.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class GroovyDslInterpreter<Context> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyDslInterpreter.class);

    private final GroovyDsl<Context> dsl;

    public GroovyDslInterpreter(GroovyDsl<Context> dsl) {
        this.dsl = Objects.requireNonNull(dsl);
    }

    public void interprete(String script, Context context) {
        GroovyCodeSource src = new GroovyCodeSource(Objects.requireNonNull(script), "script", GroovyShell.DEFAULT_CODE_BASE);
        interprete(src, context);
    }

    public void interprete(CharSource charSource, Context context) {
        Objects.requireNonNull(charSource);
        try {
            interprete(charSource.read(), context);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void interprete(GroovyCodeSource script, Context context) {
        LOGGER.debug("Starting configuration of security analysis inputs, based on groovy DSL.");

        Binding binding = new Binding();
        dsl.enable(binding, context);

        LOGGER.debug("Evaluating security analysis configuration DSL file.");
        DslLoader.createShell(binding).evaluate(script);
    }
}
