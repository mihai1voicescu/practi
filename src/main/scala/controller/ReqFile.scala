package controller

import java.net.Socket

import core.NodeLocation
import helper.socketHelper

case class ReqFile(requestingNode: NodeLocation, objectId: String, receivingNode: NodeLocation, @transient neighbour: Socket) extends Serializable {
  def send(): Unit = {
    socketHelper.send(neighbour, this)
  }
}
