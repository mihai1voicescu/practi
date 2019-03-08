package invalidation

import log.Update


/*
POJO class for invalidation
 */
case class Invalidation(objectId: String, timestamp: Long, nodeId: Long) extends Serializable {

  /*
  Method, that transforms invalidation to @Update object.
   */
  def toUpdate(): Update = {
    new Update(this.objectId, this.timestamp)
  }
}
