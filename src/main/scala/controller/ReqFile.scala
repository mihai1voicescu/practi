package controller

import java.net.Socket

import helper.socketHelper

case class ReqFile(requestNodeId: Int, objectId: String, @transient neighbour: Socket) extends Serializable {
  def send(): Unit = {
    socketHelper.send(neighbour, this)
  }
}
