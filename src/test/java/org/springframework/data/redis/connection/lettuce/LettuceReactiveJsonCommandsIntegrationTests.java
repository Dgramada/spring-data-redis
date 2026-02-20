package org.springframework.data.redis.connection.lettuce;

import io.lettuce.core.json.JsonPath;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisJsonCommands;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link LettuceReactiveJsonCommands}
 *
 * @author Yordan Tsintsov
 */
@ParameterizedClass
class LettuceReactiveJsonCommandsIntegrationTests extends LettuceReactiveCommandsTestSupport {

	private static final String JSON_VALUE = """
			{
				"name": "John",
				"lastname": "Doe",
				"age": 30,
				"address": {
					"street": "123 Main St",
					"city": "New York",
					"state": "NY"
				}
			}
			""";

	public LettuceReactiveJsonCommandsIntegrationTests(Fixture fixture) {
		super(fixture);
	}

	@Test
	void jsonClearShouldOperateCorrectly() {

		JsonPath path = JsonPath.of(RedisJsonCommands.ROOT_PATH);

		nativeCommands.jsonSet(KEY_1, path, JSON_VALUE);

		connection.jsonCommands().jsonClear(KEY_1_BBUFFER)
				.as(StepVerifier::create)
				.expectNext(2L)
				.verifyComplete();
	}

}
