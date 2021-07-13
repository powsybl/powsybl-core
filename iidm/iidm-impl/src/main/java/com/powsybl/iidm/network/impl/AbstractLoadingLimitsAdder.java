/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.LoadingLimitsAdder;
import com.powsybl.iidm.network.validation.Validation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractLoadingLimitsAdder<L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> implements LoadingLimitsAdder<L, A> {

    private static final Comparator<Integer> ACCEPTABLE_DURATION_COMPARATOR = (acceptableDuraction1, acceptableDuraction2) -> acceptableDuraction2 - acceptableDuraction1;

    protected final OperationalLimitsOwner owner;

    protected double permanentLimit = Double.NaN;

    protected final TreeMap<Integer, LoadingLimits.TemporaryLimit> temporaryLimits = new TreeMap<>(ACCEPTABLE_DURATION_COMPARATOR);

    public class TemporaryLimitAdderImpl<B extends LoadingLimitsAdder<L, B>> implements TemporaryLimitAdder<B> {

        private String name;

        private double value = Double.NaN;

        private Integer acceptableDuration;

        private boolean fictitious = false;

        private boolean ensureNameUnicity = false;

        @Override
        public TemporaryLimitAdder<B> setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> setValue(double value) {
            this.value = value;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> setAcceptableDuration(int acceptableDuration) {
            this.acceptableDuration = acceptableDuration;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> setFictitious(boolean fictitious) {
            this.fictitious = fictitious;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> ensureNameUnicity() {
            this.ensureNameUnicity = true;
            return this;
        }

        @Override
        public B endTemporaryLimit() {
            Validation v = Validation.getDefault();
            v.checkTemporaryLimit(owner, value, acceptableDuration);
            checkAndGetUniqueName(v);
            temporaryLimits.put(acceptableDuration, new AbstractLoadingLimits.TemporaryLimitImpl(name, value, acceptableDuration, fictitious));
            return (B) AbstractLoadingLimitsAdder.this;
        }

        private void checkAndGetUniqueName(Validation v) {
            v.checkTemporaryLimitName(owner, name);
            if (ensureNameUnicity) {
                int i = 0;
                String uniqueName = name;
                while (i < Integer.MAX_VALUE && nameExists(uniqueName)) {
                    uniqueName = name + "#" + i;
                    i++;
                }
                name = uniqueName;
            }
        }

        private boolean nameExists(String name) {
            return temporaryLimits.values().stream().anyMatch(t -> t.getName().equals(name));
        }
    }

    AbstractLoadingLimitsAdder(OperationalLimitsOwner owner) {
        this.owner = owner;
    }

    @Override
    public A setPermanentLimit(double permanentLimit) {
        this.permanentLimit = permanentLimit;
        return (A) this;
    }

    @Override
    public TemporaryLimitAdder<A> beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl<>();
    }

    @Override
    public double getPermanentLimit() {
        return permanentLimit;
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        return Optional.ofNullable(temporaryLimits.get(acceptableDuration)).map(LoadingLimits.TemporaryLimit::getValue).orElse(Double.NaN);
    }

    @Override
    public boolean hasTemporaryLimits() {
        return !temporaryLimits.isEmpty();
    }

    protected void checkLoadingLimits() {
        Validation v = Validation.getDefault();
        v.checkPermanentLimit(owner, permanentLimit);
        v.checkTemporaryLimits(owner, permanentLimit, Collections.unmodifiableMap(temporaryLimits));
    }
}
