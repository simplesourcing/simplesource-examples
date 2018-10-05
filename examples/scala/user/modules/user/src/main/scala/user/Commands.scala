package user

import java.time.Duration
import java.util.UUID

import io.simplesource.api.CommandAPI
import io.simplesource.data.{FutureResult, NonEmptyList, Sequence}
import org.slf4j.LoggerFactory
import model.Model._

object Commands {
  private val logger = LoggerFactory.getLogger("UserCommandExample")

  def submitCommands(commandAPI: CommandAPI[UUID, UserCommand])
    : FutureResult[CommandAPI.CommandError, NonEmptyList[Sequence]] = {
    val key = UUID.randomUUID()
    val firstName = "Sarah"
    val lastName = "Dubois"
    commandAPI
      .publishAndQueryCommand(new CommandAPI.Request[UUID, UserCommand](
                                key,
                                Sequence.first,
                                UUID.randomUUID,
                                UserCommand.Insert(firstName, lastName)),
                              Duration.ofMinutes(2L))
      .flatMap((sequences: NonEmptyList[Sequence]) => {
        def foo(sequences: NonEmptyList[Sequence]) = {
          logger.info("Received result {} new sequences", sequences)
          commandAPI.publishAndQueryCommand(
            new CommandAPI.Request[UUID, UserCommand](
              key,
              sequences.last,
              UUID.randomUUID,
              UserCommand.UpdateName("Sarah J.", "Dubowski")),
            Duration.ofMinutes(2L))
        }

        foo(sequences)
      })
  }
}
