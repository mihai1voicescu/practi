package invalidationlog

import clock.clock
import core.Controller

/**
  * Class, that is responsible for invalidation processing
  *
  * @param controller controller instance for accessing log, checkpoint and controller methods.
  */

class InvalidationProcessor(controller: Controller) extends Processor {
  /**
    * Method, that contains incoming invalidation processing logic
    *
    * @param invalidation to process
    */
  def process(invalidation: Invalidation): Unit = {
    clock.receiveStamp(invalidation);

    if (invalidation.timestamp > clock.time) {
      controller.log.insert(invalidation)

      controller.sendInvalidationForAllNeighbours(invalidation)
    }
  }

  /**
    * Method that processes object update. It expects already *increased* clock and sends the invalidation to all neighbours
    *
    * @param objId
    */
  def processUpdate(objId: String): Unit = {
    val invalidation = Invalidation(objId, clock.time, controller.node.id)

    //notifying the neighbours about invalidated object.
    controller.sendInvalidationForAllNeighbours(invalidation, false)
  }
}
