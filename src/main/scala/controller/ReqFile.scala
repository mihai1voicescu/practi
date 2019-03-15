package controller

import core.VirtualNode
import helper.socketHelper

case class ReqFile(originator: VirtualNode, requestingNode: VirtualNode, objectId: String, receivingNode: VirtualNode,
                   var path: List[VirtualNode]) extends Serializable {
  path = List(originator)

  def send(): Unit = {
    socketHelper.send(receivingNode.getControllerSocket, this)
  }
}
