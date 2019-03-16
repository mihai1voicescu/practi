package invalidationlog

import clock.clock
import core.Controller

/**
  * Class, that is responsible for invalidation processing
  *
  * @param log instance to write.
  */

class InvalidationProcessor(log: Log, controller: Controller) {
  /**
    * Method, that contains invalidation processing logic
    *
    * @param invalidation to process
    */
  def process(invalidation: Invalidation): Unit = {
    clock.receiveStamp(invalidation);

    if (invalidation.timestamp > clock.time) {
      log.insert(invalidation)

      //NOTE: this is not tested yet. Probably will change heavily.
      controller.sendInvalidationForAllNeighbours(invalidation)
    }

    //TODO update checkpoint

  }
}
