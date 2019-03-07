trait ClockInfluencer {
  var timestamp : Long

  def sendStamp(): Unit = {
    timestamp = clock.time
    clock.sendStamp(this)
  }

  def receiveStamp(): Unit = {
    clock.receiveStamp(this)

  }
}
