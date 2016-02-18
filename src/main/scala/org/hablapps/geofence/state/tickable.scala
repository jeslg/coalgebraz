package org.hablapps.geofence.state

trait Tickable[A] {
  def tick(a: A): A
  def total(a: A): Long
}

object Tickable {
  implicit val longTickable = new Tickable[Long] {
    def tick(a: Long) = a + 1
    def total(a: Long) = a
  }
}

class TickableOps[A](val a: A)(implicit T: Tickable[A]) {
  def tick: A = T.tick(a)
  def total: Long = T.total(a)
}

trait ToTickableOps {
  implicit def toTickableOps[A](a: A)(implicit P: Tickable[A]) =
    new TickableOps[A](a)(P)
}
