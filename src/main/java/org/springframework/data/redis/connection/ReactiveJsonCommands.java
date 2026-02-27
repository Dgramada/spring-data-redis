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
		 * @since 4.3
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
		 * @since 4.3
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
	 * {@code JSON.ARRAPPEND} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.arrappend">Redis Documentation: JSON.ARRAPPEND</a>
	 * @since 4.3
	 */
	class JsonArrAppendCommand extends KeyCommand {

		private final String path;
		private final List<String> values;

		public JsonArrAppendCommand(@Nullable ByteBuffer key, String path, List<String> values) {

			super(key);

			this.path = path;
			this.values = values;
		}

		/**
		 * Creates a new {@link JsonArrAppendCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonArrAppendCommand} for {@link ByteBuffer key}.
		 * @since 4.3
		 */
		public static JsonArrAppendCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonArrAppendCommand(key, ROOT_PATH, List.of());
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonArrAppendCommand} with {@literal path} applied.
		 * @since 4.3
		 */
		public JsonArrAppendCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonArrAppendCommand(getKey(), path, values);
		}

		/**
		 * Applies the JSON values. Constructs a new command instance with all previously configured properties.
		 *
		 * @param values must not be {@literal null}.
		 * @return a new {@link JsonArrAppendCommand} with {@literal values} applied.
		 * @since 4.3
		 */
		public JsonArrAppendCommand withValues(String[] values) {

			Assert.notEmpty(values, "Values must not be empty");
			Assert.noNullElements(values, "Values must not contain null elements");

			return new JsonArrAppendCommand(getKey(), path, List.of(values));
		}

		public String getPath() {
			return path;
		}

		public List<String> getValues() {
			return values;
		}

	}

	/**
	 * Append the JSON values into the array at path after the last element in it.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param values must not be {@literal null}.
	 * @return {@link Mono} emitting a list of new array lengths for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new array size for each matched array path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrappend">Redis Documentation: JSON.ARRAPPEND</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonArrAppend(ByteBuffer key, String path, String... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.noNullElements(values, "Values must not contain null elements");

		return jsonArrAppend(Mono.just(JsonArrAppendCommand.key(key).atPath(path).withValues(values)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Append the JSON values into the array at path after the last element in it.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonArrAppendCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new array sizes for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrappend">Redis Documentation: JSON.ARRAPPEND</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonArrAppendCommand, @Nullable Long>> jsonArrAppend(Publisher<JsonArrAppendCommand> commands);

	/**
	 * {@code JSON.ARRINDEX} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.arrindex">Redis Documentation: JSON.ARRINDEX</a>
	 * @since 4.3
	 */
	class JsonArrIndexCommand extends JsonCommand {

		private final String path;
		private final @Nullable String value;
		private final long start;
		private final long stop;

		public JsonArrIndexCommand(@Nullable ByteBuffer key, String path, @Nullable String value,
								   long start, long stop) {

			super(key, path);

			this.path = path;
			this.value = value;
			this.start = start;
			this.stop = stop;
		}

		/**
		 * Creates a new {@link JsonArrIndexCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonArrIndexCommand} for {@link ByteBuffer key}.
		 * @since 4.3
		 */
		public static JsonArrIndexCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonArrIndexCommand(key, ROOT_PATH, null, 0, 0);
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonArrIndexCommand} with {@literal path} applied.
		 * @since 4.3
		 */
		public JsonArrIndexCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonArrIndexCommand(getKey(), path, value, start, stop);
		}

		/**
		 * Applies the JSON value. Constructs a new command instance with all previously configured properties.
		 *
		 * @param value must not be {@literal null}.
		 * @return a new {@link JsonArrIndexCommand} with {@literal value} applied.
		 * @since 4.3
		 */
		public JsonArrIndexCommand withValue(String value) {

			Assert.notNull(value, "Value must not be null");

			return new JsonArrIndexCommand(getKey(), path, value, start, stop);
		}

		/**
		 * Applies the start index. Constructs a new command instance with all previously configured properties.
		 *
		 * @param start the start index.
		 * @return a new {@link JsonArrIndexCommand} with {@literal start} applied.
		 * @since 4.3
		 */
		public JsonArrIndexCommand fromIndex(long start) {
			return new JsonArrIndexCommand(getKey(), path, value, start, stop);
		}

		/**
		 * Applies the stop index. Constructs a new command instance with all previously configured properties.
		 *
		 * @param stop the stop index.
		 * @return a new {@link JsonArrIndexCommand} with {@literal stop} applied.
		 * @since 4.3
		 */
		public JsonArrIndexCommand toIndex(long stop) {
			return new JsonArrIndexCommand(getKey(), path, value, start, stop);
		}

		public String getPath() {
			return path;
		}

		public @Nullable String getValue() {
			return value;
		}

		public long getStart() {
			return start;
		}

		public long getStop() {
			return stop;
		}

	}

	/**
	 * Search for the first occurrence of a JSON value in an array.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value must not be {@literal null}.
	 * @return {@link Mono} emitting a list of indices for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - index of first occurrence for each matched path ({@code -1} if not found)</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrindex">Redis Documentation: JSON.ARRINDEX</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonArrIndex(ByteBuffer key, String path, String value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		return jsonArrIndex(Mono.just(JsonArrIndexCommand.key(key).atPath(path).withValue(value)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Search for the first occurrence of a JSON value in an array.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value must not be {@literal null}.
	 * @param start index to begin searching from.
	 * @return {@link Mono} emitting a list of indices for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - index of first occurrence for each matched path ({@code -1} if not found)</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrindex">Redis Documentation: JSON.ARRINDEX</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonArrIndex(ByteBuffer key, String path, String value, long start) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		return jsonArrIndex(Mono.just(JsonArrIndexCommand.key(key).atPath(path).withValue(value).fromIndex(start)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Search for the first occurrence of a JSON value in an array.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value must not be {@literal null}.
	 * @param start index to begin searching from.
	 * @param stop index to stop searching at (exclusive).
	 * @return {@link Mono} emitting a list of indices for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - index of first occurrence for each matched path ({@code -1} if not found)</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrindex">Redis Documentation: JSON.ARRINDEX</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonArrIndex(ByteBuffer key, String path, String value, long start, long stop) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		return jsonArrIndex(Mono.just(JsonArrIndexCommand.key(key).atPath(path).withValue(value).fromIndex(start).toIndex(stop)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Search for the first occurrence of a JSON value in an array.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonArrIndexCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - index of first occurrence for each matched path ({@code -1} if not found)</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrindex">Redis Documentation: JSON.ARRINDEX</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonArrIndexCommand, @Nullable Long>> jsonArrIndex(Publisher<JsonArrIndexCommand> commands);

	/**
	 * {@code JSON.ARRINSERT} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.arrinsert">Redis Documentation: JSON.ARRINSERT</a>
	 * @since 4.3
	 */
	class JsonArrInsertCommand extends KeyCommand {

		private final String path;
		private final int index;
		private final List<String> values;

		private JsonArrInsertCommand(@Nullable ByteBuffer key, String path, int index, List<String> values) {

			super(key);

			this.path = path;
			this.index = index;
			this.values = List.copyOf(values);
		}

		/**
		 * Creates a new {@link JsonArrInsertCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonArrInsertCommand} for {@link ByteBuffer key}.
		 * @since 4.3
		 */
		public static JsonArrInsertCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonArrInsertCommand(key, ROOT_PATH, 0, List.of());
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonArrInsertCommand} with {@literal path} applied.
		 * @since 4.3
		 */
		public JsonArrInsertCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonArrInsertCommand(getKey(), path, index, values);
		}

		/**
		 * Applies the index. Constructs a new command instance with all previously configured properties.
		 *
		 * @param index to insert before.
		 * @return a new {@link JsonArrInsertCommand} with {@literal index} applied.
		 * @since 4.3
		 */
		public JsonArrInsertCommand onIndex(int index) {
			return new JsonArrInsertCommand(getKey(), path, index, values);
		}

		/**
		 * Applies the values. Constructs a new command instance with all previously configured properties.
		 *
		 * @param values must not be {@literal null}.
		 * @return a new {@link JsonArrInsertCommand} with {@literal values} applied.
		 * @since 4.3
		 */
		public JsonArrInsertCommand withValues(String... values) {

			Assert.notEmpty(values, "Values must not be empty");
			Assert.noNullElements(values, "Values must not contain null elements");

			return new JsonArrInsertCommand(getKey(), path, index, List.of(values));
		}

		public String getPath() {
			return path;
		}

		public int getIndex() {
			return index;
		}

		public List<String> getValues() {
			return values;
		}

	}

	/**
	 * Insert the {@code values} into the array at path before {@code index}.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param index to insert before.
	 * @param values must not be {@literal null}. {@code null} values should be represented as JSON "null" strings.
	 * @return {@link Mono} emitting a list of new array sizes for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new array size for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrinsert">Redis Documentation: JSON.ARRINSERT</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonArrInsert(ByteBuffer key, String path, int index, String... values) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.noNullElements(values, "Values must not contain null elements");

		return jsonArrInsert(Mono.just(JsonArrInsertCommand.key(key).atPath(path).onIndex(index).withValues(values)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Insert the {@code values} into the array at path before {@code index}.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonArrInsertCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new array size for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrinsert">Redis Documentation: JSON.ARRINSERT</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonArrInsertCommand, @Nullable Long>> jsonArrInsert(Publisher<JsonArrInsertCommand> commands);

	/**
	 * Get the length of the array at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return {@link Mono} emitting a list of array lengths for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - array length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrlen">Redis Documentation: JSON.ARRLEN</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonArrLen(ByteBuffer key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonArrLen(Mono.just(JsonCommand.key(key).atPath(path)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Get the length of the array at the given key and path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - array length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrlen">Redis Documentation: JSON.ARRLEN</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonCommand, @Nullable Long>> jsonArrLen(Publisher<JsonCommand> commands);

	/**
	 * Pop and return the last element from the array at the given path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return {@link Mono} emitting a list of popped values for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code String} values - popped JSON value for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array or the array is empty</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrpop">Redis Documentation: JSON.ARRPOP</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable String>> jsonArrPop(ByteBuffer key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonArrPop(Mono.just(JsonCommand.key(key).atPath(path)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Pop and return the last element from the array at the given path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code String} values - popped JSON value for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array or the array is empty</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrpop">Redis Documentation: JSON.ARRPOP</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonCommand, @Nullable String>> jsonArrPop(Publisher<JsonCommand> commands);

	/**
	 * {@code JSON.ARRTRIM} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.arrtrim">Redis Documentation: JSON.ARRTRIM</a>
	 * @since 4.3
	 */
	class JsonArrTrimCommand extends JsonCommand {

		private final String path;
		private final long start;
		private final long stop;

		private JsonArrTrimCommand(@Nullable ByteBuffer key, String path,
								   long start, long stop) {

			super(key, path);

			this.path = path;
			this.start = start;
			this.stop = stop;
		}

		/**
		 * Creates a new {@link JsonArrTrimCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonArrTrimCommand} for {@link ByteBuffer key}.
		 * @since 4.3
		 */
		public static JsonArrTrimCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonArrTrimCommand(key, ROOT_PATH, 0, 0);
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonArrTrimCommand} with {@literal path} applied.
		 * @since 4.3
		 */
		public JsonArrTrimCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonArrTrimCommand(getKey(), path, start, stop);
		}

		/**
		 * Applies the start index. Constructs a new command instance with all previously configured properties.
		 *
		 * @param start the start index.
		 * @return a new {@link JsonArrTrimCommand} with {@literal start} applied.
		 * @since 4.3
		 */
		public JsonArrTrimCommand fromIndex(long start) {
			return new JsonArrTrimCommand(getKey(), path, start, stop);
		}

		/**
		 * Applies the stop index. Constructs a new command instance with all previously configured properties.
		 *
		 * @param stop the stop index.
		 * @return a new {@link JsonArrTrimCommand} with {@literal stop} applied.
		 * @since 4.3
		 */
		public JsonArrTrimCommand toIndex(long stop) {
			return new JsonArrTrimCommand(getKey(), path, start, stop);
		}

		public String getPath() {
			return path;
		}

		public long getStart() {
			return start;
		}

		public long getStop() {
			return stop;
		}

	}

	/**
	 * Trim the array at the given path to the given range.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param start index to start trimming from (inclusive).
	 * @param stop index to stop trimming at (inclusive).
	 * @return {@link Mono} emitting a list of new array lengths for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new array length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrtrim">Redis Documentation: JSON.ARRTRIM</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonArrTrim(ByteBuffer key, String path, int start, int stop) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonArrTrim(Mono.just(JsonArrTrimCommand.key(key).atPath(path).fromIndex(start).toIndex(stop)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Trim the array at the given path to the given range.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonArrTrimCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new array length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not an array</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.arrtrim">Redis Documentation: JSON.ARRTRIM</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonArrTrimCommand, @Nullable Long>> jsonArrTrim(Publisher<JsonArrTrimCommand> commands);

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
		 * @since 4.3
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
		 * @since 4.3
		 */
		public JsonGetCommand atPath(String... paths) {

			Assert.noNullElements(paths, "Paths must not contain null elements");

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
		Assert.noNullElements(paths, "Paths must not contain null elements");

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
		 * @since 4.3
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
		 * @since 4.3
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
		 * @since 4.3
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
		 * @since 4.3
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
		 * @since 4.3
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
	 * Get the JSON values at the given keys using the root path.
	 *
	 * @param keys must not be {@literal null}.
	 * @return {@link Mono} emitting a list of JSON values, one for each key. {@code null} elements indicate
	 *         that the key does not exist or the path does not exist within the document.
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
	 * @return {@link Mono} emitting a list of JSON values, one for each key. {@code null} elements indicate
	 *         that the key does not exist or the path does not exist within the document.
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
	 *         The output list contains one JSON value per key. {@code null} elements indicate that the key does not
	 *         exist or the path does not exist within the document.
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
		 * @since 4.3
		 */
		public static JsonMSetCommand args(List<ReactiveJsonMSetArg> args) {

			Assert.notEmpty(args, "Args must not be empty");
			Assert.noNullElements(args, "Args must not contain null elements");

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
		Assert.noNullElements(args, "Args must not contain null elements");

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
	 * {@code JSON.NUMINCRBY} command parameters.
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.numincrby">Redis Documentation: JSON.NUMINCRBY</a>
	 * @since 4.3
	 */
	class JsonNumIncrByCommand extends KeyCommand {

		private final String path;
		private final @Nullable Number number;

		private JsonNumIncrByCommand(@Nullable ByteBuffer key, String path, @Nullable Number number) {

			super(key);

			this.path = path;
			this.number = number;
		}

		/**
		 * Creates a new {@link JsonNumIncrByCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonNumIncrByCommand} for {@link ByteBuffer key}.
		 * @since 4.3
		 */
		public static JsonNumIncrByCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonNumIncrByCommand(key, ROOT_PATH, null);
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonNumIncrByCommand} with {@literal path} applied.
		 * @since 4.3
		 */
		public JsonNumIncrByCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonNumIncrByCommand(getKey(), path, number);
		}

		/**
		 * Applies the number. Constructs a new command instance with all previously configured properties.
		 *
		 * @param number must not be {@literal null}.
		 * @return a new {@link JsonNumIncrByCommand} with {@literal number} applied.
		 * @since 4.3
		 */
		public JsonNumIncrByCommand by(Number number) {

			Assert.notNull(number, "Number must not be null");

			return new JsonNumIncrByCommand(getKey(), path, number);
		}

		public String getPath() {
			return path;
		}

		public @Nullable Number getNumber() {
			return number;
		}

	}

	/**
	 * Increment the number value at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param number must not be {@literal null}.
	 * @return {@link Mono} emitting a list of new values for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Number} values - new value after increment for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a number</li>
	 *         </ul>
	 * @see <a href="https://redis.io/docs/latest/commands/json.numincrby/">Redis Documentation: JSON.NUMINCRBY</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Number>> jsonNumIncrBy(ByteBuffer key, String path, Number number) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(number, "Number must not be null");

		return jsonNumIncrBy(Mono.just(JsonNumIncrByCommand.key(key).atPath(path).by(number)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Increment the number value at the given key and path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonNumIncrByCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Number} values - new value after increment for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a number</li>
	 *         </ul>
	 * @see <a href="https://redis.io/docs/latest/commands/json.numincrby/">Redis Documentation: JSON.NUMINCRBY</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonNumIncrByCommand, @Nullable Number>> jsonNumIncrBy(Publisher<JsonNumIncrByCommand> commands);

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
		 * @since 4.3
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
		 * @since 4.3
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
		 * @since 4.3
		 */
		public JsonSetCommand value(@Nullable String value) {
			return new JsonSetCommand(getKey(), path, value, option);
		}

		/**
		 * Applies the {@link JsonSetOption}. Constructs a new command instance with all previously configured properties.
		 *
		 * @param option must not be {@literal null}.
		 * @return a new {@link JsonSetCommand} with {@link JsonSetOption} applied.
		 * @since 4.3
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
	 * @see <a href="https://redis.io/commands/json.set">Redis Documentation: JSON.SET</a>
	 * @since 4.3
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

	/**
	 * {@literal JSON.STRAPPEND} {@link Command}
	 *
	 * @author Yordan Tsintsov
	 * @see <a href="https://redis.io/commands/json.strappend">Redis Documentation: JSON.STRAPPEND</a>
	 * @since 4.3
	 */
	class JsonStrAppendCommand extends KeyCommand {

		private final String path;
		private final @Nullable String value;

		private JsonStrAppendCommand(@Nullable ByteBuffer key, String path, @Nullable String value) {

			super(key);

			this.path = path;
			this.value = value;
		}

		/**
		 * Creates a new {@link JsonStrAppendCommand} given a {@link ByteBuffer key} and a root path.
		 *
		 * @param key must not be {@literal null}.
		 * @return a new {@link JsonStrAppendCommand} for {@link ByteBuffer key}.
		 * @since 4.3
		 */
		public static JsonStrAppendCommand key(ByteBuffer key) {

			Assert.notNull(key, "Key must not be null");

			return new JsonStrAppendCommand(key, ROOT_PATH, null);
		}

		/**
		 * Applies the JSON path. Constructs a new command instance with all previously configured properties.
		 *
		 * @param path must not be {@literal null}.
		 * @return a new {@link JsonStrAppendCommand} with {@literal path} applied.
		 * @since 4.3
		 */
		public JsonStrAppendCommand atPath(String path) {

			Assert.notNull(path, "Path must not be null");

			return new JsonStrAppendCommand(getKey(), path, value);
		}

		/**
		 * Applies the JSON value. Constructs a new command instance with all previously configured properties.
		 *
		 * @param value must not be {@literal null}.
		 * @return a new {@link JsonStrAppendCommand} with {@literal value} applied.
		 * @since 4.3
		 */
		public JsonStrAppendCommand withValue(String value) {

			Assert.notNull(value, "Value must not be null");

			return new JsonStrAppendCommand(getKey(), path, value);
		}

		public String getPath() {
			return path;
		}

		public @Nullable String getValue() {
			return value;
		}

	}

	/**
	 * Append the string JSON value into the string at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value must not be {@literal null}.
	 * @return {@link Mono} emitting a list of new string lengths for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new string length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a string</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.strappend">Redis Documentation: JSON.STRAPPEND</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonStrAppend(ByteBuffer key, String path, String value) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		return jsonStrAppend(Mono.just(JsonStrAppendCommand.key(key).atPath(path).withValue(value)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Append the string JSON value into the string at the given key and path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonStrAppendCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - new string length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a string</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.strappend">Redis Documentation: JSON.STRAPPEND</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonStrAppendCommand, @Nullable Long>> jsonStrAppend(Publisher<JsonStrAppendCommand> commands);

	/**
	 * Get the string length at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return {@link Mono} emitting a list of string lengths for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - string length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a string</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.strlen">Redis Documentation: JSON.STRLEN</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Long>> jsonStrLen(ByteBuffer key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonStrLen(Mono.just(JsonCommand.key(key).atPath(path)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Get the string length at the given key and path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Long} values - string length for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a string</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.strlen">Redis Documentation: JSON.STRLEN</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonCommand, @Nullable Long>> jsonStrLen(Publisher<JsonCommand> commands);

	/**
	 * Toggle boolean values at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return {@link Mono} emitting a list of new boolean values for each matched path:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Boolean} values - new boolean value for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a boolean</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.toggle">Redis Documentation: JSON.TOGGLE</a>
	 * @since 4.3
	 */
	default Mono<List<@Nullable Boolean>> jsonToggle(ByteBuffer key, String path) {

		Assert.notNull(key, "Key must not be null");
		Assert.notNull(path, "Path must not be null");

		return jsonToggle(Mono.just(JsonCommand.key(key).atPath(path)))
				.next()
				.map(MultiValueResponse::getOutput);
	}

	/**
	 * Toggle boolean values at the given key and path.
	 *
	 * @param commands must not be {@literal null}.
	 * @return {@link Flux} of {@link MultiValueResponse} holding the {@link JsonCommand} along with the command result. The output list contains:
	 *         <ul>
	 *           <li>Empty list - if the key does not exist or path matches no elements</li>
	 *           <li>List with {@code Boolean} values - new boolean value for each matched path</li>
	 *           <li>{@code null} elements - if a matched path value is not a boolean</li>
	 *         </ul>
	 * @see <a href="https://redis.io/commands/json.toggle">Redis Documentation: JSON.TOGGLE</a>
	 * @since 4.3
	 */
	Flux<MultiValueResponse<JsonCommand, @Nullable Boolean>> jsonToggle(Publisher<JsonCommand> commands);

}
