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

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

import static org.springframework.data.redis.core.JsonOperations.*;

/**
 * Redis JSON operations bound to a certain key.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
public interface BoundJsonOperations<K> extends BoundKeyOperations<K> {

	<T> @Nullable T array(Function<JsonOperations.JsonArray, ? extends JsonOperations.JsonOperation<T>> spec);

	default @Nullable Long clear() {
		return clear(ROOT_PATH);
	}

	@Nullable Long clear(String path);

	default @Nullable Long delete() {
		return delete(ROOT_PATH);
	}

	@Nullable Long delete(String path);

	<T> @Nullable T get(Function<JsonOperations.JsonGet, ? extends JsonOperations.JsonOperation<T>> spec);

	<T> @Nullable T increment(Function<JsonOperations.JsonIncrement, ? extends JsonOperations.JsonOperation<T>> spec);

	<T> @Nullable T merge(Function<JsonOperations.JsonMerge, ? extends JsonOperations.JsonOperation<T>> spec);

	<T> @Nullable T set(Function<JsonOperations.JsonSet, ? extends JsonOperations.JsonOperation<T>> spec);

	<T> @Nullable T string(Function<JsonOperations.JsonString, ? extends JsonOperations.JsonOperation<T>> spec);

	@Nullable List<@Nullable Boolean> toggle(String path);

	@Nullable default List<JsonOperations.@Nullable JsonType> type() {
		return type(ROOT_PATH);
	}

	@Nullable List<JsonOperations.@Nullable JsonType> type(String path);

}
