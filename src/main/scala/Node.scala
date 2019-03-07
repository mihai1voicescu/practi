import java.io.File
import java.net.{InetAddress, ServerSocket, Socket}

class Node(val port: Int, val root: String, val hostname :String = "localhost", logLocation: String) {
  private val acceptSocket = new ServerSocket(port, 0, InetAddress.getByName(hostname))

  val core = new Core(acceptSocket, this, logLocation)
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
    println(this.acceptSocket.toString + " Sending body " + body.path)
    body.send(new Socket(address.host, address.port))
  }

  def createBody(filePath: String): Body = Body(root, filePath)
}

