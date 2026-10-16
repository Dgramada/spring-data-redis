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
package org.springframework.data.redis.connection.jedis;

import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.args.LongRange;
import redis.clients.jedis.commands.PipelineBinaryCommands;
import redis.clients.jedis.util.KeyValue;

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
 * {@link RedisArrayCommands} implementation for Jedis.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
@NullUnmarked
class JedisArrayCommands implements RedisArrayCommands {

	private final JedisConnection connection;

	JedisArrayCommands(JedisConnection connection) {
		this.connection = connection;
	}

	@Override
	public Long arCount(byte @NonNull [] key) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::arcount, PipelineBinaryCommands::arcount, key);
	}

	@Override
	public Long arDel(byte @NonNull [] key, long... indices) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(indices, "Indices must not be null");
		Assert.isTrue(indices.length > 0, "Indices must not be empty");

		return connection.invoke().just(UnifiedJedis::ardel, PipelineBinaryCommands::ardel, key, indices);
	}

	@Override
	public Long arDelRange(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::ardelrange, PipelineBinaryCommands::ardelrange, key, start, end);
	}

	@Override
	public Long arDelRange(byte @NonNull [] key, ArrayRange... ranges) {

		Assert.notNull(key, "Key must not be null");
		Assert.notEmpty(ranges, "Ranges must not be empty");
		Assert.noNullElements(ranges, "Ranges must not contain null elements");

		LongRange[] arrayIndexRanges = new LongRange[ranges.length];
		for (int i = 0; i < ranges.length; i++) {
			arrayIndexRanges[i] = JedisConverters.toArrayIndexRange(ranges[i]);
		}

		return connection.invoke().just(UnifiedJedis::ardelrange, PipelineBinaryCommands::ardelrange, key,
				arrayIndexRanges);
	}

	@Override
	public byte[] arGet(byte @NonNull [] key, long index) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::arget, PipelineBinaryCommands::arget, key, index);
	}

	@Override
	public List<byte[]> arGetRange(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::argetrange, PipelineBinaryCommands::argetrange, key, start, end);
	}

	@Override
	public List<byte[]> arLastItems(byte @NonNull [] key, long count) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::arlastitems, PipelineBinaryCommands::arlastitems, key, count);
	}

	@Override
	public List<byte[]> arLastItems(byte @NonNull [] key, long count, boolean reverse) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::arlastitems, PipelineBinaryCommands::arlastitems, key, count,
				reverse);
	}

	@Override
	public Long arLen(byte @NonNull [] key) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::arlen, PipelineBinaryCommands::arlen, key);
	}

	@Override
	public List<byte[]> arMGet(byte @NonNull [] key, long... indices) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(indices, "Indices must not be null");
		Assert.isTrue(indices.length > 0, "Indices must not be empty");

		return connection.invoke().just(UnifiedJedis::armget, PipelineBinaryCommands::armget, key, indices);
	}

	@Override
	public Long arMSet(byte @NonNull [] key, @NonNull Map<@NonNull Long, byte @NonNull []> pairs) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(pairs, "Pairs must not be null");
		Assert.notEmpty(pairs, "Pairs must not be empty");
		Assert.noNullElements(pairs.keySet(), "Pairs must not contain null keys");
		Assert.noNullElements(pairs.values(), "Pairs must not contain null values");

		return connection.invoke().just(UnifiedJedis::armset, PipelineBinaryCommands::armset, key, pairs);
	}

	@Override
	public byte[] arOp(byte @NonNull [] key, long start, long end, NumericAggregate numericAggregate) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(numericAggregate, "Aggregate must not be null");

		return connection.invoke().just(UnifiedJedis::aropAggregate, PipelineBinaryCommands::aropAggregate, key, start, end,
				JedisConverters.toArrayAggregate(numericAggregate));
	}

	@Override
	public Long arOp(byte @NonNull [] key, long start, long end, BitwiseAggregate aggregate) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(aggregate, "Aggregate must not be null");

		return connection.invoke().just(UnifiedJedis::aropBitwise, PipelineBinaryCommands::aropBitwise, key, start, end,
				JedisConverters.toArrayBitwise(aggregate));
	}

	@Override
	public Long arOpMatch(byte @NonNull [] key, long start, long end, byte @NonNull [] value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(value, "Value must not be null");

		return connection.invoke().just(UnifiedJedis::aropCount, PipelineBinaryCommands::aropCount, key, start, end, value);
	}

	@Override
	public Long arOpUsed(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(UnifiedJedis::aropCount, PipelineBinaryCommands::aropCount, key, start, end);
	}

	@Override
	public Long arRing(byte @NonNull [] key, long size, byte[]... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notEmpty(values, "Values must not be empty");
		Assert.noNullElements(values, "Values must not contain null elements");

		return connection.invoke().just(UnifiedJedis::arring, PipelineBinaryCommands::arring, key, size, values);
	}

	@Override
	public Map<Long, byte[]> arScan(byte @NonNull [] key, long start, long end) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().from(UnifiedJedis::arscan, PipelineBinaryCommands::arscan, key, start, end)
				.get(this::decodeArScan);
	}

	@Override
	public Map<Long, byte[]> arScan(byte @NonNull [] key, long start, long end, long limit) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().from(UnifiedJedis::arscan, PipelineBinaryCommands::arscan, key, start, end, limit)
				.get(this::decodeArScan);
	}

	@Override
	public Long arSet(byte @NonNull [] key, long index, byte[]... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(values, "Values must not be null");
		Assert.notEmpty(values, "Values must not be empty");
		Assert.noNullElements(values, "Values must not contain null values");

		return connection.invoke().just(UnifiedJedis::arset, PipelineBinaryCommands::arset, key, index, values);
	}

	private Map<Long, byte[]> decodeArScan(List<KeyValue<Long, byte[]>> list) {

		Map<Long, byte[]> result = new LinkedHashMap<>(list.size());
		list.forEach(it -> result.put(it.getKey(), it.getValue()));
		return result;
	}

}
