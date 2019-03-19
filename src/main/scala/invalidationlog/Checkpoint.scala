package invalidationlog

import scala.collection.mutable

/**
  * Class that logically represents a Checkpoint.
  *
  * @param items
  */
class Checkpoint(var items: mutable.HashMap[String, CheckpointItem]) {

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
      case false => return null
      case _ => {
        items.update(newItem.id, newItem)
        return newItem
      }

    }

  }

}
