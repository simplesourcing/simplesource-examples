package user

import java.util.UUID

import io.simplesource.api.CommandAPI.CommandError
import io.simplesource.kafka.dsl.{
  AggregateBuilder,
  AggregateSetBuilder,
  InvalidSequenceStrategy
}
import io.simplesource.kafka.internal.streams.PrefixResourceNamingStrategy
import io.circe.generic.auto._
import model.Model._
import org.slf4j.LoggerFactory

object App {
  import Handlers._
  private val logger = LoggerFactory.getLogger("App")

  def main(args: Array[String]): Unit = {
    val aggregateName = "user"
    val aggregateSet = new AggregateSetBuilder()
      .withKafkaConfig(
        builder =>
          builder
            .withKafkaApplicationId("ScalaUserRunner")
            .withKafkaBootstrap("localhost:9092")
            .withApplicationServer("localhost:1234")
            .build)
      .addAggregate(
        AggregateBuilder
          .newBuilder[UUID, UserCommand, UserEvent, Option[User]]()
          .withAggregator((a, e) => aggregator(a)(e))
          .withCommandHandler((k, a, c) => commandHandler(k, a)(c))
          .withSerdes(Json.aggregateSerdes)
          .withResourceNamingStrategy(
            new PrefixResourceNamingStrategy("example_scala_json_"))
          .withName(aggregateName)
          .withInitialValue(_ => None)
          .withCommandSequenceStrategy(InvalidSequenceStrategy.Strict)
          .build())
      .build
    val api = aggregateSet.getCommandAPI[UUID, UserCommand](aggregateName)
    logger.info("Started publishing commands")
    val result =
      Commands
        .submitCommands(api)
        .unsafePerform((e: Exception) => CommandError.InternalError)
    logger.info("Result of commands {}", result)
    logger.info("All commands published")
  }
}
