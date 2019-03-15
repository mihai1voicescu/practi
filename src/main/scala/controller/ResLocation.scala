package controller

import core.{VirtualNode}
import helper.socketHelper

case class ResLocation(objectId: String, path: List[VirtualNode]) extends Serializable {
  def send(): Unit = {
    socketHelper.send(path.head.getControllerSocket, this)
  }
}
