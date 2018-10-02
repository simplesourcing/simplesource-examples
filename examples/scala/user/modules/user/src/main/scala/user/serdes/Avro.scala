package user.serdes

import java.util.UUID

import com.sksamuel.avro4s.RecordFormat
import io.simplesource.kafka.serialization.avro.AvroAggregateSerdes
import io.simplesource.kafka.serialization.util.GenericMapper
import model.Model.{Key, User, UserCommand, UserEvent}
import org.apache.avro.generic.GenericRecord

object Avro {
  implicit class RecordFormatOps[A](recordFormat: RecordFormat[A]) {
    def genericMapper: GenericMapper[A, GenericRecord] =
      new GenericMapper[A, GenericRecord] {
        override def toGeneric(v: A): GenericRecord = recordFormat.to(v)
        override def fromGeneric(g: GenericRecord): A = recordFormat.from(g)
      }
  }

  implicit class GenericMapperOps[A](gm: GenericMapper[Key[A], GenericRecord]) {
    def unwrap: GenericMapper[A, GenericRecord] =
      new GenericMapper[A, GenericRecord] {
        override def toGeneric(a: A): GenericRecord = gm.toGeneric(Key(a))
        override def fromGeneric(g: GenericRecord): A = gm.fromGeneric(g).a
      }
  }

  val userAggregate =
    new AvroAggregateSerdes(
      RecordFormat[Option[User]].genericMapper,
      RecordFormat[Key[UserEvent]].genericMapper.unwrap,
      RecordFormat[Key[UserCommand]].genericMapper.unwrap,
      RecordFormat[Key[UUID]].genericMapper.unwrap,
      "http://localhost:8081",
      false,
      null
    )
}
