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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractCompoundScalable extends AbstractScalable implements CompoundScalable {

    protected Map<Scalable, Boolean> scalableActivityMap;

    @Override
    public Collection<Scalable> getScalables() {
        Set<Scalable> scalables = new HashSet<>();
        scalableActivityMap.keySet().stream().filter(scalableActivityMap::get).forEach(scalable -> {
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
    public double initialValue(Network n) {
        Objects.requireNonNull(n);

        double value = 0;
        for (Scalable scalable : getScalables()) {
            value += scalable.initialValue(n);
        }
        return value;
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        getScalables().forEach(scalable -> scalable.reset(n));
    }

    @Override
    public double maximumValue(Network n) {
        return maximumValue(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double maximumValue(Network n, ScalingConvention powerConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(powerConvention);

        double value = 0;
        for (Scalable scalable : getScalables()) {
            value += scalable.maximumValue(n, powerConvention);
        }
        return value;
    }

    @Override
    public double minimumValue(Network n) {
        return minimumValue(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double minimumValue(Network n, ScalingConvention powerConvention) {
        Objects.requireNonNull(n);

        double value = 0;
        for (Scalable scalable : getScalables()) {
            value += scalable.minimumValue(n, powerConvention);
        }
        return value;
    }

    @Override
    public double scale(Network n, double asked) {
        return scale(n, asked, ScalingConvention.GENERATOR);
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        for (Scalable scalable : getScalables()) {
            scalable.filterInjections(n, injections, notFoundInjections);
        }
    }

}
