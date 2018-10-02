package model

object Model {
  final case class User(firstName: String, lastName: String, yearOfBirth: Int)

  sealed trait UserCommand

  object UserCommand {
    final case class Insert(firstName: String, lastName: String)
        extends UserCommand
    final case class UpdateName(firstName: String, lastName: String)
        extends UserCommand
    final case class UpdateYearOfBirth(yearOfBirth: Int) extends UserCommand
    final case class Delete() extends UserCommand
  }

  sealed trait UserEvent
  object UserEvent {
    final case class Inserted(firstName: String, lastName: String)
        extends UserEvent
    final case class NameUpdated(firstName: String, lastName: String)
        extends UserEvent
    final case class YearOfBirthUpdated(yearOfBirth: Int) extends UserEvent
    final case class Deleted() extends UserEvent
  }

  final case class Key[A](a: A)
}
