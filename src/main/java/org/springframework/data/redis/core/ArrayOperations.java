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
package org.springframework.data.redis.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.data.redis.connection.array.ArrayRange;
import org.springframework.data.redis.connection.array.BitwiseAggregate;
import org.springframework.data.redis.connection.array.NumericAggregate;

/**
 * Redis array specific operations.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
public interface ArrayOperations<K, V> {

	/**
	 * Count the non-empty elements of the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arcount">Redis Documentation: ARCOUNT</a>
	 */
	@Nullable
	Long count(K key);

	/**
	 * Delete the element at {@code index} from the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param index index of the element to delete.
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardel">Redis Documentation: ARDEL</a>
	 */
	default @Nullable Long delete(K key, long index) {
		return delete(key, Collections.singletonList(index));
	}

	/**
	 * Delete the elements at {@code indices} from the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param indices must not be {@literal null}.
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardel">Redis Documentation: ARDEL</a>
	 */
	@Nullable
	Long delete(K key, Collection<Long> indices);

	/**
	 * Delete the elements between {@code start} and {@code end} (inclusive) from the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range to delete (inclusive).
	 * @param end end index of the range to delete (inclusive).
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardelrange">Redis Documentation: ARDELRANGE</a>
	 */
	default @Nullable Long deleteRange(K key, long start, long end) {
		return deleteRange(key, Collections.singletonList(ArrayRange.of(start, end)));
	}

	/**
	 * Delete the elements within {@code ranges} from the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param ranges must not be {@literal null} or empty.
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardelrange">Redis Documentation: ARDELRANGE</a>
	 */
	@Nullable
	Long deleteRange(K key, Collection<ArrayRange> ranges);

	/**
	 * Get the element at {@code index} from the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param index index of the element to retrieve.
	 * @return {@literal null} when key or index does not exist or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arget">Redis Documentation: ARGET</a>
	 */
	@Nullable
	V get(K key, long index);

	/**
	 * Get the elements between {@code start} and {@code end} (inclusive) from the array at {@code key}. Elements are
	 * returned in reverse index order when {@code start} is greater than {@code end}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the first element to return.
	 * @param end index of the last element to return (inclusive).
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/argetrange">Redis Documentation: ARGETRANGE</a>
	 */
	@Nullable
	List<V> getRange(K key, long start, long end);

	/**
	 * Get the most recently inserted element of the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @return {@literal null} when the array is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 */
	default @Nullable V latest(K key) {

		List<V> result = latest(key, 1);
		return result == null || result.isEmpty() ? null : result.get(0);
	}

	/**
	 * Get the {@code count} most recently inserted elements of the array at {@code key}, most recent first.
	 *
	 * @param key must not be {@literal null}.
	 * @param count number of elements to return.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 */
	default @Nullable List<V> latest(K key, long count) {
		return latest(key, count, true);
	}

	/**
	 * Get the {@code count} most recently inserted elements of the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param count number of elements to return.
	 * @param reverse {@literal true} to return the elements most recently inserted first, {@literal false} for insertion
	 *          order. Either way, the same {@code count} most recently inserted elements are returned.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 */
	@Nullable
	List<V> latest(K key, long count, boolean reverse);

	/**
	 * Get the length of the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlen">Redis Documentation: ARLEN</a>
	 */
	@Nullable
	Long length(K key);

	/**
	 * Get the elements at {@code indices} from the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param indices must not be {@literal null}.
	 * @return elements in order of {@code indices}, {@literal null} for an index that is not set. {@literal null} when
	 *         used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/armget">Redis Documentation: ARMGET</a>
	 */
	@Nullable
	List<V> multiGet(K key, Collection<Long> indices);

	/**
	 * Set multiple index-value pairs in the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param indexValuePairs must not be {@literal null}.
	 * @return the number of new slots that were set. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/armset">Redis Documentation: ARMSET</a>
	 */
	@Nullable
	Long multiSet(K key, Map<Long, V> indexValuePairs);

	/**
	 * Sum the non-empty elements between {@code start} and {@code end} (inclusive) of the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Number sum(K key, long start, long end) {
		return numOp(key, start, end, NumericAggregate.sum());
	}

	/**
	 * Get the minimum of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Number min(K key, long start, long end) {
		return numOp(key, start, end, NumericAggregate.min());
	}

	/**
	 * Get the maximum of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Number max(K key, long start, long end) {
		return numOp(key, start, end, NumericAggregate.max());
	}

	/**
	 * Apply the numeric {@code aggregate} function to the non-empty elements between {@code start} and {@code end} (inclusive) of
	 * the array at {@code key}. Integral results are returned as {@link Long}, results with a fractional part or
	 * beyond 17 significant digits as {@link Double}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @param aggregate must not be {@literal null}.
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable
	Number numOp(K key, long start, long end, NumericAggregate aggregate);

	/**
	 * Bitwise {@literal AND} of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Long and(K key, long start, long end) {
		return bitOp(key, start, end, BitwiseAggregate.and());
	}

	/**
	 * Bitwise {@literal OR} of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Long or(K key, long start, long end) {
		return bitOp(key, start, end, BitwiseAggregate.or());
	}

	/**
	 * Bitwise {@literal XOR} of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Long xor(K key, long start, long end) {
		return bitOp(key, start, end, BitwiseAggregate.xor());
	}

	/**
	 * Apply the bitwise {@code aggregate} function to the non-empty elements between {@code start} and {@code end} (inclusive) of
	 * the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @param aggregate must not be {@literal null}.
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable
	Long bitOp(K key, long start, long end, BitwiseAggregate aggregate);

	/**
	 * Count the elements between {@code start} and {@code end} (inclusive) of the array at {@code key} that are equal to
	 * {@code value}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @param value must not be {@literal null}.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable
	Long match(K key, long start, long end, V value);

	/**
	 * Count the non-empty elements between {@code start} and {@code end} (inclusive) of the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable
	Long inUse(K key, long start, long end);

	/**
	 * Insert {@code values} into the array at {@code key} as a ring buffer of {@code size}, wrapping around and
	 * overwriting the oldest elements when full.
	 *
	 * @param key must not be {@literal null}.
	 * @param size size of the ring buffer window.
	 * @param values must not be {@literal null}.
	 * @return the last index written. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arring">Redis Documentation: ARRING</a>
	 */
	@Nullable
	Long ring(K key, long size, Collection<V> values);

	/**
	 * Get the non-empty elements between {@code start} and {@code end} (inclusive) of the array at {@code key} mapped
	 * by their index.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the first element to scan.
	 * @param end index of the last element to scan (inclusive).
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arscan">Redis Documentation: ARSCAN</a>
	 */
	@Nullable
	Map<Long, V> scan(K key, long start, long end);

	/**
	 * Get at most {@code limit} non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * {@code key} mapped by their index.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the first element to scan.
	 * @param end index of the last element to scan (inclusive).
	 * @param limit maximum number of elements to return.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arscan">Redis Documentation: ARSCAN</a>
	 */
	@Nullable
	Map<Long, V> scan(K key, long start, long end, long limit);

	/**
	 * Set {@code value} at {@code index} in the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param index index at which to write.
	 * @param value value to store.
	 * @return the number of new slots that were set. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arset">Redis Documentation: ARSET</a>
	 */
	default @Nullable Long set(K key, long index, V value) {
		return set(key, index, Collections.singletonList(value));
	}

	/**
	 * Set {@code values} starting at {@code index} in the array at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param index index at which to start writing.
	 * @param values must not be {@literal null}.
	 * @return the number of new slots that were set. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arset">Redis Documentation: ARSET</a>
	 */
	@Nullable
	Long set(K key, long index, Collection<V> values);

}
