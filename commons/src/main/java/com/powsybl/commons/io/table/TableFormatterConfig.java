/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io.table;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * @author c.biasuzzi@techrain.it
 */
public class TableFormatterConfig {

    private static final String CONFIG_MODULE_NAME = "table-formatter";
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final String DEFAULT_LANGUAGE = DEFAULT_LOCALE.getLanguage();
    private static final char DEFAULT_CSV_SEPARATOR = ';';
    private static final String DEFAULT_INVALID_STRING = "inv";
    private static final boolean DEFAULT_PRINT_HEADER = true;
    private static final boolean DEFAULT_PRINT_TITLE = true;

    private final Locale locale;
    private final char csvSeparator;
    private final String invalidString;
    private final boolean printHeader;
    private final boolean printTitle;

    public static TableFormatterConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static TableFormatterConfig load(PlatformConfig platformConfig) {
        Optional<ModuleConfig> config = platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME);
        String language = config.flatMap(c -> c.getOptionalStringProperty("language")).orElse(DEFAULT_LANGUAGE);
        String separator = config.flatMap(c -> c.getOptionalStringProperty("separator")).orElse(Character.toString(DEFAULT_CSV_SEPARATOR));
        String invalidString = config.flatMap(c -> c.getOptionalStringProperty("invalid-string")).orElse(DEFAULT_INVALID_STRING);
        boolean printHeader = config.flatMap(c -> c.getOptionalBooleanProperty("print-header")).orElse(DEFAULT_PRINT_HEADER);
        boolean printTitle = config.flatMap(c -> c.getOptionalBooleanProperty("print-title")).orElse(DEFAULT_PRINT_TITLE);

        Locale locale = Locale.forLanguageTag(language);
        return new TableFormatterConfig(locale, separator.charAt(0), invalidString, printHeader, printTitle);
    }

    public TableFormatterConfig(Locale locale, char csvSeparator, String invalidString, boolean printHeader, boolean printTitle) {
        Objects.requireNonNull(locale);
        this.locale = locale;
        this.csvSeparator = csvSeparator;
        this.invalidString = invalidString;
        this.printHeader = printHeader;
        this.printTitle = printTitle;
    }

    public TableFormatterConfig(Locale locale, String invalidString, boolean printHeader, boolean printTitle) {
        this(locale, DEFAULT_CSV_SEPARATOR, invalidString, printHeader, printTitle);
    }

    public TableFormatterConfig(Locale locale, String invalidString) {
        this(locale, invalidString, DEFAULT_PRINT_HEADER, DEFAULT_PRINT_TITLE);
    }

    public TableFormatterConfig() {
        this(DEFAULT_LOCALE, DEFAULT_INVALID_STRING);
    }

    public Locale getLocale() {
        return locale;
    }

    public char getCsvSeparator() {
        return csvSeparator;
    }

    public String getInvalidString() {
        return invalidString;
    }

    public boolean getPrintHeader() {
        return printHeader;
    }

    public boolean getPrintTitle() {
        return printTitle;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [locale=" + locale +
                ", csvSeparator=" + csvSeparator +
                ", invalidString=" + invalidString +
                ", printHeader=" + printHeader +
                ", printTitle=" + printTitle +
                "]";
    }
}
