package invalidationlog

import java.io.{IOException, _}
import java.util.logging.{Level, Logger}

import core.glob
import helper.CheckpointSeeder

import scala.collection.mutable

object Checkpoint {
  private val LOGGER = Logger.getLogger(Checkpoint.getClass.getName)
}

/**
  * Class that logically represents a Checkpoint.
  * It uses @HashMap in order to store objects.
  *
  * @param items
  */
class Checkpoint(dir: String) extends Serializable {
  private val name = "checkpoint.txt"
  private var items = mutable.HashMap[String, CheckpointItem]()

  // when the object is created, initialize state restoration from file.
  {
    items = init()

    // Add a shutdown hook, so when the application is turned off, checkpoint state is dumped to file.
    sys.addShutdownHook({
      logMessage("System is shutting down, dumping checkpoint to a file.")
      val file = getCheckpointFile(dir)
      val oos = new ObjectOutputStream(new FileOutputStream(file.getPath))
      oos.writeObject(this)
      oos.close

      logMessage("Checkpoint dumped successfully.")

    })
  }

  /**
    * Method, that returns @CheckpointItem by object ID.
    *
    * @param objId
    * @return
    */
  def getById(objId: String): Option[CheckpointItem] = {
    items.contains(objId) match {
      case true => Some(items(objId))
      case _ => None
    }
  }

  /**
    * Updates the existing item in the buffer.
    *
    * @param newItem
    */
  def update(newItem: CheckpointItem): CheckpointItem = {
    items.contains(newItem.id) match {
      case false => insert(newItem)
      case _ => {
        items.update(newItem.id, newItem)
        return newItem
      }
    }
  }

  /**
    * Method that returns all items from @Checkpoint as @List of Tuples (String, Item)
    *
    * @return
    */
  def getAllItems(): List[(String, CheckpointItem)] = {
    items.toList
  }

  /**
    * Method that clears the checkpoint.
    */
  def clear(): Unit = {
    items = new mutable.HashMap[String, CheckpointItem]()
  }

  /**
    * Method that inserts item into checkpoint.
    *
    * @param item
    * @return
    */
  private def insert(item: CheckpointItem): CheckpointItem = {
    items += item.id -> item
    return item
  }

  /**
    * Function that initializes checkpoint from file
    *
    * @return
    */
  private def init(): mutable.HashMap[String, CheckpointItem] = {
    val file = getCheckpointFile(dir)
    try {
      val ois = new ObjectInputStream(new FileInputStream(dir + name))
      val inst = ois.readObject.asInstanceOf[Checkpoint]
      ois.close
      return inst.items
    } catch {
      case e: Exception => {
        logMessage("Error while reading checkpoint from file", Level.SEVERE)
        return new mutable.HashMap[String, CheckpointItem]()
      }
    }

  }

  private def getCheckpointFile(dir: String): File = {
    val file = new File(dir + name)

    if (!file.exists()) {
      file.getParentFile.mkdirs()
      file.createNewFile()
    }

    file
  }

  private def logMessage(message: String, level: Level = null, logger: Logger = Checkpoint.LOGGER): Unit = {
    logger.log(if (level == null) Level.INFO else level, s"[Checkpoint]\t$message")
  }
}