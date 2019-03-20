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
  def getById(objId: String): CheckpointItem = {
    items(objId)
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
    * Method that inserts item into checkpoint.
    *
    * @param item
    * @return
    */
  private def insert(item: CheckpointItem): CheckpointItem = {
    items += item.id -> item
    return item
  }

}
