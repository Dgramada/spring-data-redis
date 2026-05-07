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
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import org.springframework.core.ParameterizedTypeReference;

/**
 * Redis JSON operations.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
@NullUnmarked
public interface JsonOperations<K> {

	/**
	 * The root path of a JSON document ({@code $}).
	 *
	 * @since 4.2
	 */
	String ROOT_PATH = "$";

	/**
	 * Execute a JSON array operation against {@code key} as described by the given specification.
	 *
	 * @param key must not be {@literal null}.
	 * @param spec function building the {@link JsonArray} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @since 4.2
	 */
	<T> T array(@NonNull K key, @NonNull Function<JsonArray, ? extends JsonOperation<T>> spec);

	/**
	 * Clear container values (arrays/objects) and set numeric values to {@code 0} at the document root of {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @return the number of values cleared, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.clear/">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.2
	 */
	default Long clear(@NonNull K key) {
		return clear(key, ROOT_PATH);
	}

	/**
	 * Clear container values (arrays/objects) and set numeric values to {@code 0} at the given {@code path} of {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param path JSONPath expression; must not be {@literal null}.
	 * @return the number of values cleared, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.clear/">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.2
	 */
	Long clear(@NonNull K key, @NonNull String path);

	/**
	 * Delete the JSON document stored at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @return the number of paths deleted, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.del/">Redis Documentation: JSON.DEL</a>
	 * @since 4.2
	 */
	default Long delete(@NonNull K key) {
		return delete(key, ROOT_PATH);
	}

	/**
	 * Delete the JSON value stored at {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @return the number of paths deleted, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.del/">Redis Documentation: JSON.DEL</a>
	 * @since 4.2
	 */
	Long delete(@NonNull K key, @NonNull String path);

	/**
	 * Retrieve the JSON value stored at {@code key} as described by the given specification.
	 *
	 * @param key must not be {@literal null}.
	 * @param spec function building the {@link JsonGet} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.get/">Redis Documentation: JSON.GET</a>
	 * @since 4.2
	 */
	<T> T get(@NonNull K key, @NonNull Function<JsonGet, ? extends JsonOperation<T>> spec);

	/**
	 * Increment the numeric value at a path of {@code key} by a given number.
	 *
	 * @param key must not be {@literal null}.
	 * @param spec function building the {@link JsonIncrement} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.numincrby/">Redis Documentation: JSON.NUMINCRBY</a>
	 * @since 4.2
	 */
	<T> T increment(@NonNull K key, @NonNull Function<JsonIncrement, ? extends JsonOperation<T>> spec);

	/**
	 * Merge a given JSON value into the document at {@code key} using RFC7396 JSON Merge Patch semantics.
	 *
	 * @param key must not be {@literal null}.
	 * @param spec function building the {@link JsonMerge} operation.
	 * @param <T> the type produced by the resulting {@link JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.merge/">Redis Documentation: JSON.MERGE</a>
	 */
	<T> T merge(@NonNull K key, Function<JsonMerge, ? extends JsonOperation<T>> spec);

	/**
	 * Retrieve the JSON values at the document root for each of the given {@code keys}.
	 *
	 * @param keys must not be {@literal null}.
	 * @return the collected results, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.mget/">Redis Documentation: JSON.MGET</a>
	 * @since 4.2
	 */
	default JsonCollectionResult multiGet(@NonNull Collection<K> keys) {
		return multiGet(keys, ROOT_PATH);
	}

	/**
	 * Retrieve the JSON values at the given {@code path} for each of the given {@code keys}.
	 *
	 * @param keys must not be {@literal null}.
	 * @param path JSONPath expression; must not be {@literal null}.
	 * @return the collected results, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.mget/">Redis Documentation: JSON.MGET</a>
	 * @since 4.2
	 */
	JsonCollectionResult multiGet(@NonNull Collection<K> keys, @NonNull String path);

	/**
	 * Set a JSON value at a path of {@code key} as described by the given specification.
	 *
	 * @param key must not be {@literal null}.
	 * @param spec function building the {@link JsonSet} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.set/">Redis Documentation: JSON.SET</a>
	 * @since 4.2
	 */
	<T> T set(@NonNull K key, @NonNull Function<JsonSet, ? extends JsonOperation<T>> spec);

	/**
	 * Execute a JSON string operation against {@code key} as described by the given specification.
	 *
	 * @param key must not be {@literal null}.
	 * @param spec function building the {@link JsonString} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @since 4.2
	 */
	<T> T string(@NonNull K key, @NonNull Function<JsonString, ? extends JsonOperation<T>> spec);

	/**
	 * Toggle the boolean value(s) at the given {@code path} of {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param path JSONPath expression; must not be {@literal null}.
	 * @return the new boolean value(s) per matching path, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.toggle/">Redis Documentation: JSON.TOGGLE</a>
	 * @since 4.2
	 */
	List<Boolean> toggle(@NonNull K key, @NonNull String path);

	/**
	 * Report the {@link JsonType type} of the JSON value at the document root of {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @return the type(s) found at the path, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.type/">Redis Documentation: JSON.TYPE</a>
	 * @since 4.2
	 */
	default List<JsonType> type(@NonNull K key) {
		return type(key, ROOT_PATH);
	}

	/**
	 * Report the {@link JsonType type} of the JSON value(s) at the given {@code path} of {@code key}.
	 *
	 * @param key must not be {@literal null}.
	 * @param path JSONPath expression; must not be {@literal null}.
	 * @return the type(s) found at the path, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.type/">Redis Documentation: JSON.TYPE</a>
	 * @since 4.2
	 */
	List<JsonType> type(@NonNull K key, @NonNull String path);

	/**
	 * Marker for a fully-built JSON operation that, when executed by the enclosing
	 * {@link JsonOperations}, yields a value of type {@code T}.
	 *
	 * @param <T> the type produced by executing this operation.
	 * @since 4.2
	 */
	interface JsonOperation<T> {
	}

	/**
	 * A {@link JsonOperation} producing a single {@link Boolean} result.
	 *
	 * @since 4.2
	 */
	interface BooleanResponse extends JsonOperation<Boolean> {
	}

	/**
	 * A {@link JsonOperation} producing a numeric result.
	 *
	 * @since 4.2
	 */
	interface NumericListResponse extends JsonOperation<Number> {
	}

	/**
	 * A {@link JsonOperation} producing a {@link JsonResult} that exposes the returned JSON document.
	 *
	 * @since 4.2
	 */
	interface JsonResponse extends JsonOperation<JsonResult> {
	}

	/**
	 * A {@link JsonOperation} producing a list of {@link Long} values, one per matching JSONPath.
	 *
	 * @since 4.2
	 */
	interface LongListResponse extends JsonOperation<List<Long>> {
	}

	/**
	 * Base specification for operations targeting a single JSONPath.
	 *
	 * @param <P> the concrete spec type returned by {@link #at(String)}.
	 * @since 4.2
	 */
	interface PathSpec<P extends PathSpec<P>> {

		/**
		 * Target the document {@link #ROOT_PATH root} ({@code $}).
		 *
		 * @return this specification.
		 * @since 4.2
		 */
		default P atRoot() {
			return at(ROOT_PATH);
		}

		/**
		 * Target the given JSONPath.
		 *
		 * @param jsonPath JSONPath expression; must not be {@literal null}.
		 * @return this specification for fluent chaining.
		 * @since 4.2
		 */
		P at(@NonNull String jsonPath);

	}

	/**
	 * Specification for JSON array operations executed via {@link JsonOperations#array(Object, Function)}.
	 *
	 * @since 4.2
	 */
	interface JsonArray extends PathSpec<JsonArray> {

		/**
		 * Append the given {@code values} to the end of the array at the configured path.
		 *
		 * @param values must not be {@literal null}.
		 * @return the new array length(s) per matching path.
		 * @see <a href="https://redis.io/commands/json.arrappend/">Redis Documentation: JSON.ARRAPPEND</a>
		 * @since 4.2
		 */
		LongListResponse append(Object @NonNull... values);

		/**
		 * Return the index of the first occurrence of {@code value} in the array at the configured path,
		 * or {@code -1} when not found.
		 *
		 * @param value the value to search for.
		 * @return the index(es) per matching path.
		 * @see <a href="https://redis.io/commands/json.arrindex/">Redis Documentation: JSON.ARRINDEX</a>
		 * @since 4.2
		 */
		LongListResponse indexOf(Object value);

		/**
		 * Insert the given {@code values} into the array at the configured path before the given {@code index}.
		 *
		 * @param index zero-based insertion index.
		 * @param values must not be {@literal null}.
		 * @return the new array length(s) per matching path.
		 * @see <a href="https://redis.io/commands/json.arrinsert/">Redis Documentation: JSON.ARRINSERT</a>
		 * @since 4.2
		 */
		LongListResponse insert(int index, Object @NonNull... values);

		/**
		 * Return the length of the array at the configured path.
		 *
		 * @return the array length(s) per matching path.
		 * @see <a href="https://redis.io/commands/json.arrlen/">Redis Documentation: JSON.ARRLEN</a>
		 * @since 4.2
		 */
		LongListResponse length();

		/**
		 * Trim the array at the configured path so that it contains only the elements in the inclusive
		 * {@code [start, stop]} range.
		 *
		 * @param start zero-based start index, inclusive.
		 * @param stop zero-based stop index, inclusive.
		 * @return the new array length(s) per matching path.
		 * @see <a href="https://redis.io/commands/json.arrtrim/">Redis Documentation: JSON.ARRTRIM</a>
		 * @since 4.2
		 */
		LongListResponse trim(int start, int stop);

	}

	/**
	 * Specification for {@code JSON.GET} executed via {@link JsonOperations#get(Object, Function)}.
	 *
	 * @since 4.2
	 */
	interface JsonGet {

		/**
		 * Read the document at the {@link #ROOT_PATH root}.
		 *
		 * @return the JSON response.
		 */
		default JsonResponse atRoot() {
			return at(ROOT_PATH);
		}

		/**
		 * Read the JSON value(s) at the given {@code paths}.
		 *
		 * @param paths one or more JSONPath expressions; must not be {@literal null} and must not contain {@literal null}.
		 * @return the JSON response.
		 * @see <a href="https://redis.io/commands/json.get/">Redis Documentation: JSON.GET</a>
		 * @since 4.2
		 */
		JsonResponse at(@NonNull String @NonNull... paths);

	}

	/**
	 * Specification for {@code JSON.NUMINCRBY} executed via {@link JsonOperations#increment(Object, Function)}.
	 *
	 * @since 4.2
	 */
	interface JsonIncrement extends PathSpec<JsonIncrement> {

		/**
		 * Increment the numeric value(s) at the configured path by the given {@code number}.
		 *
		 * @param number the increment value; must not be {@literal null}.
		 * @return the new numeric value(s).
		 * @see <a href="https://redis.io/commands/json.numincrby/">Redis Documentation: JSON.NUMINCRBY</a>
		 * @since 4.2
		 */
		NumericListResponse by(@NonNull Number number);

	}

	/**
	 * Specification for {@code JSON.MERGE} executed via {@link JsonOperations#merge(Object, Function)}.
	 *
	 * @since 4.2
	 */
	interface JsonMerge extends PathSpec<JsonMerge> {

		/**
		 * Merge the given {@code value} into the document at the configured path using
		 * RFC&nbsp;7396 JSON Merge Patch semantics.
		 *
		 * @param value the value to merge.
		 * @return {@literal true} if the merge succeeded.
		 * @see <a href="https://redis.io/commands/json.merge/">Redis Documentation: JSON.MERGE</a>
		 * @since 4.2
		 */
		BooleanResponse with(Object value);

	}

	/**
	 * Specification for {@code JSON.SET} executed via {@link JsonOperations#set(Object, Function)}.
	 *
	 * @since 4.2
	 */
	interface JsonSet extends PathSpec<JsonSet> {

		/**
		 * Only set the value if the path already exists ({@code XX}).
		 *
		 * @return this specification for fluent chaining.
		 * @see <a href="https://redis.io/commands/json.set/">Redis Documentation: JSON.SET</a>
		 * @since 4.2
		 */
		JsonSet ifPresent();

		/**
		 * Only set the value if the path does not already exist ({@code NX}).
		 *
		 * @return this specification for fluent chaining.
		 * @see <a href="https://redis.io/commands/json.set/">Redis Documentation: JSON.SET</a>
		 * @since 4.2
		 */
		JsonSet ifAbsent();

		/**
		 * Set the JSON value at the configured path to the given {@code value}.
		 *
		 * @param value the value to write.
		 * @return {@literal true} if the value was set; {@literal false} when the {@link #ifPresent() XX}
		 *         or {@link #ifAbsent() NX} condition was not met.
		 * @see <a href="https://redis.io/commands/json.set/">Redis Documentation: JSON.SET</a>
		 * @since 4.2
		 */
		BooleanResponse to(Object value);

	}

	/**
	 * Specification for JSON string operations executed via {@link JsonOperations#string(Object, Function)}.
	 *
	 * @since 4.2
	 */
	interface JsonString extends PathSpec<JsonString> {

		/**
		 * Append {@code value} to the JSON string at the configured path.
		 *
		 * @param value must not be {@literal null}.
		 * @return the new string length(s) per matching path.
		 * @see <a href="https://redis.io/commands/json.strappend/">Redis Documentation: JSON.STRAPPEND</a>
		 * @since 4.2
		 */
		LongListResponse append(@NonNull String value);

		/**
		 * Return the length of the JSON string at the configured path.
		 *
		 * @return the string length(s) per matching path.
		 * @see <a href="https://redis.io/commands/json.strlen/">Redis Documentation: JSON.STRLEN</a>
		 * @since 4.2
		 */
		LongListResponse length();

	}

	/**
	 * Accessor for a single JSON value returned by Redis. Provides typed conversion helpers as well as
	 * raw {@link String} / {@code byte[]} access to the underlying serialized payload.
	 *
	 * @since 4.2
	 */
	interface JsonResult {

		/**
		 * Convert the value to an instance of the given {@code type}.
		 *
		 * @param type target type; must not be {@literal null}.
		 * @param <V> the target type.
		 * @return the converted value, or {@literal null} if {@link #isNull() the value is null}.
		 * @since 4.2
		 */
		<V> V as(@NonNull Class<V> type);

		/**
		 * Convert the value using the given {@link ParameterizedTypeReference} (for generic targets).
		 *
		 * @param typeRef target type reference; must not be {@literal null}.
		 * @param <V> the target type.
		 * @return the converted value, or {@literal null} if {@link #isNull() the value is null}.
		 * @since 4.2
		 */
		<V> V as(@NonNull ParameterizedTypeReference<V> typeRef);

		/**
		 * @return the raw JSON payload as {@link String}, or {@literal null} when the value is absent.
		 * @since 4.2
		 */
		String asString();

		/**
		 * @return the raw JSON payload as {@code byte[]}, or {@literal null} when the value is absent.
		 * @since 4.2
		 */
		byte[] asBytes();

		/**
		 * @return {@literal true} when the path was missing or its value was JSON {@code null}.
		 * @since 4.2
		 */
		boolean isNull();

	}

	/**
	 * Accessor for a collection of {@link JsonResult} entries returned by multi-key operations.
	 *
	 * @since 4.2
	 */
	interface JsonCollectionResult extends Iterable<JsonResult> {

		/**
		 * Convert all results to instances of the given {@code type}.
		 *
		 * @param type target type; must not be {@literal null}.
		 * @param <V> the target type.
		 * @return the converted values; entries may be {@literal null} for missing keys.
		 * @since 4.2
		 */
		<V> List<V> as(@NonNull Class<V> type);

		/**
		 * Convert all results using the given {@link ParameterizedTypeReference}.
		 *
		 * @param typeRef target type reference; must not be {@literal null}.
		 * @param <V> the target type.
		 * @return the converted values; entries may be {@literal null} for missing keys.
		 * @since 4.2
		 */
		<V> List<V> as(@NonNull ParameterizedTypeReference<V> typeRef);

		/**
		 * @return raw JSON payloads as {@link String}; entries may be {@literal null} for missing keys.
		 * @since 4.2
		 */
		List<String> asString();

		/**
		 * @return raw JSON payloads as {@code byte[]}; entries may be {@literal null} for missing keys.
		 * @since 4.2
		 */
		List<byte[]> asBytes();

		/**
		 * @return the number of result entries.
		 * @since 4.2
		 */
		int size();

		/**
		 * @return {@literal true} when there are no result entries.
		 * @since 4.2
		 */
		boolean isEmpty();

		/**
		 * @return a sequential {@link Stream} over the contained {@link JsonResult} entries.
		 * @since 4.2
		 */
		Stream<JsonResult> stream();

	}

	/**
	 * The JSON value type as reported by {@code JSON.TYPE}.
	 *
	 * @see <a href="https://redis.io/commands/json.type/">Redis Documentation: JSON.TYPE</a>
	 * @since 4.2
	 */
	enum JsonType {

		STRING,
		NUMBER,
		BOOLEAN,
		OBJECT,
		ARRAY,
		UNKNOWN

	}

}
