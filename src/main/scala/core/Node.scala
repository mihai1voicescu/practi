package core

import java.nio.file.{Files, Paths}
import java.util.logging.{ConsoleHandler, Level, Logger}

import invalidationlog.{Checkpoint, CheckpointItem}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Node {
  private val LOGGER = Logger.getLogger(core.Node.getClass.getName)
}

class Node(port: Int, val root: String, hostname: String = "localhost", id: Int, val logLocation: String) extends VirtualNode(id, hostname, port) {
  val core = new Core(this, logLocation)
  val controller = Controller(this)
  var neighbours: ListBuffer[VirtualNode] = ListBuffer()
  val checkpoint = new Checkpoint(mutable.HashMap[String, CheckpointItem]())

  new Thread(core).start()

  def logMessage(message: String, level: Level = null, logger: Logger = Node.LOGGER): Unit = {
    logger.log(if (level == null) Level.INFO else level, s"[ID:$id][$hostname]\t$message")
  }

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

  def sendToAllNeighbours(body: Body): Unit = {
    for (n <- neighbours) {
      sendBody(n, body)
    }
  }

  def sendBody(virtualNode: VirtualNode, body: Body): Unit = {
    core.sendBody(virtualNode, body)
  }

  def hasBody(filePath: String): Boolean = {
    val path = root + filePath
    Files.exists(Paths.get(path))
  }

  def createBody(filePath: String): Body = Body(root, filePath)

  def getVirtualNode(): VirtualNode = {
    new VirtualNode(id, hostname, port)
  }
}
