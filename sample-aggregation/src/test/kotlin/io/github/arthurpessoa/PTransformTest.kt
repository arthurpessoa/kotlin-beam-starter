package io.github.arthurpessoa

import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.schemas.transforms.Join
import org.apache.beam.sdk.testing.PAssert
import org.apache.beam.sdk.testing.TestPipeline
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.values.PCollection
import org.junit.jupiter.api.Test


class PTransformTest {

    @Test
    fun `should convert a csv string to Characters`() {

        val testPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(true)

        val characters: PCollection<Character> = testPipeline.apply(
            Create.of(
                listOf(
                    "1,Luke Skywalker",
                    "2,Hermione Granger",
                    "3,Jack Sparrow"
                )
            ).withCoder(StringUtf8Coder.of())
        ).apply(mapStringIntoCharacter())


        PAssert.that(characters).containsInAnyOrder(
            Character(1L, "Luke Skywalker"),
            Character(2L, "Hermione Granger"),
            Character(3L, "Jack Sparrow")
        )

        testPipeline.run()
    }


    @Test
    fun `should join`() {

        val testPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(true)

        val characters: PCollection<Character> = testPipeline
            .apply(
                "create characters",
                Create.of(
                    listOf(
                        "1,Luke Skywalker",
                        "2,Hermione Granger",
                        "3,Jack Sparrow"
                    )
                ).withCoder(StringUtf8Coder.of())
            ).apply("convert to character", mapStringIntoCharacter())


        val movies: PCollection<Movie> = testPipeline
            .apply(
                "create movies",
                Create.of(
                    listOf(
                        "1,Star Wars",
                        "2,Harry Potter",
                        "3,Pirates of the Caribean"
                    )
                ).withCoder(StringUtf8Coder.of())
            ).apply("convert to movie", mapStringIntoMovie())


        characters.apply(
            "join",
            Join.innerJoin<Character, Movie>(movies).using("id")
        ).apply(
            "map into schema",
            mapJoinedRowIntoMovieCharacter()
        )


        PAssert.that(characters).containsInAnyOrder(
            Character(1L, "Luke Skywalker"),
            Character(2L, "Hermione Granger"),
            Character(3L, "Jack Sparrow")
        )

        testPipeline.run()
    }
}
