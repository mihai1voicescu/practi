import java.net.{InetAddress, ServerSocket, Socket}

class Node(val port: Int, val root: String, val hostname :String = "localhost", logLocation: String) {

  val core = new Core(this, logLocation)
  new Thread(core).start()
  val controller = new Controller
  var neighbours: List[Address] = List()

  def setNeighbours(neighbours: List[Address]): Unit = {
    this.neighbours = neighbours
  }

  def sendToAllNeighbours(body: Body): Unit =
  {
    for (n <- neighbours) {
     sendBody(n, body)
    }
  }

  def sendBody(address: Address, body: Body): Unit = {
    core.sendBody(address, body)
  }

  def createBody(filePath: String): Body = Body(root, filePath)
}

