package core

import java.io.ObjectOutputStream
import java.net.Socket

import scala.concurrent.{ExecutionContext, Future}

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

  @transient def sendToController(objectToSend: Object): Unit = {
    this.synchronized {
      if (controllerSocket == null) {
        controllerSocket = new Socket(hostname, getControllerPort)
        controllerSocket.setKeepAlive(true)
      }

      val output = controllerSocket.getOutputStream
      new ObjectOutputStream(output).writeObject(objectToSend)

      val byteArray = new Array[Byte](4 * 1024)

      output.write(byteArray)

      output.flush()
    }
  }

  @transient def sendToControllerAsync(objectToSend: Object): Future[Unit] = {
    Future {
      sendToController(objectToSend)
    }(ExecutionContext.global)
  }

  override def toString: String = {
    this.id + "[" + hostname + ":" + port + "]"
  }
}
