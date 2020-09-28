package com.github.beansoft.devkit.util;

/**
 * Generic callback with two params.
 *
 */
@FunctionalInterface
public interface PairProcessor<L,R> {

  boolean process(L left, R right);
}
