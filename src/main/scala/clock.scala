object clock {
  var time :Long = 0

  def sendStamp(obj : ClockInfluencer): Unit = {
    time += 1
    obj.timestamp = time
  }

  def receiveStamp(obj : ClockInfluencer): Unit = {
    time = Math.max(obj.timestamp, time) + 1
  }
}
