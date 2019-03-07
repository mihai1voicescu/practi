import java.net.{ServerSocket, Socket}

class Node(val port: Int, filesDirectory: String) {
  private val acceptSocket = new ServerSocket(port)

  val core = new Core(acceptSocket)
  core.run()
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

  def createBody(filePath: String): Body = Body(filesDirectory + "" + filePath)
}

