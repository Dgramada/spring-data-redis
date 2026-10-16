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
package org.springframework.data.redis.connection.lettuce;

import io.lettuce.core.api.async.RedisArrayAsyncCommands;
import io.lettuce.core.array.ArrayIndexRange;
import io.lettuce.core.array.IndexedValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import org.springframework.data.redis.connection.RedisArrayCommands;
import org.springframework.data.redis.connection.array.ArrayRange;
import org.springframework.data.redis.connection.array.BitwiseAggregate;
import org.springframework.data.redis.connection.array.NumericAggregate;
import org.springframework.util.Assert;

/**
 * {@link RedisArrayCommands} implementation for Lettuce.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
@NullUnmarked
class LettuceArrayCommands implements RedisArrayCommands {

	private final LettuceConnection connection;

	LettuceArrayCommands(LettuceConnection connection) {
		this.connection = connection;
	}

	@Override
	public Long arCount(byte @NonNull [] key) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::arcount, key);
	}

	@Override
	public Long arDel(byte @NonNull [] key, long... indices) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(indices, "Indices must not be null");
		Assert.isTrue(indices.length > 0, "Indices must not be empty");

		return connection.invoke().just(RedisArrayAsyncCommands::ardel, key, indices);
	}

	@Override
	public Long arDelRange(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::ardelrange, key, start, end);
	}

	@Override
	public Long arDelRange(byte @NonNull [] key, ArrayRange... ranges) {

		Assert.notNull(key, "Key must not be null");
		Assert.notEmpty(ranges, "Ranges must not be empty");
		Assert.noNullElements(ranges, "Ranges must not contain null elements");

		ArrayIndexRange[] arrayIndexRanges = new ArrayIndexRange[ranges.length];
		for (int i = 0; i < ranges.length; i++) {
			arrayIndexRanges[i] = LettuceConverters.toArrayIndexRange(ranges[i]);
		}

		return connection.invoke().just(RedisArrayAsyncCommands::ardelrange, key, arrayIndexRanges);
	}

	@Override
	public byte[] arGet(byte @NonNull [] key, long index) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::arget, key, index);
	}

	@Override
	public List<byte[]> arGetRange(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::argetrange, key, start, end);
	}

	@Override
	public List<byte[]> arLastItems(byte @NonNull [] key, long count) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::arlastitems, key, count);
	}

	@Override
	public List<byte[]> arLastItems(byte @NonNull [] key, long count, boolean reverse) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::arlastitems, key, count, reverse);
	}

	@Override
	public Long arLen(byte @NonNull [] key) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::arlen, key);
	}

	@Override
	public List<byte[]> arMGet(byte @NonNull [] key, long... indices) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(indices, "Indices must not be null");
		Assert.isTrue(indices.length > 0, "Indices must not be empty");

		return connection.invoke().just(RedisArrayAsyncCommands::armget, key, indices);
	}

	@Override
	public Long arMSet(byte @NonNull [] key, @NonNull Map<@NonNull Long, byte @NonNull []> indexValuePairs) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(indexValuePairs, "Pairs must not be null");
		Assert.notEmpty(indexValuePairs, "Pairs must not be empty");
		Assert.noNullElements(indexValuePairs.keySet(), "Pairs must not contain null keys");
		Assert.noNullElements(indexValuePairs.values(), "Pairs must not contain null values");

		return connection.invoke().just(RedisArrayAsyncCommands::armset, key, indexValuePairs);
	}

	@Override
	public byte[] arOp(byte @NonNull [] key, long start, long end, NumericAggregate numericAggregate) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(numericAggregate, "Aggregate must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::aropAggregate, key, start, end,
				LettuceConverters.toArrAggregateType(numericAggregate));
	}

	@Override
	public Long arOp(byte @NonNull [] key, long start, long end, BitwiseAggregate aggregate) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(aggregate, "Aggregate must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::aropBitwise, key, start, end,
				LettuceConverters.toBitwiseAggregateType(aggregate));
	}

	@Override
	public Long arOpMatch(byte @NonNull [] key, long start, long end, byte @NonNull [] value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(value, "Value must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::aropCount, key, start, end, value);
	}

	@Override
	public Long arOpUsed(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisArrayAsyncCommands::aropCount, key, start, end);
	}

	@Override
	public Long arRing(byte @NonNull [] key, long size, byte[]... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notEmpty(values, "Values must not be empty");
		Assert.noNullElements(values, "Values must not contain null elements");

		return connection.invoke().just(RedisArrayAsyncCommands::arring, key, size, values);
	}

	@Override
	public Map<Long, byte[]> arScan(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().from(RedisArrayAsyncCommands::arscan, key, start, end).get(this::decodeArScan);
	}

	@Override
	public Map<Long, byte[]> arScan(byte @NonNull [] key, long start, long end, long limit) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().from(RedisArrayAsyncCommands::arscan, key, start, end, limit).get(this::decodeArScan);
	}

	@Override
	public Long arSet(byte @NonNull [] key, long index, byte[]... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(values, "Values must not be null");
		Assert.notEmpty(values, "Values must not be empty");
		Assert.noNullElements(values, "Values must not contain null values");

		return connection.invoke().just(RedisArrayAsyncCommands::arset, key, index, values);
	}

	private Map<Long, byte[]> decodeArScan(List<IndexedValue<byte[]>> list) {

		Map<Long, byte[]> result = new LinkedHashMap<>(list.size());
		list.forEach(it -> result.put(it.getIndex(), it.getValue()));
		return result;
	}

}
