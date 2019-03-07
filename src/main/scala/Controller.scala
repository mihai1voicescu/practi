import java.io.ObjectOutputStream
import java.net.Socket

import helper.socketHelper

case class Controller() {

  /**
    * Controller method that is responsible for sending invalidations for all neighbors found in @node
    *
    * @param invalidation
    * @param node
    */
  def sendInvalidationForAllNeighbours(invalidation: Invalidation, node: Node): Unit = {
    node.neighbours.foreach(n => sendMessage(n, invalidation))

  }

  private def sendMessage(address: Address, data: Invalidation): Unit = {
    socketHelper.send(new Socket(address.host, address.port), data)
  }
}
