/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.list;

import com.powsybl.commons.util.WeakListenerList;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
// The listeners field is not part of the list's value and must not participate in equality.
// Equality is intentionally delegated to ArrayList, which correctly implements the List contract.
@SuppressWarnings("java:S2160")
public class ObservableArrayList<E> extends ArrayList<E> implements ObservableList<E> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final WeakListenerList<ListChangeListener<E>> listeners = new WeakListenerList<>();

    public ObservableArrayList() {
        super();
    }

    public ObservableArrayList(Collection<? extends E> c) {
        super(c);
    }

    /**
     * Adds a listener to this list.
     * <p>
     * <strong>Note:</strong> listeners are held weakly by the list. The caller is responsible
     * for retaining a strong reference to the listener for as long as notifications are needed;
     * otherwise the listener may be garbage-collected and silently stop receiving events.
     */
    public void addListener(ListChangeListener<E> listener) {
        listeners.add(listener);
    }

    public void removeListener(ListChangeListener<E> listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean add(E e) {
        int index = size();
        boolean result = super.add(e);
        if (result) {
            fireAdded(List.of(e), index);
        }
        return result;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        fireAdded(List.of(element), index);
    }

    @Override
    public void addFirst(E element) {
        super.addFirst(element);
        fireAdded(List.of(element), 0);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        int fromIndex = size();
        boolean result = super.addAll(c);
        if (result) {
            fireAdded(List.copyOf(subList(fromIndex, size())), fromIndex);
        }
        return result;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        boolean result = super.addAll(index, c);
        if (result) {
            fireAdded(List.copyOf(subList(index, index + c.size())), index);
        }
        return result;
    }

    @Override
    public E remove(int index) {
        E removed = super.remove(index);
        fireRemoved(List.of(removed), index);
        return removed;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index < 0) {
            return false;
        }
        E removed = super.remove(index);
        fireRemoved(List.of(removed), index);
        return true;
    }

    @Override
    public E removeFirst() {
        E removed = super.removeFirst();
        fireRemoved(List.of(removed), 0);
        return removed;
    }

    @Override
    public E removeLast() {
        int index = size();
        E removed = super.removeLast();
        fireRemoved(List.of(removed), index - 1);
        return removed;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        List<E> toRemove = stream().filter(c::contains).toList();
        boolean result = super.removeAll(c);
        if (result) {
            fireRemoved(toRemove, 0);
        }
        return result;
    }

    @Override
    public boolean removeIf(@NonNull Predicate<? super E> filter) {
        List<E> toRemove = stream().filter(filter).toList();
        boolean result = super.removeIf(filter);
        if (result) {
            fireRemoved(toRemove, 0);
        }
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        List<E> removed = List.copyOf(subList(fromIndex, toIndex));
        super.removeRange(fromIndex, toIndex);
        fireRemoved(removed, fromIndex);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        List<E> toRemove = stream().filter(e -> !c.contains(e)).toList();
        boolean result = super.retainAll(c);
        if (result) {
            fireRemoved(toRemove, 0);
        }
        return result;
    }

    @Override
    public void replaceAll(@NonNull UnaryOperator<E> operator) {
        List<E> oldElements = List.copyOf(this);
        super.replaceAll(operator);
        fireSet(oldElements, List.copyOf(this), 0);
    }

    @Override
    public E set(int index, E element) {
        E old = super.set(index, element);
        fireSet(List.of(old), List.of(element), index);
        return old;
    }

    @Override
    public void sort(Comparator<? super E> comparator) {
        super.sort(comparator);
        fireSorted(comparator);
    }

    @Override
    public void clear() {
        super.clear();
        fireCleared();
    }

    private void fireAdded(List<E> added, int fromIndex) {
        listeners.notify(l -> l.onAdded(added, fromIndex));
    }

    private void fireRemoved(List<E> removed, int fromIndex) {
        listeners.notify(l -> l.onRemoved(removed, fromIndex));
    }

    private void fireSet(List<E> oldElements, List<E> newElements, int fromIndex) {
        listeners.notify(l -> l.onSet(oldElements, newElements, fromIndex));
    }

    private void fireSorted(Comparator<? super E> comparator) {
        listeners.notify(l -> l.onSorted(comparator));
    }

    private void fireCleared() {
        listeners.notify(ListChangeListener::onCleared);
    }
}
