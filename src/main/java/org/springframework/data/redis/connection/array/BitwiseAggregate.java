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
 * Bitwise aggregate function applied by {@literal AROP} to the non-empty elements in a range. Bitwise functions treat
 * each value as an integer (floats are truncated toward zero).
 *
 * @author Yordan Tsintsov
 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
 * @since 4.2
 */
public class BitwiseAggregate {

	private static final BitwiseAggregate AND_BITWISE_AGGREGATE = new BitwiseAggregate(Type.AND);
	private static final BitwiseAggregate OR_BITWISE_AGGREGATE = new BitwiseAggregate(Type.OR);
	private static final BitwiseAggregate XOR_BITWISE_AGGREGATE = new BitwiseAggregate(Type.XOR);

	private final Type type;

	private BitwiseAggregate(Type type) {
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
	 * Return the bitwise AND of all values in the range.
	 *
	 * @return the {@literal AND} aggregate.
	 */
	public static BitwiseAggregate and() {
		return AND_BITWISE_AGGREGATE;
	}

	/**
	 * Return the bitwise OR of all values in the range.
	 *
	 * @return the {@literal OR} aggregate.
	 */
	public static BitwiseAggregate or() {
		return OR_BITWISE_AGGREGATE;
	}

	/**
	 * Return the bitwise XOR of all values in the range.
	 *
	 * @return the {@literal XOR} aggregate.
	 */
	public static BitwiseAggregate xor() {
		return XOR_BITWISE_AGGREGATE;
	}

	/**
	 * Bitwise aggregate function types.
	 */
	public enum Type {

		/**
		 * Bitwise AND of all values.
		 */
		AND,

		/**
		 * Bitwise OR of all values.
		 */
		OR,

		/**
		 * Bitwise XOR of all values.
		 */
		XOR
	}

}
