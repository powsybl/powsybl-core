/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.ConfigVersion;

import java.util.Locale;
import java.util.Objects;

import static com.powsybl.commons.config.ConfigVersion.DEFAULT_CONFIG_VERSION;

/**
 * @author c.biasuzzi@techrain.it
 */
public class TableFormatterConfig implements Versionable {

    private static final String CONFIG_MODULE_NAME = "table-formatter";
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final String DEFAULT_LANGUAGE = DEFAULT_LOCALE.getLanguage();
    private static final char DEFAULT_CSV_SEPARATOR = ';';
    private static final String DEFAULT_INVALID_STRING = "inv";
    private static final boolean DEFAULT_PRINT_HEADER = true;
    private static final boolean DEFAULT_PRINT_TITLE = true;

    private ConfigVersion version = new ConfigVersion(DEFAULT_CONFIG_VERSION);

    private final Locale locale;
    private final char csvSeparator;
    private final String invalidString;
    private final boolean printHeader;
    private final boolean printTitle;

    public static TableFormatterConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static TableFormatterConfig load(PlatformConfig platformConfig) {
        return platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME)
                .map(config -> {
                    String language = config.getStringProperty("language", DEFAULT_LANGUAGE);
                    Locale locale = Locale.forLanguageTag(language);
                    String separator = config.getStringProperty("separator", Character.toString(DEFAULT_CSV_SEPARATOR));
                    String invalidString = config.getStringProperty("invalid-string", DEFAULT_INVALID_STRING);
                    boolean printHeader = config.getBooleanProperty("print-header", DEFAULT_PRINT_HEADER);
                    boolean printTitle = config.getBooleanProperty("print-title", DEFAULT_PRINT_TITLE);
                    return config.getOptionalStringProperty("version")
                            .map(v -> new TableFormatterConfig(new ConfigVersion(v), locale, separator.charAt(0), invalidString, printHeader, printTitle))
                            .orElseGet(() -> new TableFormatterConfig(locale, separator.charAt(0), invalidString, printHeader, printTitle));
                })
                .orElseGet(() -> new TableFormatterConfig(Locale.forLanguageTag(DEFAULT_LANGUAGE), Character.toString(DEFAULT_CSV_SEPARATOR).charAt(0),
                        DEFAULT_INVALID_STRING, DEFAULT_PRINT_HEADER, DEFAULT_PRINT_TITLE));

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

    public TableFormatterConfig(ConfigVersion version, Locale locale, char csvSeparator, String invalidString, boolean printHeader, boolean printTitle) {
        this(locale, csvSeparator, invalidString, printHeader, printTitle);
        this.version = version;
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

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }
}
