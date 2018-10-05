package user

import java.util.UUID

import io.simplesource.api.CommandAPI
import io.simplesource.data.{NonEmptyList, Result}
import model.Model._

object Handlers {
  def aggregator(a: Option[User])(e: UserEvent): Option[User] = e match {
    case UserEvent.Inserted(firstName, lastName) =>
      Option(User(firstName = firstName, lastName = lastName, yearOfBirth = 0))
    case UserEvent.NameUpdated(firstName, lastName) =>
      a.map(_.copy(firstName = firstName, lastName = lastName))
    case UserEvent.YearOfBirthUpdated(yob) => a.map(_.copy(yearOfBirth = yob))
    case UserEvent.Deleted()               => None
  }

  def commandHandler(k: UUID, a: Option[User])(c: UserCommand)
    : Result[CommandAPI.CommandError, NonEmptyList[UserEvent]] = c match {
    case UserCommand.Insert(firstName, lastName) =>
      Result.success(NonEmptyList.of(UserEvent.Inserted(firstName, lastName)))
    case UserCommand.UpdateName(firstName, lastName) =>
      Result.success(
        NonEmptyList.of(UserEvent.NameUpdated(firstName, lastName)))
    case UserCommand.UpdateYearOfBirth(yearOfBirth) =>
      Result.success(NonEmptyList.of(UserEvent.YearOfBirthUpdated(yearOfBirth)))
    case UserCommand.Delete() =>
      Result.success(NonEmptyList.of(UserEvent.Deleted()))
  }
}
