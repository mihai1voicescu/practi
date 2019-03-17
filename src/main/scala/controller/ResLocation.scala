package controller

import core.{VirtualNode}
import helper.socketHelper

case class ResLocation(objectId: String, path: List[VirtualNode]) extends Serializable {
  def send(): Unit = {
    println(path.head + " sending info ")

    val socket = path.head.getControllerSocket
    val that = this
    val thread = new Thread() {
      override def run {
        socketHelper.send(socket, that)
      }
    }
    thread.start()
  }
}
