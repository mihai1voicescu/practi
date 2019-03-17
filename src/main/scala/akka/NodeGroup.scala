package akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object NodeGroup {
  def props(groupId: Int): Props = Props(new NodeGroup(groupId))

  final case class AddNeighbour(groupId: Int, nodeId: Int)
  case object NeigbourAdded
}

class NodeGroup(groupId: Int) extends Actor with ActorLogging {

  import NodeGroup._

  var nodeIdToActor = Map.empty[Int, ActorRef]

  override def preStart(): Unit = log.info("NodeGroup {} started", groupId)

  override def postStop(): Unit = log.info("NodeGroup {} stopped", groupId)

  override def receive: Receive = {
    case trackMsg@AddNeighbour(`groupId`, _) ⇒
      nodeIdToActor.get(trackMsg.nodeId) match {
        case Some(nodeActor) ⇒
          nodeActor forward trackMsg
        case None ⇒
          log.info("Creating Node actor for {}", trackMsg.nodeId)
          val nodeActor = context.actorOf(Node.props(s"./node-files/${trackMsg.nodeId}/data", groupId, trackMsg.nodeId))
          nodeIdToActor += trackMsg.nodeId -> nodeActor
          nodeActor forward trackMsg
      }
  }
}
