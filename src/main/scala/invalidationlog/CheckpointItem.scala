package invalidationlog

import core.Body

/**
  * Data holder class for @Checkpoint.
  * @param id of an object
  * @param body
  * @param invalid
  * @param timestamp
  */
case class CheckpointItem(id: String, body: Body, invalid: Boolean, timestamp: Long) {
}
