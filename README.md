# kotlin-beam-starter

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]

If you want to clone this repository to start your own project, you can choose the license you prefer and feel free to delete anything related to the license you are dropping.

### Built With
[![Kotlin][Kotlin]][Kotlin-url]
[![ApacheBeam][ApacheBeam]][ApacheBeam-url]
[![ApacheSpark][ApacheSpark]][ApacheSpark-url]
[![ApacheKafka][ApacheKafka]][ApacheKafka-url]
[![Gradle][Gradle]][Gradle-url]

## Getting Started
### Requirements
* Docker Engine [Installation Guide](https://docs.docker.com/engine/install/)
* Java OpenJDK 11+  [Installation Guide](https://openjdk.org/install/)

### Build
```sh
./gradlew clean build
```
### Source file structure
This project aims to have a multiple-module/pipeline, to achieve that, every pipeline should be something like this [sample-aggregation](/tree/main/sample-aggregation)
This project have a few files:

* The main `/src/main/kotlin`
  * [Pipeline.kt](https://github.com/arthurpessoa/kotlin-beam-starter/blob/main/sample-aggregation/src/main/kotlin/io/github/arthurpessoa/Pipeline.kt) Defines the main() and the pipeline definition
  * [PTransforms.kt](https://github.com/arthurpessoa/kotlin-beam-starter/blob/main/sample-aggregation/src/main/kotlin/io/github/arthurpessoa/PTransform.kt) Defines the PTransform functions
* The test `src/test/kotlin` defines the unit tests
* The test `src/integrationTest/kotlin` defines the integration tests
  
## FAQ
### How can i use another runner?
> To keep this template small, it only includes the [Direct Runner](https://beam.apache.org/documentation/runners/direct/) for runtime tests, and [Spark Runner](https://beam.apache.org/documentation/runners/spark/) as a integration test example.
> For a comparison of what each runner currently supports, look at the Beam [Capability Matrix](https://beam.apache.org/documentation/runners/capability-matrix/).
> To add a new runner, visit the runner's page for instructions on how to include it.

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/arthurpessoa/kotlin-beam-starter.svg
[contributors-url]: https://github.com/arthurpessoa/kotlin-beam-starter/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/arthurpessoa/kotlin-beam-starter.svg
[forks-url]: https://github.com/arthurpessoa/kotlin-beam-starter/network/members
[stars-shield]: https://img.shields.io/github/stars/arthurpessoa/kotlin-beam-starter.svg
[stars-url]: https://github.com/arthurpessoa/kotlin-beam-starter/stargazers
[issues-shield]: https://img.shields.io/github/issues/arthurpessoa/kotlin-beam-starter.svg
[issues-url]: https://github.com/arthurpessoa/kotlin-beam-starter/issues
[license-shield]: https://img.shields.io/github/license/arthurpessoa/kotlin-beam-starter.svg
[license-url]: https://github.com/arthurpessoa/kotlin-beam-starter/blob/master/LICENSE.MD
[Kotlin]: https://img.shields.io/badge/Kotlin-grey?style=for-the-badge&logo=kotlin
[Kotlin-url]: https://kotlinlang.org/
[ApacheBeam]: https://img.shields.io/badge/Apache%20Beam-grey?style=for-the-badge&logo=Apache
[ApacheBeam-url]: https://beam.apache.org/
[ApacheSpark]: https://img.shields.io/badge/Apache%20Spark-grey?style=for-the-badge&logo=Apache%20Spark
[ApacheSpark-url]: https://spark.apache.org/
[ApacheKafka]: https://img.shields.io/badge/Apache%20Kafka-grey?style=for-the-badge&logo=Apache%20Kafka
[ApacheKafka-url]: https://kafka.apache.org/
[Gradle]: https://img.shields.io/badge/Gradle-grey?style=for-the-badge&logo=Gradle
[Gradle-url]: https://gradle.org/
