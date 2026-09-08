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
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.keyvalue.core.CriteriaAccessor;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.core.SpelSortAccessor;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.redis.repository.query.RedisOperationChain;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Redis JSON document specific {@link QueryEngine} implementation.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
class RedisDocumentQueryEngine extends QueryEngine<RedisDocumentKeyValueAdapter, RedisOperationChain, Comparator<?>> {

	/**
	 * Creates new {@link RedisDocumentQueryEngine} with defaults.
	 */
	RedisDocumentQueryEngine() {
		this(new RedisCriteriaAccessor(), new SpelSortAccessor(new SpelExpressionParser()));
	}

	/**
	 * Creates new {@link RedisQueryEngine}.
	 *
	 * @param criteriaAccessor
	 * @param sortAccessor
	 * @see QueryEngine#QueryEngine(CriteriaAccessor, SortAccessor)
	 */
	private RedisDocumentQueryEngine(CriteriaAccessor<RedisOperationChain> criteriaAccessor,
	                         @Nullable SortAccessor<Comparator<?>> sortAccessor) {
		super(criteriaAccessor, sortAccessor);
	}

	@Override
	public Collection<?> execute(@Nullable RedisOperationChain redisOperationChain, @Nullable Comparator<?> comparator, long offset, int rows, String keyspace) {
		return List.of();
	}

	@Override
	public long count(@Nullable RedisOperationChain redisOperationChain, String keyspace) {
		return 0;
	}

	/**
	 * @author Yordan Tsintsov
	 * @since 4.2
	 */
	static class RedisCriteriaAccessor implements CriteriaAccessor<RedisOperationChain> {

		@Override
		public @Nullable RedisOperationChain resolve(KeyValueQuery<?> query) {
			return (RedisOperationChain) query.getCriteria();
		}
	}

}
