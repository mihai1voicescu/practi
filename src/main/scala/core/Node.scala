package core

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.logging.{Level, Logger}

import com.sun.javaws.exceptions.InvalidArgumentException
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

  // Initialize required thingies
  {
    seedCheckpoint()
    new Thread(core).start()
  }

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

  def hasValidBody(filePath: String): Boolean = {
    val a = this.checkpoint.getById(filePath)
    a match {
      case Some(checkpointItem) => {
        return !checkpointItem.invalid
      }
    }
    false
  }

  def createBody(filePath: String): Body = Body(root, filePath)

  def getVirtualNode(): VirtualNode = {
    new VirtualNode(id, hostname, port)
  }

  /**
    * Function that seeds checkpoint from files found in root dir.
    */
  def seedCheckpoint(): Unit = {
    // get File paths in root recursively, remove directories
    val files = getFilesR(new File(root)).filter(!_.isDirectory)

    // Strip object Ids (remove root from path)
    files.foreach(f => {
      extractId(f.getPath) match {
        case Some(bodyId) => {
          // Create and insert body to checkpoint
          val bod = createBody(bodyId)
          val chkIt = new CheckpointItem(f.getPath, bod, false, clock.clock.time)
          checkpoint.update(chkIt)
        }
        case None => throw new InvalidArgumentException(Array("Could not extract file id from File path"))
      }
    })
  }

  /**
    * Function that extracts root dir from file path.
    *
    * @param path relative path of a file
    * @return object ID
    */
  private def extractId(path: String): Option[String] = {
    path match {
      case s if s.startsWith(Paths.get(root).toString) => Some(s.stripPrefix(Paths.get(root).toString))
      case _ => None
    }
  }

  /**
    * Recursive helper function, that traverses all files given in a path
    *
    * @param f root file, from where recursive file search is executed
    * @return
    */
  private def getFilesR(f: File): Array[File] = {
    val mbFile = Option(f.listFiles)
    var these = Array[File]()

    mbFile match {
      case Some(f) => these = f ++ f.filter(_.isDirectory).flatMap(getFilesR)
      case _ => // do nuffin if null
    }

    return these
  }
}
