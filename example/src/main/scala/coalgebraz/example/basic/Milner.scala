package coalgebraz
package example.basic

import scala.collection.immutable.{ Stream => LazyList }

import scalaz._, Scalaz._

import Coalgebraz._

object Milner extends App {

  // CS =def= _pub_._coin_.coffee.CS
  val cs: Milner[Channel, CSState] = milner {
    case CS0 => LazyList(pub.right -> CS1)
    case CS1 => LazyList(coin.right -> CS2)
    case CS2 => LazyList(coffee.left -> CS0)
  }

  // runMilnerIO(cs)(CS0)

  // CM =def= coin._coffee_.CM
  val cm: Milner[Channel, CMState] = milner {
    case CM0 => LazyList(coin.left -> CM1)
    case CM1 => LazyList(coffee.right -> CM0)
  }

  // runMilnerIO(cm)(CM0)

  // CTM =def= coin.(_coffee_.CTM + _tea_.CTM)
  val ctm: Milner[Channel, CTMState] = milner {
    case CTM0 => LazyList(coin.left -> CTM1)
    case CTM1 => LazyList(coffee.right -> CTM0, tea.right -> CTM0)
  }

  // runMilnerIO(ctm)(CTM0)

  // CS | CM
  val cs_cm: Milner[Channel \/ Channel, (CSState, CMState)] = cs | cm

  runMilnerIO(cs_cm)(
    (CS0, CM0),
    l => println(s"⇒ <| ${ l.fold(_.toString, "_" + _ + "_") }"),
    r => println(s"⇒ |> ${ r.fold(_.toString, "_" + _ + "_") }"))

  trait CSState
  case object CS0 extends CSState
  case object CS1 extends CSState
  case object CS2 extends CSState

  trait CMState
  case object CM0 extends CMState
  case object CM1 extends CMState

  trait CTMState
  case object CTM0 extends CTMState
  case object CTM1 extends CTMState

  trait Channel
  case object pub extends Channel
  case object coin extends Channel
  case object coffee extends Channel
  case object tea extends Channel

  object Channel {
    implicit val readChannel: Read[Channel \/ Channel] =
      new Read[Channel \/ Channel] {
        def read(s: String): Option[Channel \/ Channel] = s match {
          case "pub" => pub.left.some
          case "coin" => coin.left.some
          case "coffee" => coffee.left.some
          case "tea" => tea.left.some
          case "_pub_" => pub.right.some
          case "_coin_" => coin.right.some
          case "_coffee_" => coffee.right.some
          case "_tea_" => tea.right.some
          case _ => Option.empty
        }
      }

    implicit val readChannel2: Read[(Channel \/ Channel) \/ (Channel \/ Channel)] =
      new Read[(Channel \/ Channel) \/ (Channel \/ Channel)] {
        val left  = """<\| (.*)""".r
        val right = """\|> (.*)""".r
        def read(s: String): Option[(Channel \/ Channel) \/ (Channel \/ Channel)] = s match {
          case left(act)  => readChannel.read(act).map(_.left)
          case right(act) => readChannel.read(act).map(_.right)
          case _ => Option.empty
        }
      }
  }
}
