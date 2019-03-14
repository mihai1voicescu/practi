package core

import java.io.{DataInputStream, IOException, ObjectInputStream, ObjectOutputStream}
import java.net.{InetAddress, ServerSocket, Socket, SocketException}

import clock.clock
import invalidationlog.{Invalidation, InvalidationProcessor, Log}

import scala.collection.mutable

case class Controller(node: Node) extends Thread("ControlThread") {

  var log = new Log(node.logLocation)
  val processor = new InvalidationProcessor(log, this)
  val peerControllers = new mutable.HashMap[Int, Socket]()

  private val acceptSocket = new ServerSocket(node.getControllerPort, 50, InetAddress.getByName(node.hostname))
  this.start()

  /**
    * Controller method that is responsible for sending invalidations for all neighbors found in @node
    *
    * @param invalidation
    */
  def sendInvalidationForAllNeighbours(invalidation: Invalidation): Unit = {
    // stamp the operation
    invalidation.sendStamp()
    peerControllers.foreach { case (_, socket)
    =>
      new ObjectOutputStream(socket.getOutputStream).writeObject(invalidation)
      node.getControllerSocket.getOutputStream.flush()
    }
  }

  // TODO implement requesting a body
  def requestBody(): Unit = {

  }

  def connectToNodeController(virtualNode: VirtualNode): Unit = {
    peerControllers(virtualNode.id) = virtualNode.getControllerSocket
  }

  def invalidate(objectId: String): Unit = {
    val invalidation = Invalidation(objectId, clock.time, node.id)

    sendInvalidationForAllNeighbours(invalidation)
  }

  override def run(): Unit = {
    while (true)
      InvalidationThread(acceptSocket.accept()).start()
  }

  /**
    * Class that handles invalidation stream. Each invalidation is handled in @InvalidationProcessor.
    *
    * @param socket socket on witch invalidations are expected to arrive.
    */
  case class InvalidationThread(socket: Socket) extends Thread("InvalidationThread") {

    override def run(): Unit = {
      val ds = new DataInputStream(socket.getInputStream)

      // controllers need to be continuously connected to each other awaiting invalidations
      while (true) {

        try {
          // reset the Object input stream each time
          val in = new ObjectInputStream(ds)

          // block until invalidations actually come
          val invalidation = in.readObject().asInstanceOf[Invalidation]
          println(this.socket.toString + " Received invalidation with timestamp: " + invalidation.timestamp)

          //process the invalidation
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

}
