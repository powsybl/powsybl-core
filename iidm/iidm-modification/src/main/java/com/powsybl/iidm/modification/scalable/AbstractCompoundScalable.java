/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractCompoundScalable extends AbstractScalable implements CompoundScalable {

    protected Map<Scalable, Boolean> scalableActivityMap;

    protected AbstractCompoundScalable(double minInjection, double maxInjection, ScalingConvention scalingConvention) {
        super(minInjection, maxInjection, scalingConvention);
    }

    @Override
    public Collection<Scalable> getScalables() {
        Set<Scalable> scalables = new HashSet<>();
        scalableActivityMap.keySet().forEach(scalable -> {
            scalables.add(scalable);
            if (scalable instanceof CompoundScalable) {
                scalables.addAll(((CompoundScalable) scalable).getScalables());
            }
        });
        return scalables;
    }

    @Override
    public Collection<Scalable> getActiveScalables() {
        Set<Scalable> activeScalables = new HashSet<>();
        scalableActivityMap.keySet().stream().filter(scalableActivityMap::get).forEach(scalable -> {
            activeScalables.add(scalable);
            if (scalable instanceof CompoundScalable) {
                activeScalables.addAll(((CompoundScalable) scalable).getActiveScalables());
            }
        });
        return activeScalables;
    }

    @Override
    public void deactivateScalables(Set<Scalable> scalablesToDeactivate) {
        scalableActivityMap.keySet().forEach(scalable -> {
            if (scalablesToDeactivate.contains(scalable)) {
                scalableActivityMap.put(scalable, false);
            }
            if (scalable instanceof CompoundScalable) {
                ((CompoundScalable) scalable).deactivateScalables(scalablesToDeactivate);
            }
        });
    }

    @Override
    public void activateAllScalables() {
        scalableActivityMap.keySet().forEach(scalable -> {
            scalableActivityMap.put(scalable, true);
            if (scalable instanceof CompoundScalable) {
                ((CompoundScalable) scalable).activateAllScalables();
            }
        });
    }

    @Override
    public void activateScalables(Set<Scalable> scalablesToActivate) {
        scalableActivityMap.keySet().forEach(scalable -> {
            if (scalablesToActivate.contains(scalable)) {
                scalableActivityMap.put(scalable, true);
            }
            if (scalable instanceof CompoundScalable) {
                ((CompoundScalable) scalable).activateScalables(scalablesToActivate);
            }
        });
    }

    @Override
    public double getInitialInjection(ScalingConvention scalingConvention) {
        return getScalables().stream()
            .filter(scalable -> !(scalable instanceof CompoundScalable))
            .mapToDouble(scalable -> scalable.getInitialInjection(scalingConvention))
            .sum();
    }

    @Override
    public double getCurrentInjection(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);

        return getScalables().stream()
            .filter(scalable -> !(scalable instanceof CompoundScalable))
            .mapToDouble(scalable -> scalable.getCurrentInjection(n, scalingConvention))
            .sum();
    }

    @Override
    public void reset(Network n) {
        getScalables().forEach(scalable -> scalable.reset(n));
    }

    @Override
    public double getMaximumInjection(Network n, ScalingConvention powerConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(powerConvention);

        AtomicReference<Double> value = new AtomicReference<>((double) 0);
        Collection<Scalable> activeScalables = getActiveScalables();
        getScalables().stream().filter(scalable -> !(scalable instanceof CompoundScalable)).forEach(scalable -> {
            if (activeScalables.contains(scalable)) {
                value.updateAndGet(v -> v + scalable.getMaximumInjection(n, powerConvention));
            } else {
                value.updateAndGet(v -> v + scalable.getCurrentInjection(n, powerConvention));
            }
        });
        return value.get();
    }

    @Override
    public double getMinimumInjection(Network n, ScalingConvention powerConvention) {
        Objects.requireNonNull(n);

        AtomicReference<Double> value = new AtomicReference<>((double) 0);
        Collection<Scalable> activeScalables = getActiveScalables();
        getScalables().stream().filter(scalable -> !(scalable instanceof CompoundScalable)).forEach(scalable -> {
            if (activeScalables.contains(scalable)) {
                value.updateAndGet(v -> v + scalable.getMinimumInjection(n, powerConvention));
            } else {
                value.updateAndGet(v -> v + scalable.getCurrentInjection(n, powerConvention));
            }
        });
        return value.get();
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        for (Scalable scalable : getScalables()) {
            if (!(scalable instanceof CompoundScalable)) {
                scalable.filterInjections(n, injections, notFoundInjections);
            }
        }
    }

    @Override
    public void setInitialInjectionToNetworkValue(Network n) {
        for (Scalable scalable : getScalables()) {
            if (!(scalable instanceof CompoundScalable)) {
                scalable.setInitialInjectionToNetworkValue(n);
            }
        }
    }

}
