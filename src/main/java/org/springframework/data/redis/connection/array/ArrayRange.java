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
package org.springframework.data.redis.connection.array;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * An inclusive range of array indices identified by a {@code start} and an {@code end} index. Used by range-based array
 * commands such as {@literal ARDELRANGE}. Indices are zero-based and must not be negative; Redis rejects negative array
 * indices. Indices beyond the end of the array are allowed and simply match no elements.
 *
 * @author Yordan Tsintsov
 * @see <a href="https://redis.io/docs/latest/commands/ardelrange">Redis Documentation: ARDELRANGE</a>
 * @since 4.2
 */
public class ArrayRange {

	private final long start;
	private final long end;

	private ArrayRange(long start, long end) {
		Assert.isTrue(start >= 0, "Start must not be negative");
		Assert.isTrue(end >= 0, "End must not be negative");
		this.start = start;
		this.end = end;
	}

	/**
	 * Create a new {@link ArrayRange} spanning the indices from {@code start} to {@code end}, both inclusive. If
	 * {@code start} is greater than {@code end}, the range is processed in ascending index order regardless.
	 *
	 * @param start index of the first element in the range (inclusive).
	 * @param end index of the last element in the range (inclusive).
	 * @return the new {@link ArrayRange}.
	 */
	public static ArrayRange of(long start, long end) {
		return new ArrayRange(start, end);
	}

	/**
	 * @return the index of the first element in the range (inclusive).
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @return the index of the last element in the range (inclusive).
	 */
	public long getEnd() {
		return end;
	}

	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ArrayRange that = (ArrayRange) o;
		return start == that.start && end == that.end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@Override
	public String toString() {
		return "ArrayRange{start=" + start + ", end=" + end + '}';
	}

}
