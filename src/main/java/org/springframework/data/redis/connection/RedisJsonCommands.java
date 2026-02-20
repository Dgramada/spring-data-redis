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
import org.springframework.util.Assert;

import java.util.List;

/**
 * JSON commands supported by Redis.
 *
 * @author Yordan Tsintsov
 * @see RedisCommands
 * @since 4.3
 */
public interface RedisJsonCommands {

	String ROOT_PATH = "$";

	/**
	 * Clear container values (arrays/objects) and set numeric values to 0 at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @return the number of paths cleared.
	 * @see <a href="https://redis.io/docs/latest/commands/json.clear/">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.3
	 */
	default Long jsonClear(byte[] key) {

		Assert.notNull(key, "Key must not be null");

		return jsonClear(key, ROOT_PATH);
	}

	/**
	 * Clear container values (arrays/objects) and set numeric values to 0 at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return the number of paths cleared.
	 * @see <a href="https://redis.io/docs/latest/commands/json.clear/">Redis Documentation: JSON.CLEAR</a>
	 * @since 4.3
	 */
	Long jsonClear(byte[] key, String path);

	/**
	 * Delete the JSON value at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @return the number of paths deleted.
	 * @see <a href="https://redis.io/docs/latest/commands/json.del/">Redis Documentation: JSON.DEL</a>
	 * @since 4.3
	 */
	default Long jsonDel(byte[] key) {

		Assert.notNull(key, "Key must not be null");

		return jsonDel(key, ROOT_PATH);
	}

	/**
	 * Delete the JSON value at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @return the number of paths deleted.
	 * @see <a href="https://redis.io/docs/latest/commands/json.del/">Redis Documentation: JSON.DEL</a>
	 * @since 4.3
	 */
	Long jsonDel(byte[] key, String path);

	/**
	 * Get the JSON values at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @return list of JSON values or {@literal null} if path does not exist. Absent field values are represented using {@literal null}.
	 * @see <a href="https://redis.io/docs/latest/commands/json.get/">Redis Documentation: JSON.GET</a>
	 * @since 4.3
	 */
	default @Nullable List<@Nullable String> jsonGet(byte[] key) {

		Assert.notNull(key, "Key must not be null");

		return jsonGet(key, ROOT_PATH);
	}

	/**
	 * Get the JSON values at the given key and paths.
	 *
	 * @param key must not be {@literal null}.
	 * @param paths must not be {@literal null}.
	 * @return list of JSON values or null if path does not exist.
	 * @see <a href="https://redis.io/docs/latest/commands/json.get/">Redis Documentation: JSON.GET</a>
	 * @since 4.3
	 */
	@Nullable List<@Nullable String> jsonGet(byte[] key, String... paths);

	/**
	 * Merge the JSON value at the given key and path.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value the JSON value to merge.
	 * @return {@literal true} if the key was merged, {@literal false} otherwise.
	 * @see <a href="https://redis.io/docs/latest/commands/json.merge/">Redis Documentation: JSON.MERGE</a>
	 * @since 4.3
	 */
	Boolean jsonMerge(byte[] key, String path, @Nullable String value);

	/**
	 * Get the JSON values at the given keys.
	 *
	 * @param keys must not be {@literal null}.
	 * @return list of JSON values or {@literal null} if path does not exist. Absent field values are represented using {@literal null}.
	 * @see <a href="https://redis.io/docs/latest/commands/json.mget/">Redis Documentation: JSON.MGET</a>
	 * @since 4.3
	 */
	default @Nullable List<@Nullable String> jsonMGet(byte[]... keys) {

		Assert.notEmpty(keys, "Keys must not be empty");
		Assert.noNullElements(keys, "Keys must not be null");

		return jsonMGet(ROOT_PATH, keys);
	}

	/**
	 * Get the JSON values at the given keys and paths.
	 *
	 * @param path must not be {@literal null}.
	 * @param keys must not be {@literal null}.
	 * @return list of JSON values or null if path does not exist.
	 * @see <a href="https://redis.io/docs/latest/commands/json.mget/">Redis Documentation: JSON.MGET</a>
	 * @since 4.3
	 */
	@Nullable List<@Nullable String> jsonMGet(String path, byte[]... keys);

	/**
	 * Set the JSON values at the given keys and paths.
	 *
	 * @param args must not be {@literal null}.
	 * @return {@literal true} if the keys were set, {@literal false} otherwise. Absent field values are represented using {@literal null}.
	 * @see <a href="https://redis.io/docs/latest/commands/json.mset/">Redis Documentation: JSON.MSET</a>
	 * @since 4.3
	 */
	Boolean jsonMSet(List<JsonMSetArg> args);

	/**
	 * Set the JSON value at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @param value the JSON value to set.
	 * @return {@literal true} if the key was set, {@literal false} otherwise.
	 * @see <a href="https://redis.io/docs/latest/commands/json.set/">Redis Documentation: JSON.SET</a>
	 * @since 4.3
	 */
	default Boolean jsonSet(byte[] key, @Nullable String value) {

		Assert.notNull(key, "Key must not be null");

		return jsonSet(key, ROOT_PATH, value, JsonSetOption.upsert());
	}

	/**
	 * Set the JSON value at the given key.
	 *
	 * @param key must not be {@literal null}.
	 * @param path must not be {@literal null}.
	 * @param value the JSON value to set.
	 * @param option must not be {@literal null}.
	 * @return {@literal true} if the key was set, {@literal false} otherwise.
	 * @see <a href="https://redis.io/docs/latest/commands/json.set/">Redis Documentation: JSON.SET</a>
	 * @since 4.3
	 */
	Boolean jsonSet(byte[] key, String path, @Nullable String value, JsonSetOption option);

	/**
	 * {@code JSON.SET} command arguments for {@code NX}, {@code XX}.
	 */
	enum JsonSetOption {

		/**
		 * Do not set any additional command argument.
		 */
		UPSERT,

		/**
		 * {@code NX}
		 */
		IF_PATH_NOT_EXISTS,

		/**
		 * {@code XX}
		 */
		IF_PATH_EXISTS;

		/**
		 * Do not set any additional command argument.
		 *
		 * @return {@link JsonSetOption#UPSERT}
		 */
		public static JsonSetOption upsert() {
			return UPSERT;
		}

		/**
		 * {@code NX}
		 *
		 * @return {@link JsonSetOption#IF_PATH_NOT_EXISTS}
		 */
		public static JsonSetOption ifPathNotExists() {
			return IF_PATH_NOT_EXISTS;
		}

		/**
		 * {@code XX}
		 *
		 * @return {@link JsonSetOption#IF_PATH_EXISTS}
		 */
		public static JsonSetOption ifPathExists() {
			return IF_PATH_EXISTS;
		}

	}

	/**
	 * Arguments for {@code JSON.MSET} command.
	 *
	 * @param key the key, must not be {@literal null}.
	 * @param path the JSON path, must not be {@literal null}.
	 * @param value the value to set.
	 * @since 4.3
	 */
	record JsonMSetArg(byte[] key, String path, @Nullable Object value) {

		public JsonMSetArg {
			Assert.notNull(key, "Key must not be null");
			Assert.notNull(path, "Path must not be null");
		}

		public JsonMSetArg(byte[] key, @Nullable Object value) {
			this(key, ROOT_PATH, value);
		}

	}

}
