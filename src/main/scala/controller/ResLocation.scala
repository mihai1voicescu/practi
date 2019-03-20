package controller

import core.{VirtualNode}
import helper.socketHelper

case class ResLocation(objectId: String, path: List[VirtualNode], originator: VirtualNode, requestId: Int) extends Serializable {
  def send(): Unit = {
    println(path.head + " receiving info: " + objectId + " is at location " + originator)

    path.head.sendToControllerAsync(this)
  }
}
