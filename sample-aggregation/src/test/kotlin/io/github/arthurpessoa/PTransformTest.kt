package io.github.arthurpessoa

import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.testing.PAssert
import org.apache.beam.sdk.testing.TestPipeline
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.values.PCollection
import org.junit.jupiter.api.Test


class PTransformTest {

    @Test
    fun `should convert a csv string to mySchema`() {

        val testPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(true)

        val characters: PCollection<String> = testPipeline.apply(
            Create.of(
                listOf(
                    "1,Luke Skywalker",
                    "2,Hermione Granger",
                    "3,Jack Sparrow"
                )
            ).withCoder(StringUtf8Coder.of())
        )

        val result = characters.apply(convertToMovieCharacter())

        PAssert.that(result).containsInAnyOrder(
            MovieCharacter(1L, "Luke Skywalker"),
            MovieCharacter(2L, "Hermione Granger"),
            MovieCharacter(3L, "Jack Sparrow")
        )

        testPipeline.run()
    }
}
