package akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Node {
  def props(root: String, groupId: Int, id: Int): Props = Props(new Node(root, groupId, id))
}

class Node(val root: String, groupId: Int, id: Int) extends Actor with ActorLogging {
  val controller: ActorRef = context.actorOf(Controller.props(id), s"node-${id}-controller")

  override def preStart(): Unit = log.info("Node {} started", id)

  override def postStop(): Unit = log.info("Node {} stopped", id)

  override def receive: Receive = {
    case NodeGroup.AddNeighbour(`groupId`, `id`) =>
      log.error("Node {} cannot be neighbour of itself", id)

    case NodeGroup.AddNeighbour(`groupId`, _) =>
      sender() ! NodeGroup.NeigbourAdded
  }

}
