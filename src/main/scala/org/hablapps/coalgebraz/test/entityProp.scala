package org.hablapps.coalgebraz.test

import scala.language.higherKinds

import Function.const

import org.hablapps.coalgebraz._

sealed trait EntityProp[B] {
  def andNext(nxt: Unit => EntityProp[B]): EntityProp[B] = this match {
    case AndNext(now, nxt2) => now andNext (_ => nxt2(()) andNext nxt)
    case now: Now[B] => AndNext(now, nxt)
  }
}
case class Now[B](f: B => Boolean) extends EntityProp[B]
case class AndNext[B](
  val now: Now[B],
  val nxt: Unit => EntityProp[B]) extends EntityProp[B]

object EntityProp {

  def now[B](f: B => Boolean): EntityProp[B] = Now(f)

  def ignore[B]: EntityProp[B] = now(const(true))

  def next[B](f: B => Boolean): EntityProp[B] =
    ignore andNext (_ => now(f))

  def always[B](f: B => Boolean): EntityProp[B] =
    Now(f) andNext (_ => always(f))

  def never[B](f: B => Boolean): EntityProp[B] = always(! f(_))
}
