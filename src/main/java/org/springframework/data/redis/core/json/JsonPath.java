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
package org.springframework.data.redis.core.json;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JSON path.
 *
 * @author Yordan Tsintsov
 * @since 4.3
 */
@NullMarked
public final class JsonPath {

	private static final String ROOT_PATH = "$";
	private static final JsonPath ROOT = new JsonPath(ROOT_PATH);

	private final String path;

	private JsonPath(String path) {
		this.path = path;
	}

	public static JsonPath root() {
		return ROOT;
	}

	public static JsonPath of(String path) {

		Assert.hasText(path, "Path must not be empty");

		return new JsonPath(path);
	}

	public JsonPath child(String child) {

		Assert.hasText(child, "Child must not be empty");

		return new JsonPath(path + "." + child);
	}

	public JsonPath index(int index) {
		return new JsonPath(path + "[" + index + "]");
	}

	public JsonPath recursive() {
		return new JsonPath(path + "..");
	}

	public JsonPath wildcard() {
		return new JsonPath(path + ".*");
	}

	public JsonPath allElements() {
		return new JsonPath(path + "[*]");
	}

	public JsonPath slice(SliceExpression expr) {
		return new JsonPath(path + expr);
	}

	public JsonPath indices(int... indices) {

		Assert.isTrue(indices.length > 0, "Indices must not be empty");

		String joined = Arrays.stream(indices)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining(","));

		return new JsonPath(path + "[" + joined + "]");
	}

	public JsonPath children(String... names) {

		Assert.notEmpty(names, "Names must not be empty");

		String joined = Arrays.stream(names)
				.map(n -> "'" + n + "'")
				.collect(Collectors.joining(","));

		return new JsonPath(path + "[" + joined + "]");
	}

	public String getPath() {
		return path;
	}

	public JsonPath filter(FilterExpression filter) {
		return new JsonPath(path + filter);
	}

	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonPath jsonPath = (JsonPath) o;
		return Objects.equals(path, jsonPath.path);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(path);
	}

	@Override
	public String toString() {
		return "JsonPath{path='" + path + '\'' + '}';
	}

	public static final class SliceExpression {

		private final @Nullable Integer start;
		private final @Nullable Integer end;
		private final @Nullable Integer step;

		private SliceExpression(@Nullable Integer start, @Nullable Integer end, @Nullable Integer step) {

			this.start = start;
			this.end = end;
			this.step = step;
		}

		public static SliceExpression slice() {
			return new SliceExpression(null, null, null);
		}

		public SliceExpression withStart(@Nullable Integer start) {
			return new SliceExpression(start, end, step);
		}

		public SliceExpression withEnd(@Nullable Integer end) {
			return new SliceExpression(start, end, step);
		}

		public SliceExpression withStep(@Nullable Integer step) {

			Assert.isTrue(step == null || step != 0, "Step must not be zero");

			return new SliceExpression(start, end, step);
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder("[");
			if (start != null) {
				sb.append(start);
			}
			sb.append(":");
			if (end != null) {
				sb.append(end);
			}
			if (step != null) {
				sb.append(":").append(step);
			}
			sb.append("]");
			return sb.toString();
		}

	}

	public static final class FilterExpression {

		private final String expression;

		private FilterExpression(String expression) {
			this.expression = expression;
		}

		public static FilterExpression of(String expression) {

			Assert.hasText(expression, "Expression must not be empty");

			return new FilterExpression(expression);
		}

		@Override
		public String toString() {
			return "[?(" + expression + ")]";
		}

	}

}
