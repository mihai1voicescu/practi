package controller

import core.VirtualNode
import helper.socketHelper

case class ReqFile(originator: VirtualNode, requestingNode: VirtualNode, objectId: String, receivingNode: VirtualNode,
                   var path: List[VirtualNode], requestId: Int) extends Serializable {

  path = List(originator)

  def send(): Unit = {
    println(requestingNode + " sending body request to " + receivingNode + " for " + objectId)
    receivingNode.sendToControllerAsync(this)
  }
}
