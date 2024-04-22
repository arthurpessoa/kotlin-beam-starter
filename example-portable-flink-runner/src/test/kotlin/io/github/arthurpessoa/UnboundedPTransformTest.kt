package io.github.arthurpessoa

import io.github.arthurpessoa.schema.Character
import org.apache.beam.sdk.schemas.SchemaRegistry
import org.apache.beam.sdk.testing.TestStream
import org.apache.beam.sdk.values.Row.withSchema
import org.apache.beam.sdk.values.TimestampedValue
import org.joda.time.Instant
import org.junit.jupiter.api.Test

class OutboundPTransformTest {


    @Test
    fun `should join`() {
        val schema = SchemaRegistry.createDefault().getSchema(Character::class.java)
        val instant = Instant.now()
        
        val testStreamBuilder = TestStream.create(schema).addElements(
            TimestampedValue.of(withSchema(schema).addValues(1L, "Luke Skywalker").build(), instant),
            TimestampedValue.of(withSchema(schema).addValues(2L, "Hermione Granger").build(), instant),
            TimestampedValue.of(withSchema(schema).addValues(3L, "Jack Sparrow").build(), instant)
        )
    }
}