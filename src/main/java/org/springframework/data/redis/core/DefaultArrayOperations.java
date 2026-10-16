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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.data.redis.connection.array.ArrayRange;
import org.springframework.data.redis.connection.array.BitwiseAggregate;
import org.springframework.data.redis.connection.array.NumericAggregate;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link ArrayOperations}.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
class DefaultArrayOperations<K, V> extends AbstractOperations<K, V> implements ArrayOperations<K, V> {

	private static final Pattern INTEGRAL = Pattern.compile("[+-]?\\d+");

	DefaultArrayOperations(RedisTemplate<K, V> template) {
		super(template);
	}

	@Override
	public @Nullable Long count(K key) {
		return execute(c -> c.arrayCommands().arCount(rawKey(key)));
	}

	@Override
	public @Nullable Long delete(K key, Collection<Long> indices) {
		return execute(c -> c.arrayCommands().arDel(rawKey(key), toIndices(indices)));
	}

	@Override
	public @Nullable Long deleteRange(K key, Collection<ArrayRange> ranges) {
		return execute(c -> c.arrayCommands().arDelRange(rawKey(key), toArrayRanges(ranges)));
	}

	@Override
	public @Nullable V get(K key, long index) {
		return deserializeValue(execute(c -> c.arrayCommands().arGet(rawKey(key), index)));
	}

	@Override
	public @Nullable List<V> getRange(K key, long start, long end) {

		List<byte[]> rawValues = execute(c -> c.arrayCommands().arGetRange(rawKey(key), start, end));

		return deserializeValues(rawValues);
	}

	@Override
	public @Nullable List<V> latest(K key, long count, boolean reverse) {

		List<byte[]> rawValues = execute(c -> c.arrayCommands().arLastItems(rawKey(key), count, reverse));

		return deserializeValues(rawValues);
	}

	@Override
	public @Nullable Long length(K key) {
		return execute(c -> c.arrayCommands().arLen(rawKey(key)));
	}

	@Override
	public @Nullable List<V> multiGet(K key, Collection<Long> indices) {

		List<byte[]> rawValues = execute(c -> c.arrayCommands().arMGet(rawKey(key), toIndices(indices)));

		return deserializeValues(rawValues);
	}

	@Override
	public @Nullable Long multiSet(K key, Map<Long, V> indexValuePairs) {
		return execute(c -> c.arrayCommands().arMSet(rawKey(key), rawIndexValuePairs(indexValuePairs)));
	}

	@Override
	public @Nullable Number numOp(K key, long start, long end, NumericAggregate aggregate) {
		return toNumber(execute(c -> c.arrayCommands().arOp(rawKey(key), start, end, aggregate)));
	}

	@Override
	public @Nullable Long bitOp(K key, long start, long end, BitwiseAggregate aggregate) {
		return execute(c -> c.arrayCommands().arOp(rawKey(key), start, end, aggregate));
	}

	@Override
	public @Nullable Long match(K key, long start, long end, V value) {
		return execute(c -> c.arrayCommands().arOpMatch(rawKey(key), start, end, rawValue(value)));
	}

	@Override
	public @Nullable Long inUse(K key, long start, long end) {
		return execute(c -> c.arrayCommands().arOpUsed(rawKey(key), start, end));
	}

	@Override
	public @Nullable Long ring(K key, long size, Collection<V> values) {
		return execute(c -> c.arrayCommands().arRing(rawKey(key), size, rawValues(values)));
	}

	@Override
	public @Nullable Map<Long, V> scan(K key, long start, long end) {
		return deserializeIndexedValues(execute(c -> c.arrayCommands().arScan(rawKey(key), start, end)));
	}

	@Override
	public @Nullable Map<Long, V> scan(K key, long start, long end, long limit) {
		return deserializeIndexedValues(execute(c -> c.arrayCommands().arScan(rawKey(key), start, end, limit)));
	}

	@Override
	public @Nullable Long set(K key, long index, Collection<V> values) {
		return execute(c -> c.arrayCommands().arSet(rawKey(key), index, rawValues(values)));
	}

	private Map<Long, byte[]> rawIndexValuePairs(Map<Long, V> indexValuePairs) {

		Assert.notEmpty(indexValuePairs, "Index-value pairs must not be 'null' or empty");
		Assert.noNullElements(indexValuePairs.keySet(), "Indices must not contain 'null' value");
		Assert.noNullElements(indexValuePairs.values(), "Values must not contain 'null' value");

		Map<Long, byte[]> result = new LinkedHashMap<>(indexValuePairs.size());

		for (Map.Entry<Long, V> entry : indexValuePairs.entrySet()) {
			result.put(entry.getKey(), rawValue(entry.getValue()));
		}

		return result;
	}

	private @Nullable Map<Long, V> deserializeIndexedValues(@Nullable Map<Long, byte[]> rawEntries) {

		if (rawEntries == null) {
			return null;
		}

		Map<Long, V> result = new LinkedHashMap<>(rawEntries.size());

		for (Map.Entry<Long, byte[]> entry : rawEntries.entrySet()) {
			result.put(entry.getKey(), deserializeValue(entry.getValue()));
		}

		return result;
	}

	private static @Nullable Number toNumber(byte @Nullable [] rawNumber) {

		if (rawNumber == null) {
			return null;
		}

		String value = new String(rawNumber, StandardCharsets.UTF_8);

		if (INTEGRAL.matcher(value).matches()) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException ex) {
				return new BigInteger(value);
			}
		}

		return switch (value) {
			case "inf", "+inf" -> Double.POSITIVE_INFINITY;
			case "-inf" -> Double.NEGATIVE_INFINITY;
			case "nan", "-nan" -> Double.NaN;
			default -> Double.parseDouble(value);
		};
	}

	private static ArrayRange[] toArrayRanges(Collection<ArrayRange> ranges) {

		Assert.notEmpty(ranges, "Ranges must not be 'null' or empty");
		Assert.noNullElements(ranges, "Ranges must not contain 'null' value");

		return ranges.toArray(ArrayRange[]::new);
	}

	private static long[] toIndices(Collection<Long> indices) {

		Assert.notEmpty(indices, "Indices must not be 'null' or empty");
		Assert.noNullElements(indices, "Indices must not contain 'null' value");

		long[] result = new long[indices.size()];
		int i = 0;
		for (Long index : indices) {
			result[i++] = index;
		}

		return result;
	}

}
