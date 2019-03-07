case class Controller() {
  def sendInvalidation(objectId: String, node: Node): Unit = {
    node.neighbours.foreach(n => sendMessage(n))

  }

  private def sendMessage(address: Address): Unit = {

  }
}
