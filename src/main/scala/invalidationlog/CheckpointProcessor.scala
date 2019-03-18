package invalidationlog

import core.Controller

/**
  * Class that is responsible for processing invalidations in terms of checkpoint.
  *
  * @param controller
  */
class CheckpointProcessor(controller: Controller) extends Processor {
  /**
    * General method that processes upcoming invalidation in terms of checkpoint.
    *
    * @param invalidation
    */
  override def process(inv: Invalidation): Unit = {
    processInvalidation(controller.node.checkpoint.getById(inv.objId), inv)
  }

  /**
    * Functional style function, that process null values.
    *
    * @param existingItem
    * @param inv
    */
  private def processInvalidation(existingItem: Option[CheckpointItem], inv: Invalidation): Unit = {
    existingItem match {
      case Some(item) => processExisting(inv, item)
      case None => addNewItem(inv)
    }
  }

  private def addNewItem(newItem: Invalidation): Unit = {
    // not sure what to do with invalidations that contains object ID which is not in the checkpoint
  }

  /**
    * Method that processes invalidation on existing checkpoint item. If timestamp of invalidation is higher, it updates checkpoint, marking the item invalid
    *
    * @param inv  received invalidation
    * @param item existing checkpoint item
    */
  private def processExisting(inv: Invalidation, item: CheckpointItem): Unit = {
    //  From the book "Note that checkpoint update for an incoming invalidation is skipped if the checkpoint entry already stores a logical time that is at least as high
    // as the logical time of the incoming invalidation."
    if (inv.timestamp <= item.timestamp) return

    val newItem = new CheckpointItem(item.id, item.body, true, inv.timestamp)
    controller.node.checkpoint.items = controller.node.checkpoint.items.updated(controller.node.checkpoint.items.indexOf(item), newItem)
  }
}
