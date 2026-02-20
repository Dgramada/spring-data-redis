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
import io.lettuce.core.json.arguments.JsonMsetArgs;
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
	public @Nullable List<@Nullable String> jsonGet(byte[] key, String... path) {

		Assert.notNull(key, "Key must not be null");
		Assert.noNullElements(path, "Path must not be null");

		JsonPath[] paths = Stream.of(path).map(JsonPath::of).toArray(JsonPath[]::new);

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonGet, key, paths)
				.get(valueList -> valueList.stream().map(value -> value != null ? value.toString() : null).toList());
	}

	@Override
	public Boolean jsonMerge(byte[] key, String path, @Nullable String value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonMerge, key, JsonPath.of(path), value)
				.get(LettuceConverters::stringToBoolean);
	}

	@Override
	public @Nullable List<@Nullable String> jsonMGet(String path, byte[]... keys) {

		Assert.notNull(path, "Path must not be null");
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
	public Boolean jsonSet(byte[] key, String path, @Nullable String value, JsonSetOption option) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(option, "Option must not be null");

		return connection.invoke()
				.from(RedisJsonAsyncCommands::jsonSet, key, JsonPath.of(path), value, LettuceConverters.toJsonSetArgs(option))
				.get(LettuceConverters::stringToBoolean);
	}

}
