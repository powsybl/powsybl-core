/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link ModuleConfigRepository} reading configuration from a YAML file.
 *
 * <p>String values may reference environment variables using the {@code ${VAR}} syntax.
 * A default value can be provided with {@code ${VAR:-default}}, which is used when the
 * environment variable is not set. Placeholders whose variable is not set and which have
 * no default are left unchanged, so unrelated placeholders (such as {@code ${user.home}}
 * resolved later by {@link PlatformEnv}) are preserved. Substitution is performed while the
 * file is loaded, before values are parsed, so it applies to every property type (integer,
 * boolean, path, list, ...) and not only to strings. To use an environment variable in a
 * typed value, quote it in the YAML, e.g. {@code max-iterations: "${MAX_ITERATIONS}"}.
 *
 * <p>In addition to environment variables, the reserved placeholder {@value #CONFIG_DIR_VARIABLE}
 * resolves to the absolute path of the directory containing the YAML file. It allows resources to
 * be referenced relatively to the configuration file, independently of the working directory, e.g.
 * {@code my-resource: ${config_dir}/resources/data.txt}. It takes precedence over an environment
 * variable of the same name.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class YamlModuleConfigRepository extends AbstractModuleConfigRepository {

    // ${VAR} or ${VAR:-default}, VAR being a shell-like identifier
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)(?::-([^}]*))?}");

    static final String CONFIG_DIR_VARIABLE = "config_dir";

    public YamlModuleConfigRepository(Path yamlConfigFile) {
        this(yamlConfigFile, System.getenv());
    }

    YamlModuleConfigRepository(Path yamlConfigFile, Map<String, String> env) {
        Objects.requireNonNull(yamlConfigFile);
        Objects.requireNonNull(env);

        Map<String, String> variables = new HashMap<>(env);
        Path configDir = yamlConfigFile.toAbsolutePath().getParent();
        variables.put(CONFIG_DIR_VARIABLE, configDir != null ? configDir.toString() : "");

        try (Reader reader = Files.newBufferedReader(yamlConfigFile, StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml();
            Object data = yaml.load(reader);
            if (!(data instanceof Map)) {
                throw new PowsyblException("Named modules are expected at the first level of the YAML");
            }
            for (Map.Entry<String, Object> e : ((Map<String, Object>) data).entrySet()) {
                String moduleName = e.getKey();
                if (!(e.getValue() instanceof Map)) {
                    throw new PowsyblException("Properties are expected at the second level of the YAML");
                }
                Map<Object, Object> properties = (Map<Object, Object>) substituteEnvVars(e.getValue(), variables);
                configs.put(moduleName, new MapModuleConfig(properties, yamlConfigFile.getFileSystem()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Recursively substitutes environment variable placeholders in every string value
     * found in the given loaded YAML value (scalars, lists and nested maps).
     */
    private static Object substituteEnvVars(Object value, Map<String, String> variables) {
        if (value instanceof String s) {
            return substituteEnvVars(s, variables);
        } else if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(entry.getKey(), substituteEnvVars(entry.getValue(), variables));
            }
            return result;
        } else if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            for (Object item : list) {
                result.add(substituteEnvVars(item, variables));
            }
            return result;
        }
        return value;
    }

    private static String substituteEnvVars(String value, Map<String, String> variables) {
        Matcher matcher = ENV_VAR_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String defaultValue = matcher.group(2);
            String replacement = variables.get(name);
            if (replacement == null) {
                replacement = defaultValue;
            }
            // leave the placeholder untouched when the variable is not set and no default is given
            String resolved = replacement != null ? replacement : matcher.group();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
