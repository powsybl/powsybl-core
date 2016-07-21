/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.constraints;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.iidm.network.TwoTerminalsConnectable;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.security.LimitViolationFilter;
import eu.itesla_project.modules.security.LimitViolationType;
import eu.itesla_project.modules.security.Security;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ConstraintsModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintsModifier.class);

    private Network network;
    private ConstraintsModifierConfig config;

    public ConstraintsModifier(Network network) {
        this(network, ConstraintsModifierConfig.load());
    }

    public ConstraintsModifier(Network network, ConstraintsModifierConfig config) {
        LOGGER.info(config.toString());
        this.network = network;
        this.config = config;
    }

    public void looseConstraints(String stateId) {
        looseConstraints(stateId, 0f, false);
    }

    public void looseConstraints(String stateId, float margin) {
        looseConstraints(stateId, margin, false);
    }

    public void looseConstraints(String stateId, float margin, boolean applyToBaseCase) {
        if ( network.getStateManager().getStateIds().contains(stateId) ) {
            String workingStateId = network.getStateManager().getWorkingStateId();
            network.getStateManager().setWorkingState(stateId);
            List<LimitViolation> violations = Security.checkLimits(network);
            looseConstraints(stateId, violations, margin, applyToBaseCase);
            network.getStateManager().setWorkingState(workingStateId);
        } else {
            throw new RuntimeException("No "+stateId+" in network "+network.getId()+": cannot loose constraints");
        }
    }

    public void looseConstraints(String stateId, List<LimitViolation> violations) {
        looseConstraints(stateId, violations, 0f, false);
    }

    public void looseConstraints(String stateId, List<LimitViolation> violations, float margin) {
        looseConstraints(stateId, violations, margin, false);
    }

    public void looseConstraints(String stateId, List<LimitViolation> violations, float margin, boolean applyToBaseCase) {
        Objects.requireNonNull(stateId, "state id is null");
        Objects.requireNonNull(violations, "violations is null");
        if ( network.getStateManager().getStateIds().contains(stateId) ) {
            String workingStateId = network.getStateManager().getWorkingStateId();
            network.getStateManager().setWorkingState(stateId);
            LOGGER.info("Loosening constraints of network {}, state {}, using margin {}", 
                    network.getId(), 
                    network.getStateManager().getWorkingStateId(), 
                    margin);
            LimitViolationFilter violationsFilter = new LimitViolationFilter(new HashSet<LimitViolationType>(config.getViolationsTypes()), 0);
            List<LimitViolation> filteredViolations = violationsFilter.apply(violations);
            String report = Security.printLimitsViolations(violations, violationsFilter);;
            if (report != null) {
                LOGGER.debug("Fixing constraints of network {}, state {}, causing the following {} violations:\n{}", 
                        network.getId(), 
                        network.getStateManager().getWorkingStateId(), 
                        filteredViolations.size(), 
                        report);
            }
            for (LimitViolation violation : filteredViolations) {
                LOGGER.debug("Fixing the constraints causing the {} violation on equipment {}", 
                        violation.getLimitType(),
                        violation.getSubject().getId());
                switch (violation.getLimitType()) {
                case CURRENT: 
                    setNewCurrentLimit(stateId, violation, margin, applyToBaseCase);
                    break;
                case HIGH_VOLTAGE:
                    setNewHighVoltageLimit(stateId, violation, margin, applyToBaseCase);
                    break;
                case LOW_VOLTAGE:
                    setNewLowVoltageLimit(stateId, violation, margin, applyToBaseCase);
                    break;
                }
            }
            network.getStateManager().setWorkingState(workingStateId);
        } else {
            throw new RuntimeException("No "+stateId+" in network "+network.getId()+": cannot loose constraints");
        }
    }

    private void setNewCurrentLimit(String stateId, LimitViolation violation, float margin, boolean applyToBaseCase) {
        TwoTerminalsConnectable violatedBranch = (TwoTerminalsConnectable) violation.getSubject();
        // not sure if I need to reload the branch from the network ...
        TwoTerminalsConnectable branch = null;
        if ( violatedBranch instanceof Line )
            branch = network.getLine(violatedBranch.getId());
        else if ( violatedBranch instanceof TwoWindingsTransformer )
            branch = network.getTwoWindingsTransformer(violatedBranch.getId());
        if ( branch != null ) {
            float newLimit = getNewUpperLimit(violation, margin);
            if ( branch.getTerminal1().getI() == violation.getValue() ) {
                LOGGER.debug("State {}: changing current limit 1 of branch {}: {} -> {}", 
                        stateId, 
                        branch.getId(), 
                        violation.getLimit(), 
                        newLimit);
                branch.newCurrentLimits1().setPermanentLimit(newLimit).add();
                if ( applyToBaseCase && !StateManager.INITIAL_STATE_ID.equals(stateId) ) { // change the limit also to basecase
                    String initialStateId = StateManager.INITIAL_STATE_ID;
                    network.getStateManager().setWorkingState(initialStateId);
                    if ( violatedBranch instanceof Line )
                        branch = network.getLine(violatedBranch.getId());
                    else if ( violatedBranch instanceof TwoWindingsTransformer )
                        branch = network.getTwoWindingsTransformer(violatedBranch.getId());
                    if ( branch != null ) {
                        LOGGER.debug("State {}: changing current limit 1 of branch {}: {} -> {}", 
                                initialStateId, 
                                branch.getId(), 
                                violation.getLimit(), 
                                newLimit);
                        branch.newCurrentLimits1().setPermanentLimit(newLimit).add();
                    } else {
                        LOGGER.warn("State {}: cannot change current limit of branch {}: no branch with this id in the network", 
                                initialStateId, 
                                violatedBranch.getId());
                    }
                    network.getStateManager().setWorkingState(stateId);
                }
            } else if ( branch.getTerminal2().getI() == violation.getValue() ) {
                LOGGER.debug("State {}: changing current limit 2 of branch {}: {} -> {}",
                        stateId, 
                        branch.getId(), 
                        violation.getLimit(), 
                        newLimit);
                branch.newCurrentLimits2().setPermanentLimit(newLimit).add();
                if ( applyToBaseCase && !StateManager.INITIAL_STATE_ID.equals(stateId) ) { // change the limit also to basecase
                    String initialStateId = StateManager.INITIAL_STATE_ID;
                    network.getStateManager().setWorkingState(initialStateId);
                    if ( violatedBranch instanceof Line )
                        branch = network.getLine(violatedBranch.getId());
                    else if ( violatedBranch instanceof TwoWindingsTransformer )
                        branch = network.getTwoWindingsTransformer(violatedBranch.getId());
                    if ( branch != null ) {
                        LOGGER.debug("State {}: changing current limit 2 of branch {}: {} -> {}", 
                                initialStateId, 
                                branch.getId(), 
                                violation.getLimit(), 
                                newLimit);
                        branch.newCurrentLimits2().setPermanentLimit(newLimit).add();
                    } else {
                        LOGGER.warn("State {}: cannot change current limit of branch {}: no branch with this id in the network", 
                                initialStateId, 
                                violatedBranch.getId());
                    }
                    network.getStateManager().setWorkingState(stateId);
                }
            }
        } else {
            LOGGER.warn("State {}: cannot change current limit of branch {}: no branch with this id in the network", 
                    stateId, 
                    violatedBranch.getId());
        }
    }

    private void setNewHighVoltageLimit(String stateId, LimitViolation violation, float margin, boolean applyToBaseCase) {
        VoltageLevel violatedVoltageLevel = (VoltageLevel) violation.getSubject();
        VoltageLevel voltageLevel = network.getVoltageLevel(violatedVoltageLevel.getId());
        if ( voltageLevel != null ) {
            if ( violation.getValue() > voltageLevel.getHighVoltageLimit() ) { // it could already have been fixed
                float newLimit = getNewUpperLimit(violation, margin);
                LOGGER.debug("State {}: changing high voltage limit of voltage level {}: {} -> {}", 
                        stateId, 
                        voltageLevel.getId(), 
                        violation.getLimit(), 
                        newLimit);
                voltageLevel.setHighVoltageLimit(newLimit);
                if ( applyToBaseCase && !StateManager.INITIAL_STATE_ID.equals(stateId) ) { // change the limit also to basecase
                    String initialStateId = StateManager.INITIAL_STATE_ID;
                    network.getStateManager().setWorkingState(initialStateId);
                    voltageLevel = network.getVoltageLevel(violatedVoltageLevel.getId());
                    if ( voltageLevel != null ) {
                        LOGGER.debug("State {}: changing high voltage limit of voltage level {}: {} -> {}", 
                                initialStateId, 
                                voltageLevel.getId(), 
                                violation.getLimit(), 
                                newLimit);
                        voltageLevel.setHighVoltageLimit(newLimit);
                    } else {
                        LOGGER.warn("State {}: cannot change high voltage limit of voltage level {}: no voltage level with this id in the network", 
                                initialStateId, 
                                violatedVoltageLevel.getId());
                    }
                    network.getStateManager().setWorkingState(stateId);
                }
            }
        } else {
            LOGGER.warn("State {}: cannot change high voltage limit of voltage level {}: no voltage level with this id in the network", 
                    stateId, 
                    violatedVoltageLevel.getId());
        }
    }

    private void setNewLowVoltageLimit(String stateId, LimitViolation violation, float margin, boolean applyToBaseCase) {
        VoltageLevel violatedVoltageLevel = (VoltageLevel) violation.getSubject();
        VoltageLevel voltageLevel = network.getVoltageLevel(violatedVoltageLevel.getId());
        if ( voltageLevel != null ) {
            if ( violation.getValue() < voltageLevel.getLowVoltageLimit() ) { // it could already have been fixed
                float newLimit = getNewLowerLimit(violation, margin);
                LOGGER.debug("State {}: changing low voltage limit of voltage level {}: {} -> {}", 
                        stateId, 
                        voltageLevel.getId(), 
                        violation.getLimit(), 
                        newLimit);
                voltageLevel.setLowVoltageLimit(newLimit);
                if ( applyToBaseCase && !StateManager.INITIAL_STATE_ID.equals(stateId) ) { // change the limit also to basecase
                    String initialStateId = StateManager.INITIAL_STATE_ID;
                    network.getStateManager().setWorkingState(initialStateId);
                    voltageLevel = network.getVoltageLevel(violatedVoltageLevel.getId());
                    if ( voltageLevel != null ) {
                        LOGGER.debug("State {}: changing low voltage limit of voltage level {}: {} -> {}", 
                                initialStateId, 
                                voltageLevel.getId(), 
                                violation.getLimit(), 
                                newLimit);
                        voltageLevel.setLowVoltageLimit(newLimit);
                    } else {
                        LOGGER.warn("State {}: cannot change high voltage limit of voltage level {}: no voltage level with this id in the network", 
                                initialStateId, 
                                violatedVoltageLevel.getId());
                    }
                    network.getStateManager().setWorkingState(stateId);
                }
            }
        } else {
            LOGGER.warn("State {}: cannot change low voltage limit of voltage level {}: no voltage level with this id in the network", 
                    stateId, 
                    violatedVoltageLevel.getId());
        }
    }

    private float getNewUpperLimit(LimitViolation violation, float margin) {
        float newLimit = 9999;
        if ( config.isInAreaOfInterest(violation, network) ) {
            float increment = (float) ((violation.getLimit() == 0) 
                    ? Math.ceil(violation.getValue()*100) 
                            : Math.ceil((violation.getValue()-violation.getLimit())*100/violation.getLimit()));
            increment += margin;
            newLimit = (violation.getLimit() == 0) 
                    ? (increment/100) 
                            : (violation.getLimit()+(violation.getLimit()*increment/100));
        }
        return newLimit;
    }

    private float getNewLowerLimit(LimitViolation violation, float margin) {
        float newLimit = -9999;
        if ( config.isInAreaOfInterest(violation, network) ) {
            float increment = (float) ((violation.getLimit() == 0) 
                    ? Math.ceil(-violation.getValue()*100) 
                            : Math.ceil((violation.getLimit()-violation.getValue())*100/violation.getLimit()));
            increment += margin;
            newLimit = (violation.getLimit() == 0) 
                    ? (increment/100) 
                            : (violation.getLimit()-(violation.getLimit()*increment/100));
        }
        return newLimit;
    }

}
