/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.connection.array;

import org.springframework.util.Assert;

/**
 * Numeric aggregate function applied by {@literal AROP} to the non-empty elements in a range.
 *
 * @author Yordan Tsintsov
 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
 * @since 4.2
 */
public class NumericAggregate {

	private static final NumericAggregate SUM_NUMERIC_AGGREGATE = new NumericAggregate(Type.SUM);
	private static final NumericAggregate MIN_NUMERIC_AGGREGATE = new NumericAggregate(Type.MIN);
	private static final NumericAggregate MAX_NUMERIC_AGGREGATE = new NumericAggregate(Type.MAX);

	private final Type type;

	private NumericAggregate(Type type) {
		Assert.notNull(type, "Type must not be null");
		this.type = type;
	}

	/**
	 * @return the aggregate function type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Return the sum of all numeric values in the range.
	 *
	 * @return the {@literal SUM} aggregate.
	 */
	public static NumericAggregate sum() {
		return SUM_NUMERIC_AGGREGATE;
	}

	/**
	 * Return the minimum numeric value in the range.
	 *
	 * @return the {@literal MIN} aggregate.
	 */
	public static NumericAggregate min() {
		return MIN_NUMERIC_AGGREGATE;
	}

	/**
	 * Return the maximum numeric value in the range.
	 *
	 * @return the {@literal MAX} aggregate.
	 */
	public static NumericAggregate max() {
		return MAX_NUMERIC_AGGREGATE;
	}

	/**
	 * Numeric aggregate function types.
	 */
	public enum Type {

		/**
		 * Sum of all numeric values.
		 */
		SUM,

		/**
		 * Minimum numeric value.
		 */
		MIN,

		/**
		 * Maximum numeric value.
		 */
		MAX
	}

}
