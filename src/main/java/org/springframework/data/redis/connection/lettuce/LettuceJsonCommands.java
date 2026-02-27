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

import io.lettuce.core.api.async.RedisJsonAsyncCommands;
import io.lettuce.core.json.JsonPath;
import io.lettuce.core.json.JsonValue;
import io.lettuce.core.json.arguments.JsonMsetArgs;
import io.lettuce.core.json.arguments.JsonRangeArgs;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.RedisJsonCommands;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@link RedisJsonCommands} implementation for Lettuce.
 *
 * @author Yordan Tsintsov
 * @since 4.3
 */
class LettuceJsonCommands implements RedisJsonCommands {

	private final LettuceConnection connection;

	LettuceJsonCommands(LettuceConnection connection) {
		this.connection = connection;
	}

	@Override
	public List<@Nullable Long> jsonArrAppend(byte[] key, String path, String... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.noNullElements(values, "Values must not be null");

		JsonValue[] jsonValues = Stream.of(values).map(LettuceConverters::toJsonValue).toArray(JsonValue[]::new);

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrappend, key, JsonPath.of(path), jsonValues);
	}

	@Override
	public List<@Nullable Long> jsonArrIndex(byte[] key, String path, String value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrindex, key, JsonPath.of(path), LettuceConverters.toJsonValue(value));
	}

	@Override
	public List<@Nullable Long> jsonArrIndex(byte[] key, String path, String value, long start) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		JsonRangeArgs args = JsonRangeArgs.Builder.start(start);

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrindex, key, JsonPath.of(path), LettuceConverters.toJsonValue(value), args);
	}

	@Override
	public List<@Nullable Long> jsonArrIndex(byte[] key, String path, String value, long start, long stop) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		JsonRangeArgs args = JsonRangeArgs.Builder.start(start).stop(stop);

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrindex, key, JsonPath.of(path), LettuceConverters.toJsonValue(value), args);
	}

	@Override
	public List<@Nullable Long> jsonArrInsert(byte[] key, String path, int index, String... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.noNullElements(values, "Values must not be null");

		JsonValue[] jsonValues = Stream.of(values).map(LettuceConverters::toJsonValue).toArray(JsonValue[]::new);

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrinsert, key, JsonPath.of(path), index, jsonValues);
	}

	@Override
	public List<@Nullable Long> jsonArrLen(byte[] key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrlen, key, JsonPath.of(path));
	}

	@Override
	public List<@Nullable String> jsonArrPop(byte[] key) {

		Assert.notNull(key, "Key must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrpopRaw, key);
	}

	@Override
	public List<@Nullable String> jsonArrPop(byte[] key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrpopRaw, key, JsonPath.of(path));
	}

	@Override
	public List<@Nullable String> jsonArrPop(byte[] key, String path, int index) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrpopRaw, key, JsonPath.of(path), index);
	}

	@Override
	public List<@Nullable Long> jsonArrTrim(byte[] key, String path, int start, int stop) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		JsonRangeArgs args = JsonRangeArgs.Builder.start(start).stop(stop);

		return connection.invoke().just(RedisJsonAsyncCommands::jsonArrtrim, key, JsonPath.of(path), args);
	}

	@Override
	public Long jsonClear(byte[] key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonClear, key, JsonPath.of(path));
	}

	@Override
	public Long jsonDel(byte[] key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonDel, key, JsonPath.of(path));
	}

	@Override
	public List<@Nullable String> jsonGet(byte[] key, String... path) {

		Assert.notNull(key, "Key must not be null");
		Assert.noNullElements(path, "Path must not be null");

		JsonPath[] paths = Stream.of(path).map(JsonPath::of).toArray(JsonPath[]::new);

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonGet, key, paths)
				.get(valueList -> valueList.stream().map(value -> value != null ? value.toString() : null).toList());
	}

	@Override
	public Boolean jsonMerge(byte[] key, String path, String value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonMerge, key, JsonPath.of(path), LettuceConverters.toJsonValue(value))
				.get(LettuceConverters::stringToBoolean);
	}

	@Override
	public List<@Nullable String> jsonMGet(String path, byte[]... keys) {

		Assert.notNull(path, "Path must not be null");
		Assert.notEmpty(keys, "Keys must not be empty");
		Assert.noNullElements(keys, "Keys must not be null");

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonMGet, JsonPath.of(path), keys)
				.get(jsonValueList -> jsonValueList.stream().map(value -> value != null ? value.toString() : null).toList());
	}

	@Override
	public Boolean jsonMSet(List<JsonMSetArg> args) {

		Assert.notNull(args, "Args must not be null");
		Assert.notEmpty(args, "Args must not be empty");
		Assert.noNullElements(args, "Args must not contain null elements");

		List<JsonMsetArgs<byte[], byte[]>> lettuceArgs = args.stream().map(LettuceConverters::toJsonMsetArgs).toList();

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonMSet, lettuceArgs)
				.get(LettuceConverters::stringToBoolean);
	}

	@Override
	public List<@Nullable Number> jsonNumIncrBy(byte[] key, String path, Number number) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(number, "Number must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonNumincrby, key, JsonPath.of(path), number);
	}

	@Override
	public Boolean jsonSet(byte[] key, String path, String value, JsonSetOption option) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");
		Assert.notNull(option, "Option must not be null");

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonSet, key, JsonPath.of(path), LettuceConverters.toJsonValue(value), LettuceConverters.toJsonSetArgs(option))
				.get(LettuceConverters::stringToBoolean);
	}

	@Override
	public List<@Nullable Long> jsonStrAppend(byte[] key, String path, String value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonStrappend, key, JsonPath.of(path), LettuceConverters.toJsonValue(value));
	}

	@Override
	public List<@Nullable Long> jsonStrLen(byte[] key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke().just(RedisJsonAsyncCommands::jsonStrlen, key, JsonPath.of(path));
	}

	@Override
	public List<@Nullable Boolean> jsonToggle(byte[] key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonToggle, key, JsonPath.of(path))
				.get(jsonValueList ->
						jsonValueList.stream().map(value -> value != null ? LettuceConverters.toBoolean(value) : null).toList());
	}

}
