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

import io.lettuce.core.json.JsonPath;
import io.lettuce.core.json.arguments.JsonMsetArgs;
import io.lettuce.core.json.arguments.JsonSetArgs;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.connection.ReactiveJsonCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection.BooleanResponse;
import org.springframework.data.redis.connection.ReactiveRedisConnection.MultiValueResponse;
import org.springframework.data.redis.connection.ReactiveRedisConnection.NumericResponse;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * {@link ReactiveJsonCommands} implementation for Lettuce.
 *
 * @author Yordan Tsintsov
 * @since 4.3
 */
class LettuceReactiveJsonCommands implements ReactiveJsonCommands {

	private final LettuceReactiveRedisConnection connection;

	/**
	 * Creates new {@link LettuceReactiveJsonCommands}.
	 *
	 * @param connection must not be {@literal null}.
	 */
	LettuceReactiveJsonCommands(LettuceReactiveRedisConnection connection) {

		Assert.notNull(connection, "LettuceReactiveRedisConnection must not be null");
		this.connection = connection;
	}

	@Override
	public Flux<NumericResponse<JsonCommand, Long>> jsonClear(Publisher<JsonCommand> commands) {

		return connection.execute(reactiveCommands -> Flux.from(commands).concatMap((command) -> {

			Assert.notNull(command.getKey(), "Key must not be null");

			if (command.getPath() == null) {
				return reactiveCommands.jsonClear(command.getKey())
						.map(value -> new NumericResponse<>(command, value));
			}

			return reactiveCommands.jsonClear(command.getKey(), JsonPath.of(command.getPath()))
					.map(value -> new NumericResponse<>(command, value));
		}));
	}

	@Override
	public Flux<NumericResponse<JsonCommand, Long>> jsonDel(Publisher<JsonCommand> commands) {

		return connection.execute(reactiveCommands -> Flux.from(commands).concatMap((command) -> {

			Assert.notNull(command.getKey(), "Key must not be null");

			if (command.getPath() == null) {
				return reactiveCommands.jsonDel(command.getKey())
						.map(value -> new NumericResponse<>(command, value));
			}

			return reactiveCommands.jsonDel(command.getKey(), JsonPath.of(command.getPath()))
					.map(value -> new NumericResponse<>(command, value));
		}));
	}

	@Override
	public Flux<MultiValueResponse<JsonGetCommand, @Nullable String>> jsonGet(Publisher<JsonGetCommand> commands) {

		return connection.execute(reactiveCommands -> Flux.from(commands).concatMap((command) -> {

			Assert.notNull(command.getKey(), "Key must not be null");
			Assert.noNullElements(command.getPaths(), "Paths must not be null");

			JsonPath[] paths = command.getPaths().stream().map(JsonPath::of).toArray(JsonPath[]::new);

			return reactiveCommands.jsonGet(command.getKey(), paths)
					.collectList()
					.map(value -> new MultiValueResponse<>(command, value.stream().map(v -> v != null ? v.toString() : null).toList()))
					.defaultIfEmpty(new MultiValueResponse<>(command, Collections.emptyList()));
		}));
	}

	@Override
	public Flux<BooleanResponse<JsonMergeCommand>> jsonMerge(Publisher<JsonMergeCommand> commands) {

		return connection.execute(reactiveCommands -> Flux.from(commands).concatMap((command) -> {

			Assert.notNull(command.getKey(), "Key must not be null");
			Assert.notNull(command.getPath(), "Path must not be null");

			return reactiveCommands.jsonMerge(command.getKey(), JsonPath.of(command.getPath()), command.getValue())
					.map(LettuceConverters::stringToBoolean)
					.map(value -> new BooleanResponse<>(command, value))
					.defaultIfEmpty(new BooleanResponse<>(command, Boolean.FALSE));
		}));
	}

	@Override
	public Flux<MultiValueResponse<JsonMGetCommand, @Nullable String>> jsonMGet(Publisher<JsonMGetCommand> commands) {

		return connection.execute(reactiveCommands -> Flux.from(commands).concatMap((command) -> {

			Assert.notNull(command.getPath(), "Path must not be null");
			Assert.notEmpty(command.getKeys(), "Keys must not be empty");
			Assert.noNullElements(command.getKeys(), "Keys must not contain null elements");

			return reactiveCommands.jsonMGet(JsonPath.of(command.getPath()), command.getKeys().toArray(ByteBuffer[]::new))
					.collectList()
					.map(value -> new MultiValueResponse<>(command, value.stream().map(v -> v != null ? v.toString() : null).toList()))
					.defaultIfEmpty(new MultiValueResponse<>(command, Collections.emptyList()));
		}));
	}

	@Override
	public Flux<BooleanResponse<JsonMSetCommand>> jsonMSet(Publisher<JsonMSetCommand> commands) {

		return connection.execute(reactiveCommands -> Flux.from(commands).concatMap((command) -> {

			Assert.notEmpty(command.getArgs(), "Args must not be empty");
			Assert.noNullElements(command.getArgs(), "Args must not contain null elements");

			List<JsonMsetArgs<ByteBuffer, ByteBuffer>> args = command.getArgs().stream()
					.map(LettuceConverters::toJsonMsetArgs)
					.toList();

			return reactiveCommands.jsonMSet(args)
					.map(LettuceConverters::stringToBoolean)
					.map(value -> new BooleanResponse<>(command, value))
					.defaultIfEmpty(new BooleanResponse<>(command, Boolean.FALSE));
		}));
	}

	@Override
	public Flux<BooleanResponse<JsonSetCommand>> jsonSet(Publisher<JsonSetCommand> commands) {

		return this.connection.execute(reactiveCommands -> Flux.from(commands).concatMap((command) -> {

			Assert.notNull(command.getKey(), "Key must not be null");
			Assert.notNull(command.getPath(), "Path must not be null");
			Assert.notNull(command.getOption(), "Option must not be null");

			JsonPath path = JsonPath.of(command.getPath());
			JsonSetArgs args = LettuceConverters.toJsonSetArgs(command.getOption());

			return reactiveCommands.jsonSet(command.getKey(), path, command.getValue(), args)
					.map(LettuceConverters::stringToBoolean)
					.map(value -> new BooleanResponse<>(command, value))
					.defaultIfEmpty(new BooleanResponse<>(command, Boolean.FALSE));
		}));
	}

}
