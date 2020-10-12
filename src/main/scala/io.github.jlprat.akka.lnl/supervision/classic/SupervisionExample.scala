package io.github.jlprat.akka.lnl.supervision.classic

import akka.actor.Actor
import akka.actor.Props
import io.github.jlprat.akka.lnl.supervision.classic.SupervisionExample.Child._


object SupervisionExample {

  object Child {

    sealed trait Command
    case class Save(tag: String, value: Int) extends Command
    case class Retrieve(key: Key)            extends Command

    case class Key(id: String)
    case class Stored(product: Product)

    case class Product(hash: String, tag: String, value: Int)

    def props(): Props = Props(classOf[Child])

  }

  class Child extends Actor {

    var storage: Map[String, Product] = Map.empty

    override def receive: Actor.Receive = {
      case Save(tag, value) =>
        val hash = s"${tag.hashCode()}"
        storage = storage.updated(hash, Product(hash, tag, value))
        sender() ! Key(hash)
      case Retrieve(key) if !storage.contains(key.id) =>
        throw new IllegalStateException("No such key!")
      case Retrieve(key) =>
        sender() ! Stored(storage(key.id))
    }
  }

  object Parent {

  }

  class Parent extends Actor {

    override def receive: Actor.Receive = ???


  }

}
