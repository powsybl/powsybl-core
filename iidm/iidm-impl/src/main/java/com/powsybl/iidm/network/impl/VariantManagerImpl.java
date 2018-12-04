/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Ints;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VariantManagerImpl implements VariantManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantManagerImpl.class);

    private static final int INITIAL_VARIANT_INDEX = 0;

    private VariantContext variantContext;

    private final ObjectStore objectStore;

    private final BiMap<String, Integer> id2index = HashBiMap.create();

    private int variantArraySize;

    private final Deque<Integer> unusedIndexes = new ArrayDeque<>();

    private final ReentrantLock variantLock = new ReentrantLock();

    VariantManagerImpl(ObjectStore objectStore) {
        this.variantContext = new MultiVariantContext(INITIAL_VARIANT_INDEX);
        this.objectStore = objectStore;
        // the network has always a zero index initial variant
        id2index.put(VariantManagerConstants.INITIAL_VARIANT_ID, INITIAL_VARIANT_INDEX);
        variantArraySize = INITIAL_VARIANT_INDEX + 1;
    }

    VariantContext getVariantContext() {
        return variantContext;
    }

    @Override
    public Collection<String> getVariantIds() {
        variantLock.lock();
        try {
            return Collections.unmodifiableSet(id2index.keySet());
        } finally {
            variantLock.unlock();
        }
    }

    int getVariantArraySize() {
        return variantArraySize;
    }

    int getVariantCount() {
        return id2index.size();
    }

    Collection<Integer> getVariantIndexes() {
        return id2index.values();
    }

    private int getVariantIndex(String variantId) {
        Integer index = id2index.get(variantId);
        if (index == null) {
            throw new PowsyblException("Variant '" + variantId + "' not found");
        }
        return index;
    }

    @Override
    public String getWorkingVariantId() {
        variantLock.lock();
        try {
            int index = variantContext.getVariantIndex();
            return id2index.inverse().get(index);
        } finally {
            variantLock.unlock();
        }
    }

    @Override
    public void setWorkingVariant(String variantId) {
        variantLock.lock();
        try {
            int index = getVariantIndex(variantId);
            variantContext.setVariantIndex(index);
        } finally {
            variantLock.unlock();
        }
    }

    private Iterable<MultiVariantObject> getStafulObjects() {
        return FluentIterable.from(objectStore.getAll()).filter(MultiVariantObject.class);
    }

    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId) {
        cloneVariant(sourceVariantId, Arrays.asList(targetVariantId));
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        if (targetVariantIds.isEmpty()) {
            throw new IllegalArgumentException("Empty target variant id list");
        }
        LOGGER.debug("Creating variants {}", targetVariantIds);
        variantLock.lock();
        try {
            int sourceIndex = getVariantIndex(sourceVariantId);
            int initVariantArraySize = variantArraySize;
            int extendedCount = 0;
            List<Integer> recycled = new ArrayList<>();
            for (String targetVariantId : targetVariantIds) {
                if (id2index.containsKey(targetVariantId)) {
                    throw new PowsyblException("Target variant '" + targetVariantId + "' already exists");
                }
                if (unusedIndexes.isEmpty()) {
                    // extend variant array size
                    id2index.put(targetVariantId, variantArraySize);
                    variantArraySize++;
                    extendedCount++;
                } else {
                    // recycle an index
                    int index = unusedIndexes.pollLast();
                    id2index.put(targetVariantId, index);
                    recycled.add(index);
                }
            }
            if (!recycled.isEmpty()) {
                int[] indexes = Ints.toArray(recycled);
                for (MultiVariantObject obj : getStafulObjects()) {
                    obj.allocateVariantArrayElement(indexes, sourceIndex);
                }
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Recycling variant array indexes {}", Arrays.toString(indexes));
                }
            }
            if (extendedCount > 0) {
                for (MultiVariantObject obj : getStafulObjects()) {
                    obj.extendVariantArraySize(initVariantArraySize, extendedCount, sourceIndex);
                }
                LOGGER.trace("Extending variant array size to {} (+{})", variantArraySize, extendedCount);
            }
        } finally {
            variantLock.unlock();
        }
    }

    @Override
    public void removeVariant(String variantId) {
        variantLock.lock();
        try {
            if (VariantManagerConstants.INITIAL_VARIANT_ID.equals(variantId)) {
                throw new PowsyblException("Removing initial variant is forbidden");
            }
            int index = getVariantIndex(variantId);
            id2index.remove(variantId);
            LOGGER.debug("Removing variant '{}'", variantId);
            if (index == variantArraySize - 1) {
                // remove consecutive unsused index starting from the end
                int number = 0; // number of elements to remove
                for (int j = index; j >= 0; j--) {
                    if (id2index.containsValue(j)) {
                        break;
                    } else {
                        number++;
                        unusedIndexes.remove(j);
                    }
                }
                // reduce variant array size
                for (MultiVariantObject obj : getStafulObjects()) {
                    obj.reduceVariantArraySize(number);
                }
                variantArraySize -= number;
                LOGGER.trace("Reducing variant array size to {}", variantArraySize);
            } else {
                unusedIndexes.add(index);
                // delete variant array element at the unused index to avoid memory leak
                // (so that variant data can be garbage collected)
                for (MultiVariantObject obj : getStafulObjects()) {
                    obj.deleteVariantArrayElement(index);
                }
                LOGGER.trace("Deleting variant array element at index {}", index);
            }
            // if the removed variant is the working variant, unset the working variant
            variantContext.resetIfVariantIndexIs(index);
        } finally {
            variantLock.unlock();
        }
    }

    @Override
    public void allowVariantMultiThreadAccess(boolean allow) {
        variantLock.lock();
        try {
            if (allow) {
                int index = variantContext.getVariantIndex();
                variantContext = ThreadLocalMultiVariantContext.INSTANCE;
                variantContext.setVariantIndex(index);
            } else {
                variantContext = new MultiVariantContext(variantContext.getVariantIndex());
            }
        } finally {
            variantLock.unlock();
        }
    }

    @Override
    public boolean isVariantMultiThreadAccessAllowed() {
        variantLock.lock();
        try {
            return variantContext instanceof ThreadLocalMultiVariantContext;
        } finally {
            variantLock.unlock();
        }
    }

    void forEachVariant(Runnable r) {
        variantLock.lock();
        int currentVariantIndex = variantContext.getVariantIndex();
        try {
            for (int index : id2index.values()) {
                variantContext.setVariantIndex(index);
                r.run();
            }
        } finally {
            variantContext.setVariantIndex(currentVariantIndex);
            variantLock.unlock();
        }
    }

}
