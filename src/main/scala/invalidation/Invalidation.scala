package invalidation

import log.Update


/*
POJO class for invalidation
 */
case class Invalidation(objectId: String,override var timestamp: Long, nodeId: Int) extends Serializable with clock.ClockInfluencer {
  /*
  Method, that transforms invalidation to @Update object.
   */
  def toUpdate(): Update = {
    new Update(this.objectId, this.timestamp)
  }
}
