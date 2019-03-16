package core

import java.net.Socket

class VirtualNode(val id: Int, val hostname: String, val port: Int) extends Serializable {

  @transient var controllerSocket: Socket = null

  def getControllerPort: Int = {
    port + 2
  }

  def getCorePort: Int = {
    port
  }

  def getHTTPPort: Int = {
    port + 1
  }

  @transient def getControllerSocket: Socket = {
    if (controllerSocket == null) {
      controllerSocket = new Socket(hostname, getControllerPort)
      controllerSocket.setKeepAlive(true)
    }


    controllerSocket
  }

  override def toString: String = {
    this.id + "[" + hostname + ":" + port + "]"
  }
}
