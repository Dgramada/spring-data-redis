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
package org.springframework.data.redis.connection;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import org.springframework.data.redis.connection.array.ArrayRange;
import org.springframework.data.redis.connection.array.BitwiseAggregate;
import org.springframework.data.redis.connection.array.NumericAggregate;

/**
 * Array-specific commands supported by Redis
 *
 * @author Yordan Tsintsov
 * @see RedisCommands
 * @since 4.2
 */
@NullUnmarked
public interface RedisArrayCommands {

	/**
	 * Return the number of non-empty elements.
	 *
	 * @param key must not be {@literal null}.
	 * @return the number of non-empty elements in an array.
	 * @see <a href="https://redis.io/docs/latest/commands/arcount">Redis Documentation: ARCOUNT</a>
	 * @since 4.2
	 */
	Long arCount(byte @NonNull [] key);

	/**
	 * Delete the elements at the specified indices.
	 *
	 * @param key must not be {@literal null}.
	 * @param indices of the elements to delete. Deleting an index that does not exist counts as zero elements deleted and
	 *          does not modify the array.
	 * @return the number of elements deleted.
	 * @see <a href="https://redis.io/docs/latest/commands/ardel">Redis Documentation: ARDEL</a>
	 * @since 4.2
	 */
	Long arDel(byte @NonNull [] key, long... indices);

	/**
	 * Delete elements in a range.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the range (inclusive). If start is greater than end for a given pair, the range is processed
	 *          in ascending order regardless
	 * @param end index of the range (inclusive).
	 * @return the number of elements deleted.
	 * @see <a href="https://redis.io/docs/latest/commands/ardelrange">Redis Documentation: ARDELRANGE</a>
	 * @since 4.2
	 */
	Long arDelRange(byte @NonNull [] key, long start, long end);

	/**
	 * Delete elements in one or more ranges.
	 *
	 * @param key must not be {@literal null}.
	 * @param ranges to delete.
	 * @return the number of elements deleted.
	 * @see <a href="https://redis.io/docs/latest/commands/ardelrange">Redis Documentation: ARDELRANGE</a>
	 * @since 4.2
	 */
	Long arDelRange(byte @NonNull [] key, ArrayRange... ranges);

	/**
	 * Get the value at the index.
	 *
	 * @param key must not be {@literal null}.
	 * @param index the index of the element to retrieve.
	 * @return the value at the given index or {@literal null} if key or index doesn't exist.
	 * @see <a href="https://redis.io/docs/latest/commands/arget">Redis Documentation: ARGET</a>
	 * @since 4.2
	 */
	byte[] arGet(byte @NonNull [] key, long index);

	/**
	 * Get values in a range of indices.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the first element to return. If start is greater than end, elements are returned in reverse
	 *          index order.
	 * @param end index of the last element to return (inclusive). If end is less than start, elements are returned in
	 *          reverse index order.
	 * @return the values in a range of indices.
	 * @see <a href="https://redis.io/docs/latest/commands/argetrange">Redis Documentation: ARGETRANGE</a>
	 * @since 4.2
	 */
	List<byte[]> arGetRange(byte @NonNull [] key, long start, long end);

	/**
	 * Return the most recently inserted elements.
	 *
	 * @param key must not be {@literal null}.
	 * @param count the number of most recently inserted elements to return.
	 * @return the most recently inserted elements.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 * @since 4.2
	 */
	List<byte[]> arLastItems(byte @NonNull [] key, long count);

	/**
	 * Return the most recently inserted elements.
	 *
	 * @param key must not be {@literal null}.
	 * @param count the number of most recently inserted elements to return.
	 * @param reverse {@literal true} to return the elements in reverse chronological order (most recently inserted
	 *          first); {@literal false} to return them in chronological order (oldest of the returned elements first).
	 *          Either way, the same {@code count} most recently inserted elements are returned.
	 * @return the most recently inserted elements.
	 * @see <a href="https://redis.io/docs/latest/commands/arlastitems">Redis Documentation: ARLASTITEMS</a>
	 * @since 4.2
	 */
	List<byte[]> arLastItems(byte @NonNull [] key, long count, boolean reverse);

	/**
	 * Returns the length of the array.
	 *
	 * @param key must not be {@literal null}.
	 * @return the length of the array.
	 * @see <a href="https://redis.io/docs/latest/commands/arlen">Redis Documentation: ARLEN</a>
	 * @since 4.2
	 */
	Long arLen(byte @NonNull [] key);

	/**
	 * Get values at multiple indices.
	 *
	 * @param key must not be {@literal null}.
	 * @param indices of the elements to retrieve.
	 * @return values at multiple indices or null for any index that is not set.
	 * @see <a href="https://redis.io/docs/latest/commands/armget">Redis Documentation: ARMGET</a>
	 * @since 4.2
	 */
	List<byte[]> arMGet(byte @NonNull [] key, long... indices);

	/**
	 * Set multiple index-value pairs in an array.
	 *
	 * @param key must not be {@literal null}.
	 * @param indexValuePairs must not be {@literal null}
	 * @return the number of new slots that were set.
	 * @see <a href="https://redis.io/docs/latest/commands/armset">Redis Documentation: ARMSET</a>
	 * @since 4.2
	 */
	Long arMSet(byte @NonNull [] key, @NonNull Map<@NonNull Long, byte @NonNull []> indexValuePairs);

	/**
	 * Perform a numeric aggregate operation ({@literal SUM}, {@literal MIN} or {@literal MAX}) on the non-empty elements
	 * in a range.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the range (inclusive). The range is always processed from the lower to the higher index
	 *          regardless of argument order.
	 * @param end index of the range (inclusive).
	 * @param numericAggregate the numeric aggregate function to apply. Must not be {@literal null}.
	 * @return the aggregated numeric value or {@literal null} if no numeric elements are present in the range.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 * @since 4.2
	 */
	byte[] arOp(byte @NonNull [] key, long start, long end, NumericAggregate numericAggregate);

	/**
	 * Perform a bitwise or counting aggregate operation ({@literal AND}, {@literal OR} or {@literal XOR}) on the
	 * non-empty elements in a range.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the range (inclusive). The range is always processed from the lower to the higher index
	 *          regardless of argument order.
	 * @param end index of the range (inclusive).
	 * @param aggregate the bitwise or counting aggregate function to apply. Must not be {@literal null}.
	 * @return the bitwise result or the number of matching elements. {@literal null} for bitwise operations if the range
	 *         is empty.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 * @since 4.2
	 */
	Long arOp(byte @NonNull [] key, long start, long end, BitwiseAggregate aggregate);

	/**
	 * Count the non-empty elements in a range that are equal to the given {@code value}.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the range (inclusive). The range is always processed from the lower to the higher index
	 *          regardless of argument order.
	 * @param end index of the range (inclusive).
	 * @param value the value to compare each element against. Must not be {@literal null}.
	 * @return the number of elements in the range equal to {@code value}.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 * @since 4.2
	 */
	Long arOpMatch(byte @NonNull [] key, long start, long end, byte @NonNull [] value);

	/**
	 * Count the non-empty elements in a range.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the range (inclusive). The range is always processed from the lower to the higher index
	 *          regardless of argument order.
	 * @param end index of the range (inclusive).
	 * @return the number of non-empty elements in the range.
	 * @see <a href="https://redis.io/docs/latest/commands/arop">Redis Documentation: AROP</a>
	 * @since 4.2
	 */
	Long arOpUsed(byte @NonNull [] key, long start, long end);

	/**
	 * Insert values into a ring buffer, wrapping and truncating as needed. Each value is placed at the next position in
	 * the ring and wraps back to the first index when the end of the window is reached. When the buffer is full, newer
	 * values overwrite older ones. If the window is smaller than the current one, the array is truncated to fit.
	 *
	 * @param key must not be {@literal null}.
	 * @param size size of the ring buffer window.
	 * @param values to insert into the ring buffer.
	 * @return the last index where a value was inserted.
	 * @see <a href="https://redis.io/docs/latest/commands/arring">Redis Documentation: ARRING</a>
	 * @since 4.2
	 */
	Long arRing(byte @NonNull [] key, long size, byte[]... values);

	/**
	 * Iterate the non-empty elements in a range of indices.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the first element to scan. If start is greater than end, elements are returned in reverse
	 *          index order.
	 * @param end index of the last element to scan (inclusive).
	 * @return the non-empty elements in the range mapped by their index.
	 * @see <a href="https://redis.io/docs/latest/commands/arscan">Redis Documentation: ARSCAN</a>
	 * @since 4.2
	 */
	Map<Long, byte[]> arScan(byte @NonNull [] key, long start, long end);

	/**
	 * Iterate the non-empty elements in a range of indices returning at most {@code limit} elements.
	 *
	 * @param key must not be {@literal null}.
	 * @param start index of the first element to scan. If start is greater than end, elements are returned in reverse
	 *          index order.
	 * @param end index of the last element to scan (inclusive).
	 * @param limit the maximum number of elements to return.
	 * @return the non-empty elements in the range mapped by their index.
	 * @see <a href="https://redis.io/docs/latest/commands/arscan">Redis Documentation: ARSCAN</a>
	 * @since 4.2
	 */
	Map<Long, byte[]> arScan(byte @NonNull [] key, long start, long end, long limit);

	/**
	 * Set values in the array starting from a specified index.
	 *
	 * @param key must not be {@literal null}.
	 * @param index at which to start writing.
	 * @param values to store.
	 * @return the number of new slots that were set.
	 * @see <a href="https://redis.io/docs/latest/commands/arset">Redis Documentation: ARSET</a>
	 * @since 4.2
	 */
	Long arSet(byte @NonNull [] key, long index, byte[]... values);

}
