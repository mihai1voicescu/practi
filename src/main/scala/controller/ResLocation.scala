package controller

import core.{VirtualNode}
import helper.socketHelper

case class ResLocation(objectId: String, path: List[VirtualNode]) extends Serializable {
  def send(): Unit = {
    println(path.head + " sending info ")

    path.head.sendToControllerAsync(this)
  }
}
