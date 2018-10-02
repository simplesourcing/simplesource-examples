package user

import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.simplesource.api.CommandAPI.CommandError
import io.simplesource.data.{NonEmptyList, Reason, Result, Sequence}
import io.simplesource.kafka.api.AggregateSerdes
import io.simplesource.kafka.model._
import io.simplesource.kafka.serialization.util.GenericSerde
import org.apache.kafka.common.serialization.{Serde, Serdes => KafkaSerdes}

import collection.JavaConverters._

object Json {

  private implicit class CodecPairOps[B](codec: (Encoder[B], Decoder[B])) {
    def asSerde: Serde[B] = {
      implicit val encoder = codec._1
      implicit val decoder = codec._2
      serdeFromCodecs
    }
  }

  private def serdeFromCodecs[B](implicit e: Encoder[B],
                                 d: Decoder[B]): Serde[B] = {
    import io.circe.parser._
    import io.circe.syntax._
    def toB: java.util.function.Function[String, B] =
      parse(_).flatMap(j => j.as[B]).fold(throw _, identity)
    def fromB: java.util.function.Function[B, String] = _.asJson.noSpaces

    GenericSerde.of[B, String](KafkaSerdes.String(), fromB, toB)
  }

  private def productCodecs2[A0: Encoder: Decoder, A1: Encoder: Decoder, B](
      n0: String,
      n1: String)(b2p: B => (A0, A1),
                  p2b: (A0, A1) => B): (Encoder[B], Decoder[B]) = {
    val encoder: Encoder[B] = Encoder.forProduct2(n0, n1)(b2p)
    val decoder: Decoder[B] =
      Decoder.forProduct2(n0, n1)((a0: A0, a1: A1) => p2b(a0, a1))

    (encoder, decoder)
  }

  private def productCodecs3[A0: Encoder: Decoder,
                             A1: Encoder: Decoder,
                             A2: Encoder: Decoder,
                             B](n0: String, n1: String, n2: String)(
      b2p: B => (A0, A1, A2),
      p2b: (A0, A1, A2) => B): (Encoder[B], Decoder[B]) = {
    implicit val encoder: Encoder[B] = Encoder.forProduct3(n0, n1, n2)(b2p)
    implicit val decoder: Decoder[B] =
      Decoder.forProduct3(n0, n1, n2)((a0: A0, a1: A1, a2: A2) =>
        p2b(a0, a1, a2))

    (encoder, decoder)
  }

  def aggregateSerdes[K: Encoder: Decoder,
                      C: Encoder: Decoder,
                      E: Encoder: Decoder,
                      A: Encoder: Decoder]: AggregateSerdes[K, C, E, A] =
    new AggregateSerdes[K, C, E, A] {

      val aks = serdeFromCodecs[K]

      val crs = productCodecs3[C, Long, UUID, CommandRequest[C]]("command",
                                                                 "readSequence",
                                                                 "commandId")(
        v => (v.command(), v.readSequence().getSeq, v.commandId()),
        (v, rs, id) => new CommandRequest(v, Sequence.position(rs), id)
      ).asSerde

      val crks = serdeFromCodecs[UUID]

      val vwss =
        productCodecs2[E, Long, ValueWithSequence[E]]("value", "sequence")(
          v => (v.value(), v.sequence().getSeq),
          (v, s) => new ValueWithSequence(v, Sequence.position(s))
        ).asSerde

      val au =
        productCodecs2[A, Long, AggregateUpdate[A]]("aggregate", "sequence")(
          v => (v.aggregate(), v.sequence().getSeq),
          (v, s) => new AggregateUpdate(v, Sequence.position(s))
        )

      val aus = au.asSerde

      val ur = {
        implicit val cee: Encoder[Reason[CommandError]] =
          implicitly[Encoder[String]]
            .contramap[CommandError](c => c.toString)
            .contramap[Reason[CommandError]](_.getError)
        implicit val ced: Decoder[Reason[CommandError]] =
          implicitly[Decoder[String]]
            .map[CommandError](s => CommandError.valueOf(s))
            .map(e => Reason.of(e, e.toString))

        implicit val nele: Encoder[NonEmptyList[Reason[CommandError]]] =
          io.circe.generic.semiauto
            .deriveEncoder[List[Reason[CommandError]]]
            .contramapObject(nel => nel.head() :: nel.tail().asScala.toList)
        implicit val neld: Decoder[NonEmptyList[Reason[CommandError]]] =
          io.circe.generic.semiauto
            .deriveDecoder[List[Reason[CommandError]]]
            .map(l => NonEmptyList.fromList(l.asJava))

        implicit val aue = au._1
        implicit val aud = au._2

        type ErrorOrUpdate =
          Either[NonEmptyList[Reason[CommandError]], AggregateUpdate[A]]

        implicit def rese: Encoder[Result[CommandError, AggregateUpdate[A]]] =
          io.circe.generic.semiauto
            .deriveEncoder[ErrorOrUpdate]
            .contramapObject(r => {
              if (r.isSuccess)
                Right[NonEmptyList[Reason[CommandError]], AggregateUpdate[A]](
                  r.getOrElse(null))
              else
                Left[NonEmptyList[Reason[CommandError]], AggregateUpdate[A]](
                  r.failureReasons().get())
            })
        implicit def resd: Decoder[Result[CommandError, AggregateUpdate[A]]] =
          io.circe.generic.semiauto
            .deriveDecoder[ErrorOrUpdate]
            .map({
              case Right(r) =>
                Result.success[CommandError, AggregateUpdate[A]](r)
              case Left(e) =>
                Result.failure[CommandError, AggregateUpdate[A]](e)
            })

        productCodecs3[UUID,
                       Long,
                       Result[CommandError, AggregateUpdate[A]],
                       AggregateUpdateResult[A]]("commandId",
                                                 "readSequence",
                                                 "updatedAggregateResult")(
          x =>
            (x.commandId(),
             x.readSequence().getSeq,
             x.updatedAggregateResult()),
          (id, seq, ur) =>
            new AggregateUpdateResult(id, Sequence.position(seq), ur))
      }.asSerde

      override def aggregateKey(): Serde[K] = aks
      override def commandRequest(): Serde[CommandRequest[C]] = crs
      override def commandResponseKey(): Serde[UUID] = crks
      override def valueWithSequence(): Serde[ValueWithSequence[E]] = vwss
      override def aggregateUpdate(): Serde[AggregateUpdate[A]] = aus
      override def updateResult(): Serde[AggregateUpdateResult[A]] = ur
    }
}
