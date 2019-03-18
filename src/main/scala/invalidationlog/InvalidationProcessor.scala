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
    * Method, that contains invalidation processing logic
    *
    * @param invalidation to process
    */
  def process(invalidation: Invalidation): Unit = {
    clock.receiveStamp(invalidation);

    if (invalidation.timestamp > clock.time) {
      controller.log.insert(invalidation)

      //NOTE: this is not tested yet. Probably will change heavily.
      controller.sendInvalidationForAllNeighbours(invalidation)
    }
  }
}
