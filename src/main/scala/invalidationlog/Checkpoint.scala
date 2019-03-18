package invalidationlog

/**
  * Class that logically represents a Checkpoint.
  *
  * @param items
  */
class Checkpoint(var items: List[CheckpointItem]) {

  /**
    * Method, that returns @CheckpointItem by object ID.
    *
    * @param objId
    * @return
    */
  def getById(objId: String): Option[CheckpointItem] = {
    items.find(i => i.id.equals(objId))
  }

}
