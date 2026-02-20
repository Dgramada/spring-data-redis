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

import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.connection.ReactiveRedisConnection.BooleanResponse;
import org.springframework.data.redis.connection.ReactiveRedisConnection.Command;
import org.springframework.data.redis.connection.ReactiveRedisConnection.MultiValueResponse;
import org.springframework.data.redis.connection.ReactiveRedisConnection.NumericResponse;
import org.springframework.data.redis.connection.ReactiveRedisConnection.KeyCommand;
import org.springframework.data.redis.connection.RedisJsonCommands.JsonSetOption;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * JSON-specific Redis commands executed using reactive infrastructure.
 *
 * @author Yordan Tsintsov
 * @see RedisJsonCommands
 * @since 4.3
 */
public interface ReactiveJsonCommands {

	String ROOT_PATH = "$";

	/**
	 * Generic JSON command parameters for commands that take a key and optional path.
	 * Used by {@code JSON.DEL}, {@code JSON.CLEAR}, and similar commands.
	 *
	 * @author Yordan Tsintsov
	 * @since 4.3
	 */
	class JsonCommand extends KeyCommand {

		private final @Nullable String path;

		private JsonCommand(@Nullable ByteBuffer key, @Nullable String path) {

			super(key);

			this.path = path;
		}

		/**
		 * Creates a new {@link JsonCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonCommand} for {@link ByteBuffer key}.
		 */
		public static JsonCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonCommand(key, null);
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonCommand} with {@literal path} applied.
		 */
		public JsonCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonCommand(getKey(), path);
		}

		public @Nullable String getPath() {
			return path;
		}

	}

	/**
	 * Clear container values (arrays/objects) and set numeric values to 0 at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @return {@link Mono} emitting the number of paths cleared.
	 * @see <a href="https://redis.io/commands/json.clear">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.3
	 */
	default Mono<Long> jsonClear(ByteBuffer key) {

		Assert.notNull(key, "Key must not be null");

		return jsonClear(Mono.just(JsonCommand.key(key)))
				.next()
				.map(NumericResponse::getOutput);
	}

	/**
	 * Clear container values (arrays/objects) and set numeric values to 0 at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return {@link Mono} emitting the number of paths cleared.
	 * @see <a href="https://redis.io/commands/json.clear">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.3
	 */
	default Mono<Long> jsonClear(ByteBuffer key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonClear(Mono.just(JsonCommand.key(key).atPath(path)))
				.next()
				.map(NumericResponse::getOutput);
	}

	/**
	 * Clear container values (arrays/objects) and set numeric values to 0 at the given key.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link NumericResponse} holding the number of paths cleared.
	 * @see <a href="https://redis.io/commands/json.clear">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.3
	 * */
	Flux<NumericResponse<JsonCommand, Long>> jsonClear(Publisher<JsonCommand> commands);

	/**
	 * Delete the JSON value at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @return {@link Mono} emitting the number of deleted values.
	 * @see <a href="https://redis.io/commands/json.del">Redis Documentation: JSON.DEL</a>
	 * @since 4.3
	 */
	default Mono<Long> jsonDel(ByteBuffer key) {

		Assert.notNull(key, "Key must not be null");

		return jsonDel(Mono.just(JsonCommand.key(key)))
				.next()
				.map(NumericResponse::getOutput);
	}

	/**
	 * Delete the JSON value at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return {@link Mono} emitting the number of deleted values.
	 * @see <a href="https://redis.io/commands/json.del">Redis Documentation: JSON.DEL</a>
	 * @since 4.3
	 */
	default Mono<Long> jsonDel(ByteBuffer key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonDel(Mono.just(JsonCommand.key(key).atPath(path)))
				.next()
				.map(NumericResponse::getOutput);
	}

	/**
	 * Delete the JSON value at the given key.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link Long} holding the number of deleted values.
	 * @see <a href="https://redis.io/commands/json.del">Redis Documentation: JSON.DEL</a>
	 * @since 4.3
	 */
	Flux<NumericResponse<JsonCommand, Long>> jsonDel(Publisher<JsonCommand> commands);

	/**
	 * {@code JSON.GET} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.get">Redis Documentation: JSON.GET</a>
	 * @since 4.3
	 */
	class JsonGetCommand extends KeyCommand {

		private final List<String> paths;

		private JsonGetCommand(@Nullable ByteBuffer key, List<String> paths) {

			super(key);

			this.paths = List.copyOf(paths);
		}

		/**
		 * Creates a new {@link JsonGetCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonGetCommand} for {@link ByteBuffer key}.
		 */
		public static JsonGetCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonGetCommand(key, List.of(ROOT_PATH));
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param paths must not be {@literal null}.
		 * @return a new {@link JsonGetCommand} with {@literal path} applied.
		 */
		public JsonGetCommand atPath(String... paths) {

			Assert.noNullElements(paths, "Paths must not be null");

			return new JsonGetCommand(getKey(), List.of(paths));
		}

		public List<String> getPaths() {
			return paths;
		}

	}

	/**
	 * Get the JSON value at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @return {@link Mono} emitting the JSON values. Absent field values are represented using {@literal null}.
	 * @see <a href="https://redis.io/commands/json.get">Redis Documentation: JSON.GET</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable String>> jsonGet(ByteBuffer key) {

		Assert.notNull(key, "Key must not be null");

		return jsonGet(Mono.just(JsonGetCommand.key(key)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Get the JSON values at the given key and paths.
	 *
	 * @param key must not be {@literal null}.
	 * @param paths must not be {@literal null}.
	 * @return {@link Mono} emitting the JSON values. Absent field values are represented using {@literal null}.
	 * @see <a href="https://redis.io/commands/json.get">Redis Documentation: JSON.GET</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable String>> jsonGet(ByteBuffer key, String... paths) {

		Assert.notNull(key, "Key must not be null");
		Assert.noNullElements(paths, "Paths must not be null");

		return jsonGet(Mono.just(JsonGetCommand.key(key).atPath(paths)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Get the JSON values at the given key and paths.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonGetCommand} along with the command result.
	 * Absent field values are represented using {@literal null}. Absent keys are represented using an empty list.
	 * @see <a href="https://redis.io/commands/json.get">Redis Documentation: JSON.GET</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonGetCommand, @Nullable String>> jsonGet(Publisher<JsonGetCommand> commands);

	/**
	 * {@code JSON.MERGE} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.merge">Redis Documentation: JSON.MERGE</a>
	 * @since 4.3
	 */
	class JsonMergeCommand extends KeyCommand {

		private final String path;
		private final @Nullable String value;

		private JsonMergeCommand(@Nullable ByteBuffer key, String path, @Nullable String value) {

			super(key);

			this.path = path;
			this.value = value;
		}

		/**
		 * Creates a new {@link JsonMergeCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonMergeCommand} for {@link ByteBuffer key}.
		 */
		public static JsonMergeCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonMergeCommand(key, ROOT_PATH, null);
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonMergeCommand} with {@literal path} applied.
		 */
		public JsonMergeCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonMergeCommand(getKey(), path, value);
		}

		/**
		 * Applies the JSON value. Constructs a new command instance with all previously configured properties.
		 *
		 * @param value the JSON value to merge.
		 * @return a new {@link JsonMergeCommand} with {@literal value} applied.
		 */
		public JsonMergeCommand value(String value) {
			return new JsonMergeCommand(getKey(), path, value);
		}

		public String getPath() {
			return path;
		}

		public @Nullable String getValue() {
			return value;
		}

	}

	/**
	 * Merge the JSON value at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value the JSON value to merge.
	 * @return {@link Mono} emitting {@literal true} if the key was merged, {@literal false} otherwise.
	 * @see <a href="https://redis.io/commands/json.merge">Redis Documentation: JSON.MERGE</a>
	 * @since 4.3
	 */
	default Mono<Boolean> jsonMerge(ByteBuffer key, String path, String value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonMerge(Mono.just(JsonMergeCommand.key(key).atPath(path).value(value)))
				.next()
				.map(BooleanResponse::getOutput);
	}

	/**
	 * Merge the JSON value at the given key and path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link BooleanResponse} holding the {@link JsonMergeCommand} along with the command result.
	 * @see <a href="https://redis.io/commands/json.merge">Redis Documentation: JSON.MERGE</a>
	 * @since 4.3
	 */
	Flux<BooleanResponse<JsonMergeCommand>> jsonMerge(Publisher<JsonMergeCommand> commands);

	/**
	 * {@code JSON.MGET} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.mget">Redis Documentation: JSON.MGET</a>
	 * @since 4.3
	 */
	class JsonMGetCommand implements Command {

		private final String path;
		private final List<ByteBuffer> keys;

		private JsonMGetCommand(String path, List<ByteBuffer> keys) {

			this.path = path;
			this.keys = List.copyOf(keys);
		}

		@Override
		public @Nullable ByteBuffer getKey() {
			return null;
		}

		/**
		 * Creates a new {@link JsonMGetCommand} given a {@link List} of keys and a root path.
		 *
		 * @param keys must not be {@literal null}.
		 * @return a new {@link JsonMGetCommand} for {@link List} of keys.
		 */
		public static JsonMGetCommand keys(List<ByteBuffer> keys) {

			Assert.notNull(keys, "Keys must not be null");

			return new JsonMGetCommand(ROOT_PATH, keys);
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonMGetCommand} with {@literal path} applied.
		 */
		public JsonMGetCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonMGetCommand(path, keys);
		}

		public String getPath() {
			return path;
		}

		public List<ByteBuffer> getKeys() {
			return keys;
		}

	}

	/**
	 * Get the JSON values at the given keys.
	 *
	 * @param keys must not be {@literal null}.
	 * @return list of JSON values or null if path does not exist.
	 * @see <a href="https://redis.io/commands/json.mget">Redis Documentation: JSON.MGET</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable String>> jsonMGet(List<ByteBuffer> keys) {

		Assert.notNull(keys, "Keys must not be null");

		return jsonMGet(Mono.just(JsonMGetCommand.keys(keys)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Get the JSON values at the given keys and path.
	 *
	 * @param keys must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return list of JSON values or null if path does not exist.
	 * @see <a href="https://redis.io/commands/json.mget">Redis Documentation: JSON.MGET</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable String>> jsonMGet(List<ByteBuffer> keys, String path) {

		Assert.notNull(keys, "Keys must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonMGet(Mono.just(JsonMGetCommand.keys(keys).atPath(path)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Get the JSON values at the given keys.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonMGetCommand} along with the command result.
	 * @see <a href="https://redis.io/commands/json.mget">Redis Documentation: JSON.MGET</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonMGetCommand, @Nullable String>> jsonMGet(Publisher<JsonMGetCommand> commands);

	/**
	 * {@code JSON.MSET} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.mset">Redis Documentation: JSON.MSET</a>
	 * @since 4.3
	 */
	class JsonMSetCommand implements Command {

		private final List<ReactiveJsonMSetArg> args;

		private JsonMSetCommand(List<ReactiveJsonMSetArg> args) {
			this.args = List.copyOf(args);
		}

		/**
		 * Creates a new {@link JsonMSetCommand} given a {@link List} of {@link ReactiveJsonMSetArg args}.
		 *
		 * @param args must not be {@literal null} or empty.
		 * @return a new {@link JsonMSetCommand} for {@link List} of {@link ReactiveJsonMSetArg args}.
		 */
		public static JsonMSetCommand args(List<ReactiveJsonMSetArg> args) {

			Assert.notEmpty(args, "Args must not be empty");
			Assert.noNullElements(args, "Args must not be null");

			return new JsonMSetCommand(args);
		}

		@Override
		public @Nullable ByteBuffer getKey() {
			return null;
		}

		public List<ReactiveJsonMSetArg> getArgs() {
			return args;
		}

		/**
		 * Argument for {@literal JSON.MSET} command.
		 *
		 * @param key the key, must not be {@literal null}.
		 * @param path the JSON path, must not be {@literal null}.
		 * @param value the value to set.
		 * @since 4.3
		 */
		public record ReactiveJsonMSetArg(ByteBuffer key, String path, @Nullable Object value) {

			public ReactiveJsonMSetArg {

				Assert.notNull(key, "Key must not be null");
				Assert.notNull(path, "Path must not be null");
			}

			public ReactiveJsonMSetArg(ByteBuffer key, @Nullable Object value) {
				this(key, ROOT_PATH, value);
			}

		}

	}

	/**
	 * Set the JSON value at the given key and path.
	 *
	 * @param args must not be {@literal null} or empty.
	 * @return {@link Mono} emitting {@literal true} if the key was set, {@literal false} otherwise.
	 * @see <a href="https://redis.io/commands/json.mset">Redis Documentation: JSON.MSET</a>
	 * @since 4.3
	 */
	default Mono<Boolean> jsonMSet(List<JsonMSetCommand.ReactiveJsonMSetArg> args) {

		Assert.notEmpty(args, "Args must not be empty");
		Assert.noNullElements(args, "Args must not be null");

		return jsonMSet(Mono.just(JsonMSetCommand.args(args)))
				.next()
				.map(BooleanResponse::getOutput);
	}

	/**
	 * Set the JSON value at the given key and path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link BooleanResponse} holding the {@link JsonMSetCommand} along with the command result.
	 * @see <a href="https://redis.io/commands/json.mset">Redis Documentation: JSON.MSET</a>
	 * @since 4.3
	 */
	Flux<BooleanResponse<JsonMSetCommand>> jsonMSet(Publisher<JsonMSetCommand> commands);

	/**
	 * {@literal JSON.SET} {@link Command}
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/docs/latest/commands/json.set/">Redis Documentation: JSON.SET</a>
	 * @since 4.3
	 */
	class JsonSetCommand extends KeyCommand {

		private final String path;
		private final @Nullable String value;
		private final JsonSetOption option;

		private JsonSetCommand(@Nullable ByteBuffer key, String path,
							  @Nullable String value, JsonSetOption option) {

			super(key);

			this.path = path;
			this.value = value;
			this.option = option;
		}

		/**
		 * Creates a new {@link JsonSetCommand} given a {@link ByteBuffer key}.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonSetCommand} for {@link ByteBuffer key}.
		 */
		public static JsonSetCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonSetCommand(key, ROOT_PATH, null, JsonSetOption.upsert());
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonSetCommand} with {@literal path} applied.
		 */
		public JsonSetCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonSetCommand(getKey(), path, value, option);
		}

		/**
		 * Applies the JSON value. Constructs a new command instance with all previously configured properties.
		 *
		 * @param value the JSON value to set.
		 * @return a new {@link JsonSetCommand} with {@literal value} applied.
		 */
		public JsonSetCommand value(@Nullable String value) {
			return new JsonSetCommand(getKey(), path, value, option);
		}

		/**
		 * Applies the {@link JsonSetOption}. Constructs a new command instance with all previously configured properties.
		 *
		 * @param option must not be {@literal null}.
		 * @return a new {@link JsonSetCommand} with {@link JsonSetOption} applied.
		 */
		public JsonSetCommand withOption(JsonSetOption option) {

			Assert.notNull(option, "JsonSetOption must not be null");

			return new JsonSetCommand(getKey(), path, value, option);
		}

		public String getPath() {
			return path;
		}

		public @Nullable String getValue() {
			return value;
		}

		public JsonSetOption getOption() {
			return option;
		}

	}

	/**
	 * Set the JSON value at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value the JSON value to set.
	 * @param option must not be {@literal null}.
	 * @return {@literal true} if the key was set, {@literal false} otherwise.
	 */
	default Mono<Boolean> jsonSet(ByteBuffer key, String path, @Nullable String value, JsonSetOption option) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(option, "JsonSetOption must not be null");

		return jsonSet(Mono.just(JsonSetCommand.key(key).atPath(path).value(value).withOption(option)))
				.next()
				.map(BooleanResponse::getOutput);
	}

	/**
	 * Set the JSON value at the given key.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link BooleanResponse} holding the {@link JsonSetCommand} along with the command result.
	 * @see <a href="https://redis.io/commands/json.set">Redis Documentation: JSON.SET</a>
	 * @since 4.3
	 */
	Flux<BooleanResponse<JsonSetCommand>> jsonSet(Publisher<JsonSetCommand> commands);

}
