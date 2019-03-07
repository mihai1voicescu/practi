import java.net.{InetAddress, ServerSocket, Socket}

class Node(val port: Int, val root: String, val hostname :String = "localhost") {
  private val acceptSocket = new ServerSocket(port, 0, InetAddress.getByName(hostname))

  val core = new Core(acceptSocket, this)
  new Thread(core).start()
  val controller = new Controller
  var neighbours: List[Address] = List()

  def setNeighbours(neighbours: List[Address]): Unit = {
    this.neighbours = neighbours
  }

  def sendToAllNeighbours(body: Body): Unit =
  {
    for (n <- neighbours) {
      body.send(new Socket(n.host, n.port))
    }
  }

  def createBody(filePath: String): Body = Body(root, filePath)
}

