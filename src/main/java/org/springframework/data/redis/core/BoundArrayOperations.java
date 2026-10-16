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
 * Array operations bound to a certain key.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
public interface BoundArrayOperations<K, V> extends BoundKeyOperations<K> {

	/**
	 * Count the non-empty elements of the array at the bound key.
	 *
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arcount">Redis Documentation: ARCOUNT</a>
	 */
	@Nullable Long count();

	/**
	 * Delete the element at {@code index} from the array at the bound key.
	 *
	 * @param index index of the element to delete.
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardel">Redis Documentation: ARDEL</a>
	 */
	default @Nullable Long delete(long index) {
		return delete(Collections.singletonList(index));
	}

	/**
	 * Delete the elements at {@code indices} from the array at the bound key.
	 *
	 * @param indices must not be {@literal null}.
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardel">Redis Documentation: ARDEL</a>
	 */
	@Nullable Long delete(Collection<Long> indices);

	/**
	 * Delete the elements between {@code start} and {@code end} (inclusive) from the array at the bound key.
	 *
	 * @param start start index of the range to delete (inclusive).
	 * @param end end index of the range to delete (inclusive).
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardelrange">Redis Documentation: ARDELRANGE</a>
	 */
	default @Nullable Long deleteRange(long start, long end) {
		return deleteRange(Collections.singletonList(ArrayRange.of(start, end)));
	}

	/**
	 * Delete the elements within {@code ranges} from the array at the bound key.
	 *
	 * @param ranges must not be {@literal null} or empty.
	 * @return the number of elements deleted. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/ardelrange">Redis Documentation: ARDELRANGE</a>
	 */
	@Nullable Long deleteRange(Collection<ArrayRange> ranges);

	/**
	 * Get the element at {@code index} from the array at the bound key.
	 *
	 * @param index index of the element to retrieve.
	 * @return {@literal null} when key or index does not exist or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arget">Redis Documentation: ARGET</a>
	 */
	@Nullable V get(long index);

	/**
	 * Get the elements between {@code start} and {@code end} (inclusive) from the array at the bound key. Elements are
	 * returned in reverse index order when {@code start} is greater than {@code end}.
	 *
	 * @param start index of the first element to return.
	 * @param end index of the last element to return (inclusive).
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/argetrange">Redis Documentation: ARGETRANGE</a>
	 */
	@Nullable List<V> getRange(long start, long end);

	/**
	 * Get the most recently inserted element of the array at the bound key.
	 *
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 */
	default @Nullable V latest() {

		List<V> result = latest(1);
		return result == null || result.isEmpty() ? null : result.get(0);
	}

	/**
	 * Get the {@code count} most recently inserted elements of the array at the bound key, most recent first.
	 *
	 * @param count number of elements to return.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 */
	default @Nullable List<V> latest(long count) {
		return latest(count, true);
	}

	/**
	 * Get the {@code count} most recently inserted elements of the array at the bound key.
	 *
	 * @param count number of elements to return.
	 * @param reverse {@literal true} to return the elements most recently inserted first, {@literal false} for insertion
	 *          order. Either way, the same {@code count} most recently inserted elements are returned.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 */
	@Nullable List<V> latest(long count, boolean reverse);

	/**
	 * Get the length of the array at the bound key.
	 *
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arlen">Redis Documentation: ARLEN</a>
	 */
	@Nullable Long length();

	/**
	 * Get the elements at {@code indices} from the array at the bound key.
	 *
	 * @param indices must not be {@literal null}.
	 * @return elements in order of {@code indices}, {@literal null} for an index that is not set. {@literal null} when
	 *         used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/armget">Redis Documentation: ARMGET</a>
	 */
	@Nullable List<V> multiGet(Collection<Long> indices);

	/**
	 * Set multiple index-value pairs in the array at the bound key.
	 *
	 * @param indexValuePairs must not be {@literal null}.
	 * @return the number of new slots that were set. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/armset">Redis Documentation: ARMSET</a>
	 */
	@Nullable Long multiSet(Map<Long, V> indexValuePairs);

	/**
	 * Sum the non-empty elements between {@code start} and {@code end} (inclusive) of the array at the bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Number sum(long start, long end) {
		return numOp(start, end, NumericAggregate.sum());
	}

	/**
	 * Get the minimum of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at the
	 * bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Number min(long start, long end) {
		return numOp(start, end, NumericAggregate.min());
	}

	/**
	 * Get the maximum of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at the
	 * bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Number max(long start, long end) {
		return numOp(start, end, NumericAggregate.max());
	}

	/**
	 * Apply the numeric {@code aggregate} function to the non-empty elements between {@code start} and {@code end} (inclusive) of
	 * the array at the bound key. Integral results are returned as {@link Long}, results with a fractional part or
	 * beyond 17 significant digits as {@link Double}.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @param aggregate must not be {@literal null}.
	 * @return {@literal null} when the range has no numeric elements or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable Number numOp(long start, long end, NumericAggregate aggregate);

	/**
	 * Bitwise {@literal AND} of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * the bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Long and(long start, long end) {
		return bitOp(start, end, BitwiseAggregate.and());
	}

	/**
	 * Bitwise {@literal OR} of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * the bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Long or(long start, long end) {
		return bitOp(start, end, BitwiseAggregate.or());
	}

	/**
	 * Bitwise {@literal XOR} of the non-empty elements between {@code start} and {@code end} (inclusive) of the array at
	 * the bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	default @Nullable Long xor(long start, long end) {
		return bitOp(start, end, BitwiseAggregate.xor());
	}

	/**
	 * Apply the bitwise {@code aggregate} function to the non-empty elements between {@code start} and {@code end} (inclusive) of
	 * the array at the bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @param aggregate must not be {@literal null}.
	 * @return {@literal null} when the range is empty or when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable Long bitOp(long start, long end, BitwiseAggregate aggregate);

	/**
	 * Count the elements between {@code start} and {@code end} (inclusive) of the array at the bound key that are equal
	 * to {@code value}.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @param value must not be {@literal null}.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable Long match(long start, long end, V value);

	/**
	 * Count the non-empty elements between {@code start} and {@code end} (inclusive) of the array at the bound key.
	 *
	 * @param start start index of the range (inclusive).
	 * @param end end index of the range (inclusive).
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 */
	@Nullable Long inUse(long start, long end);

	/**
	 * Insert {@code values} into the array at the bound key as a ring buffer of {@code size}, wrapping around and
	 * overwriting the oldest elements when full.
	 *
	 * @param size size of the ring buffer window.
	 * @param values must not be {@literal null}.
	 * @return the last index written. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arring">Redis Documentation: ARRING</a>
	 */
	@Nullable Long ring(long size, Collection<V> values);

	/**
	 * Get the non-empty elements between {@code start} and {@code end} (inclusive) of the array at the bound key mapped
	 * by their index.
	 *
	 * @param start index of the first element to scan.
	 * @param end index of the last element to scan (inclusive).
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arscan">Redis Documentation: ARSCAN</a>
	 */
	@Nullable Map<Long, V> scan(long start, long end);

	/**
	 * Get at most {@code limit} non-empty elements between {@code start} and {@code end} (inclusive) of the array at the
	 * bound key mapped by their index.
	 *
	 * @param start index of the first element to scan.
	 * @param end index of the last element to scan (inclusive).
	 * @param limit maximum number of elements to return.
	 * @return {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arscan">Redis Documentation: ARSCAN</a>
	 */
	@Nullable Map<Long, V> scan(long start, long end, long limit);

	/**
	 * Set {@code value} at {@code index} in the array at the bound key.
	 *
	 * @param index index at which to write.
	 * @param value value to store.
	 * @return the number of new slots that were set. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arset">Redis Documentation: ARSET</a>
	 */
	default @Nullable Long set(long index, V value) {
		return set(index, Collections.singleton(value));
	}

	/**
	 * Set {@code values} starting at {@code index} in the array at the bound key.
	 *
	 * @param index index at which to start writing.
	 * @param values must not be {@literal null}.
	 * @return the number of new slots that were set. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/docs/latest/commands/arset">Redis Documentation: ARSET</a>
	 */
	@Nullable Long set(long index, Collection<V> values);

}
