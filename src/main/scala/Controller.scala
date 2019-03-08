import java.io.{DataInputStream, File, IOException, ObjectInputStream}
import java.net.{InetAddress, ServerSocket, Socket, SocketException}

import helper.socketHelper
import invalidation._
import log.Log

case class Controller(node: Node) extends Runnable {

  var log = new Log(new File(node.logLocation))
  val processor = new InvalidationProcessor(log)

  //TODO change port to something controllable
  private val acceptSocket = new ServerSocket(node.port + 10, 50, InetAddress.getByName(node.hostname))

  /**
    * Controller method that is responsible for sending invalidations for all neighbors found in @node
    *
    * @param invalidation
    * @param node
    */
  def sendInvalidationForAllNeighbours(invalidation: Invalidation, node: Node): Unit = {
    node.neighbours.foreach(n => sendMessage(n, invalidation))

  }

  def requestBody(): Unit = {

  }

  private def sendMessage(address: Address, data: Invalidation): Unit = {
    socketHelper.send(new Socket(address.host, address.port), data)
  }

  override def run(): Unit = {
    while (true)
      InvalidationThread(acceptSocket.accept()).start()
  }

  /**
    * Class that handles invalidation stream.
    *
    * @param socket socket on witch invalidations are expected to arrive.
    */
  case class InvalidationThread(socket: Socket) extends Thread("InvalidationThread") {

    override def run(): Unit = {
      try {
        val ds = new DataInputStream(socket.getInputStream)
        val in = new ObjectInputStream(ds)


        val invalidation = in.readObject().asInstanceOf[Invalidation]
        println(this.socket.toString + " Received invalidation with timestamp: " + invalidation.timestamp)

        processor.process(invalidation)
      }
      catch {
        case e: SocketException =>
          () // avoid stack trace when stopping a client with Ctrl-C
        case e: IOException =>
          e.printStackTrace();
      }
    }

  }

}
