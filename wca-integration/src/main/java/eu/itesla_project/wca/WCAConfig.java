/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WCAConfig {

    private static final float DEFAULT_REDUCED_VARIABLE_RATIO = 1f;

    private final Path xpressHome;

    private final float reducedVariableRatio;

    private final boolean debug;

    public static WCAConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("wca");
        Path xpressHome = config.getPathProperty("xpressHome");
        float reducedVariableRatio = config.getFloatProperty("reducedVariableRatio", DEFAULT_REDUCED_VARIABLE_RATIO);
        boolean debug = config.getBooleanProperty("debug", false);
        return new WCAConfig(xpressHome, reducedVariableRatio, debug);
    }

    public WCAConfig(Path xpressHome, float reducedVariableRatio, boolean debug) {
        Objects.requireNonNull(xpressHome);
        this.xpressHome = xpressHome;
        this.reducedVariableRatio = reducedVariableRatio;
        this.debug = debug;
    }

    public Path getXpressHome() {
        return xpressHome;
    }

    public float getReducedVariableRatio() {
        return reducedVariableRatio;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [xpressHome=" + xpressHome +
                ", reducedVariableRatio=" + reducedVariableRatio +
                ", debug=" + debug +
                "]";
    }
}
