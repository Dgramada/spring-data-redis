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

import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import static org.springframework.data.redis.core.JsonOperations.*;

/**
 * Redis JSON operations bound to a certain key.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
@NullUnmarked
public interface BoundJsonOperations<K> extends BoundKeyOperations<@NonNull K> {

	/**
	 * Execute a JSON array operation against the bound key as described by the given specification.
	 *
	 * @param spec function building the {@link JsonOperations.JsonArray} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperations.JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @since 4.2
	 */
	<T> T array(@NonNull Function<JsonOperations.JsonArray, ? extends JsonOperations.JsonOperation<T>> spec);

	/**
	 * Clear container values (arrays/objects) and set numeric values to {@code 0} at the document root of the bound key.
	 *
	 * @return the number of values cleared, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.clear/">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.2
	 */
	default Long clear() {
		return clear(ROOT_PATH);
	}

	/**
	 * Clear container values (arrays/objects) and set numeric values to {@code 0} at the given {@code path} of the bound key.
	 *
	 * @param path JSONPath expression; must not be {@literal null}.
	 * @return the number of values cleared, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.clear/">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.2
	 */
	Long clear(@NonNull String path);

	default Long delete() {
		return delete(ROOT_PATH);
	}

	/**
	 * Delete the JSON value stored at the bound key (entire document at the root path).
	 *
	 * @return the number of paths deleted, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.del/">Redis Documentation: JSON.DEL</a>
	 * @since 4.2
	 */
	Long delete(@NonNull String path);

	/**
	 * Retrieve the JSON value(s) stored at the bound key as described by the given specification.
	 *
	 * @param spec function building the {@link JsonOperations.JsonGet} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperations.JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.get/">Redis Documentation: JSON.GET</a>
	 * @since 4.2
	 */
	<T> T get(@NonNull Function<JsonOperations.JsonGet, ? extends JsonOperations.JsonOperation<T>> spec);

	/**
	 * Increment the numeric value(s) at a path of the bound key by a given number.
	 *
	 * @param spec function building the {@link JsonOperations.JsonIncrement} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperations.JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.numincrby/">Redis Documentation: JSON.NUMINCRBY</a>
	 * @since 4.2
	 */
	<T> T increment(@NonNull Function<JsonOperations.JsonIncrement, ? extends JsonOperations.JsonOperation<T>> spec);

	/**
	 * Merge a given JSON value into the document at the bound key using RFC7396 JSON Merge Patch semantics.
	 *
	 * @param spec function building the {@link JsonOperations.JsonMerge} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperations.JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.merge/">Redis Documentation: JSON.MERGE</a>
	 * @since 4.2
	 */
	<T> T merge(@NonNull Function<JsonOperations.JsonMerge, ? extends JsonOperations.JsonOperation<T>> spec);

	/**
	 * Set a JSON value at a path of the bound key as described by the given specification.
	 *
	 * @param spec function building the {@link JsonOperations.JsonSet} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperations.JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.set/">Redis Documentation: JSON.SET</a>
	 * @since 4.2
	 */
	<T> T set(@NonNull Function<JsonOperations.JsonSet, ? extends JsonOperations.JsonOperation<T>> spec);

	/**
	 * Execute a JSON string operation against the bound key as described by the given specification.
	 *
	 * @param spec function building the {@link JsonOperations.JsonString} operation; must not be {@literal null}.
	 * @param <T> the type produced by the resulting {@link JsonOperations.JsonOperation}.
	 * @return the operation result, or {@literal null} when used in pipeline / transaction.
	 * @since 4.2
	 */
	<T> T string(@NonNull Function<JsonOperations.JsonString, ? extends JsonOperations.JsonOperation<T>> spec);

	/**
	 * Toggle the boolean value(s) at the given {@code path} of the bound key.
	 *
	 * @param path JSONPath expression; must not be {@literal null}.
	 * @return the new boolean value(s) per matching path, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.toggle/">Redis Documentation: JSON.TOGGLE</a>
	 * @since 4.2
	 */
	List<Boolean> toggle(@NonNull String path);

	/**
	 * Report the {@link JsonOperations.JsonType type} of the JSON value at the document root of the bound key.
	 *
	 * @return the type(s) found at the path, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.type/">Redis Documentation: JSON.TYPE</a>
	 * @since 4.2
	 */
	default List<JsonOperations.JsonType> type() {
		return type(ROOT_PATH);
	}

	/**
	 * Report the {@link JsonOperations.JsonType type} of the JSON value(s) at the given {@code path} of the bound key.
	 *
	 * @param path JSONPath expression; must not be {@literal null}.
	 * @return the type(s) found at the path, or {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/json.type/">Redis Documentation: JSON.TYPE</a>
	 * @since 4.2
	 */
	List<JsonOperations.JsonType> type(@NonNull String path);

}
