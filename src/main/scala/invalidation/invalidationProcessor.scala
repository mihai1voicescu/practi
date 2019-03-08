package invalidation

import log.Log


/**
  * Class, that is responsible for invalidation processing
  *
  * @param log instance to write.
  */

class InvalidationProcessor(log: Log) {
  /**
    * Method, that contains invalidation processing logic
    *
    * @param invalidation to process
    */
  def process(invalidation: Invalidation): Unit = {
    log.insert(invalidation.toUpdate())

  }
}
