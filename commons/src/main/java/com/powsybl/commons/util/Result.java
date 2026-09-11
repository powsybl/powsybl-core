/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * Either a value or the reason why it could not be produced.
 *
 * <p>This is the two track of railway oriented programming, for computations that can legitimately have no answer:
 * they return a {@code Result} rather than signalling with an exception, and callers stay on the success track by
 * chaining with {@link #map} and {@link #flatMap}, which do nothing to a failure but carry its reason along. The
 * reason therefore travels to the one place that decides what to do with it, without every step in between having
 * to know, and the compiler will not let a caller read a value that was never produced.</p>
 *
 * <p>An expected outcome is what this is for: a lookup that may find nothing, an input a mapping cannot express, a
 * request a service can refuse. A broken invariant stays an exception.</p>
 *
 * <p>Both tracks can be read by pattern matching, which the compiler checks for exhaustiveness:</p>
 * <pre>{@code
 * switch (parse(text)) {
 *     case Result.Success(Integer parsed) -> use(parsed);
 *     case Result.Failure(String reason) -> report(reason);
 * }
 * }</pre>
 *
 * <p>Instances are immutable and neither track carries {@code null}.</p>
 *
 * @param <T> the type of the value produced on the success track
 * @param <E> the type of the reason carried on the failure track
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
public sealed interface Result<T, E> {

    /** A computation that produced a value. */
    record Success<T, E>(T value) implements Result<T, E> {
        public Success {
            Objects.requireNonNull(value);
        }
    }

    /** A computation that produced no value, and the reason why. */
    record Failure<T, E>(E reason) implements Result<T, E> {
        public Failure {
            Objects.requireNonNull(reason);
        }
    }

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E reason) {
        return new Failure<>(reason);
    }

    /** Apply the given function to the value if there is one, and carry the reason of the failure otherwise. */
    default <U> Result<U, E> map(Function<? super T, ? extends U> function) {
        Objects.requireNonNull(function);
        return switch (this) {
            case Success(T value) -> success(function.apply(value));
            case Failure(E reason) -> failure(reason);
        };
    }

    /**
     * Continue with the given computation if there is a value, and carry the reason of the failure otherwise.
     *
     * <p>This is how a step that can fail in turn is chained: the result stays flat, so a chain of them reports
     * the reason of the first step that failed and never runs the ones after it.</p>
     */
    default <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> function) {
        Objects.requireNonNull(function);
        return switch (this) {
            case Success(T value) -> Objects.requireNonNull(function.apply(value));
            case Failure(E reason) -> failure(reason);
        };
    }

    /** Leave the two tracks by answering with the given function of the value, or of the reason of the failure. */
    default <R> R fold(Function<? super T, ? extends R> onSuccess, Function<? super E, ? extends R> onFailure) {
        Objects.requireNonNull(onSuccess);
        Objects.requireNonNull(onFailure);
        return switch (this) {
            case Success(T value) -> onSuccess.apply(value);
            case Failure(E reason) -> onFailure.apply(reason);
        };
    }
}
