package akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Controller {
  def props(nodeId: Int): Props = Props(new Controller(nodeId))
  final case class Invalidation(objId: String, var timestamp: Long, nodeId: Int)
  final case class RequestBody(objectId: String)
}

class Controller(nodeId: Int) extends Actor with ActorLogging {
  import Controller._
  val locationTable = new mutable.HashMap[String, ActorRef]()
  val processedRequests = new ListBuffer[Int]()

  override def receive: Receive = {
    case RequestBody(objectId) => {
      if (locationTable.contains(objectId)) {
        //It knows which neighbour to ask for the file
      }
    }
  }
}
