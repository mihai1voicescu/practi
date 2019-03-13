package invalidationlog

/*
POJO class for invalidation
 */
case class Invalidation(objId: String, var timestamp: Long, nodeId: Int) extends Update(objId, timestamp) with Serializable {
}
