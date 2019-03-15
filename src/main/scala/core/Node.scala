package core

import scala.collection.mutable.ListBuffer

class Node(port: Int, val root: String, hostname :String = "localhost", id : Int, val logLocation: String) extends VirtualNode(id, hostname, port) {
  val core = new Core(this, logLocation)
  new Thread(core).start()
  val controller = Controller(this)
  var neighbours: ListBuffer[VirtualNode] = ListBuffer()

  def addNeighbours(neighbours: List[VirtualNode]): Unit = {
    this.neighbours ++= neighbours

    for (n <- neighbours)
      controller.connectToNodeController(n)
  }

  def addNeighbour(neighbour: VirtualNode): Unit = {
    this.neighbours += neighbour
    controller.connectToNodeController(neighbour)
  }

  def invalidate(objectId: String): Unit = {
    controller.invalidate(objectId)
  }

  def sendToAllNeighbours(body: Body): Unit =
  {
    for (n <- neighbours) {
     sendBody(n, body)
    }
  }

  def sendBody(virtualNode: VirtualNode, body: Body): Unit = {
    core.sendBody(virtualNode, body)
  }

  def createBody(filePath: String): Body = Body(root, filePath)
}
