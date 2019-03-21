package core

import java.io._
import java.net.Socket
import java.nio.file.{Files, Paths}

import clock.ClockInfluencer
import helper.fileHelper

import scala.concurrent.{ExecutionContext, Future}

case class Body(@transient var directory: String, path: String) extends Serializable with ClockInfluencer  {
  override var timestamp: Long = 0

  import java.io.FileInputStream

  def bind(node: Node): Unit = {
    directory = node.dataDir
  }

  def send(conn: Socket, withStamp :Boolean = true): Unit = {
    val input = new BufferedInputStream(new FileInputStream(directory + path))
    val output = conn.getOutputStream

    new ObjectOutputStream(output).writeObject(this)

    val byteArray = new Array[Byte](4 * 1024)

    if (withStamp)
      sendStamp()

    Future {
      var len = input.read(byteArray)
      while (len != -1) {
        output.write(byteArray, 0, len)
        len = input.read(byteArray)
      }
      output.flush()
      input.close()
      output.close()
      conn.close()
    }(ExecutionContext.global)
  }

  def receive(ds: InputStream): Unit = {
    fileHelper.checkSandbox(path)

    val filePath = directory + path
    // If the path does not exist yet, create the necessary parent folders
    if (!Files.exists(Paths.get(filePath))) {
      val file = new File(new File(filePath).getParent)
      file.mkdirs()
    }

    val output = new FileOutputStream(directory + path)
    val input = new BufferedInputStream(ds)

    val byteArray = new Array[Byte](4 * 1024)

    receiveStamp()

    Future {
      var len = input.read(byteArray)
      while (len != -1) {
        output.write(byteArray, 0, len)
        len = input.read(byteArray)
      }
      input.close()
      output.close()
    }(ExecutionContext.global)
  }

}
