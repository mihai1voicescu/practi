package core

import java.io.{DataInputStream, IOException, ObjectInputStream, ObjectOutputStream}
import java.net.{InetAddress, ServerSocket, Socket, SocketException}

import clock.clock
import invalidationlog.{Invalidation, InvalidationProcessor, Log}
import controller.ReqFile

import scala.collection.mutable

case class Controller(node: Node) extends Thread("ControlThread") {

  var log = new Log(node.logLocation)
  val processor = new InvalidationProcessor(log, this)
  val locationTable = new mutable.HashMap[String, NodeLocation]() //map(object id, peer)
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

  def requestBody(objectId: String): Unit = {
    if (locationTable.contains(objectId)) {
      val fileRequest = ReqFile(node.id, objectId, locationTable(objectId).)

      //todo send the file request
    } else {//Broadcast to neighbours

    }
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
          in.readObject() match {
            case invalidation: Invalidation => {
              println(this.socket.toString + " Received invalidation with timestamp: " + invalidation.timestamp)

              //process the invalidation
              processor.process(invalidation)
            }
            case reqFile: ReqFile =>
              if (locationTable.contains(reqFile.objectId)) {
                //Throw an exception, this should never happen
              } else {
                if (node.id == locationTable(reqFile.objectId).id) {
                  //This means this node currently has the file
                }
              }
              //1. check if the file is on this node
              //2. send reqfile to other nodes if needed
              //3. send body if needed
          }
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
