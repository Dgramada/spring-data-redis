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

import org.jspecify.annotations.Nullable;

import org.springframework.core.ParameterizedTypeReference;

/**
 * Redis JSON operations.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
public interface JsonOperations<K> {

	String ROOT_PATH = "$";

	<T> @Nullable T array(K key, Function<JsonArray, ? extends JsonOperation<T>> spec);

	default @Nullable Long clear(K key) {
		return clear(key, ROOT_PATH);
	}

	@Nullable Long clear(K key, String path);

	default @Nullable Long delete(K key) {
		return delete(key, ROOT_PATH);
	}

	@Nullable Long delete(K key, String path);

	<T> @Nullable T get(K key, Function<JsonGet, ? extends JsonOperation<T>> spec);

	<T> @Nullable T increment(K key, Function<JsonIncrement, ? extends JsonOperation<T>> spec);

	<T> @Nullable T merge(K key, Function<JsonMerge, ? extends JsonOperation<T>> spec);

	default @Nullable JsonCollectionResult multiGet(Collection<K> keys) {
		return multiGet(keys, ROOT_PATH);
	}

	@Nullable JsonCollectionResult multiGet(Collection<K> keys, String path);

	<T> @Nullable T set(K key, Function<JsonSet, ? extends JsonOperation<T>> spec);

	<T> @Nullable T string(K key, Function<JsonString, ? extends JsonOperation<T>> spec);

	@Nullable List<@Nullable Boolean> toggle(K key, String path);

	@Nullable default List<@Nullable JsonType> type(K key) {
		return type(key, ROOT_PATH);
	}

	@Nullable List<@Nullable JsonType> type(K key, String path);

	interface JsonOperation<T extends @Nullable Object> {
	}

	interface BooleanResponse extends JsonOperation<@Nullable Boolean> {
	}

	interface NumericListResponse extends JsonOperation<@Nullable Number> {
	}

	interface JsonResponse extends JsonOperation<@Nullable JsonResult> {
	}

	interface LongListResponse extends JsonOperation<@Nullable List<@Nullable Long>> {
	}

	interface PathSpec<P extends PathSpec<P>> {

		default P atRoot() {
			return at(ROOT_PATH);
		}

		P at(String jsonPath);

	}

	interface JsonArray extends PathSpec<JsonArray> {

		LongListResponse append(@Nullable Object ... values);

		LongListResponse indexOf(@Nullable Object value);

		LongListResponse insert(int index, @Nullable Object ... values);

		LongListResponse length();

		LongListResponse trim(int start, int stop);

	}

	interface JsonGet {

		default JsonResponse atRoot() {
			return at(ROOT_PATH);
		}

		JsonResponse at(String... paths);

	}

	interface JsonIncrement extends PathSpec<JsonIncrement> {

		NumericListResponse by(Number number);

	}

	interface JsonMerge extends PathSpec<JsonMerge> {

		BooleanResponse with(@Nullable Object value);

	}

	interface JsonSet extends PathSpec<JsonSet> {

		JsonSet ifPresent();

		JsonSet ifAbsent();

		BooleanResponse to(@Nullable Object value);

	}

	interface JsonString extends PathSpec<JsonString> {

		LongListResponse append(String value);

		LongListResponse length();

	}

	interface JsonResult {

		<V> @Nullable V as(Class<V> type);

		<V> @Nullable V as(ParameterizedTypeReference<V> typeRef);

		@Nullable
		String asString();

		byte @Nullable [] asBytes();

		boolean isNull();

	}

	interface JsonCollectionResult extends Iterable<JsonResult> {

		<V> List<@Nullable V> as(Class<V> type);

		<V> List<@Nullable V> as(ParameterizedTypeReference<V> typeRef);

		List<@Nullable String> asString();

		List<byte @Nullable []> asBytes();

		int size();

		boolean isEmpty();

		Stream<JsonResult> stream();

	}

	enum JsonType {

		STRING,
		NUMBER,
		BOOLEAN,
		OBJECT,
		ARRAY,
		UNKNOWN

	}

}
