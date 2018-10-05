package stephenzoio.freesourcing.data

import java.util.UUID

import com.sksamuel.avro4s.{Record, RecordFormat}
import model.Model.{Key, User, UserCommand}
import org.scalatest.{Matchers, WordSpec}

class AvroTests extends WordSpec with Matchers {
  "record format" must {
    "serialise and deserialise Strings" ignore {
      val recordFormat = RecordFormat[String]
      val s = "TestString"
      val record: Record = recordFormat.to(s)
      val sAfter = recordFormat.from(record)
      sAfter shouldBe s
    }

    "serialise and deserialise uuids" ignore {
      val recordFormat = RecordFormat[UUID]
      val uuid = UUID.randomUUID()
      val record: Record = recordFormat.to(uuid)
      val uuidAfter = recordFormat.from(record)
      uuidAfter shouldBe uuid
    }

    "serialise and deserialise uuids in case class" in {
      val recordFormat = RecordFormat[Key[UUID]]
      val key = Key(UUID.randomUUID())
      val record: Record = recordFormat.to(key)
      val keyAfter = recordFormat.from(record)
      keyAfter shouldBe key
    }

    "serialise and deserialise users" in {
      val user = User("Firstname", "Lastname", 1999)
      val recordFormat = RecordFormat[User]
      val record: Record = recordFormat.to(user)
      val userAfter = recordFormat.from(record)
      userAfter shouldBe user
    }

    "serialise and deserialise user commands" ignore {
      val insertUser = UserCommand.Insert("Firstname", "Lastname")
      val recordFormat = RecordFormat[UserCommand]
      val record: Record = recordFormat.to(insertUser)
      val insertUserAfter = recordFormat.from(record)
      insertUserAfter shouldBe insertUser
    }
  }
}
