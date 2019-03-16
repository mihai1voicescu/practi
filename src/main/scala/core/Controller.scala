package core

import java.io._
import java.net.{InetAddress, ServerSocket, Socket, SocketException}

import clock.clock
import controller.{ReqFile, ResLocation}
import invalidationlog.{Invalidation, InvalidationProcessor, Log}

import scala.collection.mutable

case class Controller(node: Node) extends Thread("ControlThread") {


  var log = new Log(node.logLocation)
  val processor = new InvalidationProcessor(log, this)
  /**
    * Table of (object id, peer) describing which peer should be asked next if looking for corresponding object
    */
  val locationTable = new mutable.HashMap[String, VirtualNode]() //map(object id, peer)
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
    val currentVirtualNode = node.getVirtualNode()
    if (locationTable.contains(objectId)) {
      //It knows which neighbour to ask for the file
      val fileRequest = ReqFile(currentVirtualNode, currentVirtualNode, objectId, locationTable(objectId),
        List(locationTable(objectId), currentVirtualNode))
      fileRequest.send()
    } else {
      //It does not know which neighbour to ask; broadcast to all neighbours
      for (n <- node.neighbours) {
        val fileRequest = ReqFile(currentVirtualNode, currentVirtualNode, objectId,
          n, List(n, currentVirtualNode))
        fileRequest.send()
      }
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
              //1. check if the file is on this node
              //2. send reqfile to other nodes if needed
              //3. send body if needed

              //If the current node has the file but has not yet marked it in its table, do so now
              if (node.hasBody(reqFile.objectId) && !locationTable.contains(reqFile.objectId)) {
                locationTable.put(reqFile.objectId, node.getVirtualNode())
              }
              if (locationTable.contains(reqFile.objectId)) {
                if (node.id == locationTable(reqFile.objectId).id) {
                  //This means this node currently has the file
                  node.sendBody(reqFile.originator, node.createBody(reqFile.objectId))
                } else {
                  //The node does not have the file but one of the neighbours may have it
                  val fileRequest = ReqFile(reqFile.originator, node.getVirtualNode(), reqFile.objectId,
                    locationTable(reqFile.objectId), locationTable(reqFile.objectId) +: reqFile.path)
                  fileRequest.send()
                }
              } else {
                //Node does not know where the object is so ask all neighbours
                for (n <- node.neighbours) {
                  val fileRequest = ReqFile(reqFile.originator, node.getVirtualNode(), reqFile.objectId, n,
                    n +: reqFile.path)
                  fileRequest.send()
                }
              }
            case resLocation: ResLocation =>
              //The node gets information about where to find a file
              if (locationTable.contains(resLocation.objectId)) {
                locationTable.update(resLocation.objectId, resLocation.path.head)
              } else {
                locationTable.put(resLocation.objectId, resLocation.path.head)
              }
              if (resLocation.path.tail.nonEmpty) {
                val newResLocation = ResLocation(resLocation.objectId, resLocation.path.tail)
                newResLocation.send()
              }
            case _ =>
          }

          in.close()
        }
        catch {
          case e: SocketException =>
            () // avoid stack trace when stopping a client with Ctrl-C
          case e: IOException =>
            e.printStackTrace();
          case e: EOFException =>
            e.printStackTrace()
        }
      }
    }

  }

}
