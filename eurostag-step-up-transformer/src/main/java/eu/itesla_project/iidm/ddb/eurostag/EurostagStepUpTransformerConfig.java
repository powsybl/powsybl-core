/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.loadflow.api.LoadFlowFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagStepUpTransformerConfig {

    private static final boolean DEFAULT_MOVE_AUX = true;
    private static final boolean DEFAULT_NO_REACTIVE_LIMITS = false;
    private static final boolean DEFAULT_NO_ACTIVE_LOSSES = true;
    private static final boolean DEFAULT_REMOVE_ALREADY_EXISTING_STATORS = false;

    private final List<Path> ddbPath;
    private final String genDictFileName;
    private final String auxDictFileName;
    private final String statorVoltageLevelsFileName;
    private final Class<? extends LoadFlowFactory> loadFlowFactoryClass;
    private final boolean moveAux;
    private final boolean noReactiveLimits;
    private final boolean noActiveLosses;
    private final boolean removeAlreadyExistingStators;

    public static EurostagStepUpTransformerConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig(EurostagStepUpTransformerPostProcessor.class.getSimpleName());
        List<Path> ddbPath = config.getPathListProperty("ddbPath");
        String genDictFileName = config.getStringProperty("genDict");
        String auxDictFileName = config.getStringProperty("auxDict");
        String statorVoltageLevelsFileName = config.getStringProperty("statorVoltageLevelsFileName");
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("loadFlowFactoryClass", LoadFlowFactory.class);
        boolean moveAux = config.getBooleanProperty("moveAux", DEFAULT_MOVE_AUX);
        boolean noReactiveLimits = config.getBooleanProperty("noReactiveLimits", DEFAULT_NO_REACTIVE_LIMITS);
        boolean noActiveLosses = config.getBooleanProperty("noActiveLosses", DEFAULT_NO_ACTIVE_LOSSES);
        boolean removeAlreadyExistingStators = config.getBooleanProperty("removeAlreadyExistingStators", DEFAULT_REMOVE_ALREADY_EXISTING_STATORS);
        return new EurostagStepUpTransformerConfig(ddbPath, genDictFileName, auxDictFileName, statorVoltageLevelsFileName, loadFlowFactoryClass,
                                                   moveAux, noReactiveLimits, noActiveLosses, removeAlreadyExistingStators);
    }

    public EurostagStepUpTransformerConfig() {
        this(Arrays.asList(), "???", "???", "???", null, DEFAULT_MOVE_AUX, DEFAULT_NO_REACTIVE_LIMITS, DEFAULT_NO_ACTIVE_LOSSES,
                DEFAULT_REMOVE_ALREADY_EXISTING_STATORS);
    }

    public EurostagStepUpTransformerConfig(List<Path> ddbPath, String genDictFileName, String auxDictFileName, String statorVoltageLevelsFileName,
                                           Class<? extends LoadFlowFactory> loadFlowFactoryClass, boolean moveAux,
                                           boolean noReactiveLimits, boolean noActiveLosses, boolean removeAlreadyExistingStators) {
        this.ddbPath = ddbPath;
        this.genDictFileName = genDictFileName;
        this.auxDictFileName = auxDictFileName;
        this.statorVoltageLevelsFileName = statorVoltageLevelsFileName;
        this.loadFlowFactoryClass = loadFlowFactoryClass;
        this.moveAux = moveAux;
        this.noReactiveLimits = noReactiveLimits;
        this.noActiveLosses = noActiveLosses;
        this.removeAlreadyExistingStators = removeAlreadyExistingStators;
    }

    public List<Path> getDdbPath() {
        return ddbPath;
    }

    public String getGenDictFileName() {
        return genDictFileName;
    }

    public String getAuxDictFileName() {
        return auxDictFileName;
    }

    public String getStatorVoltageLevelsFileName() {
        return statorVoltageLevelsFileName;
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
        return loadFlowFactoryClass;
    }

    public boolean isNoReactiveLimits() {
        return noReactiveLimits;
    }

    public boolean isNoActiveLosses() {
        return noActiveLosses;
    }

    public boolean isMoveAux() {
        return moveAux;
    }

    public boolean isRemoveAlreadyExistingStators() {
        return removeAlreadyExistingStators;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ddbPath=" + ddbPath +
                ", genDictFileName=" + genDictFileName +
                ", auxDictFileName=" + auxDictFileName +
                ", statorVoltageLevelsFileName=" + statorVoltageLevelsFileName +
                ", loadFlowFactoryClass=" + loadFlowFactoryClass +
                ", moveAux=" + moveAux +
                ", noReactiveLimits=" + noReactiveLimits +
                ", noActiveLosses=" + noActiveLosses +
                ", removeAlreadyExistingStators=" + removeAlreadyExistingStators +
                "]";
    }
}
