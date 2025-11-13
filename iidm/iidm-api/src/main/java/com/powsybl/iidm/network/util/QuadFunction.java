package com.powsybl.iidm.network.util;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface QuadFunction<T, U, V, W, R> {
    default <X> QuadFunction<T, U, V, W, X> andThen(Function<? super R, ? extends X> after) {
        Objects.requireNonNull(after);
        return (t, u, v, w) -> after.apply(this.apply(t, u, v, w));
    }

    R apply(T var1, U var2, V var3, W var4);
}
