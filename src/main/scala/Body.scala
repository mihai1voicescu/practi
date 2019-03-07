import java.io._
import java.net.Socket
import java.nio.file.{Files, Paths}

import scala.concurrent.{ExecutionContext, Future}

case class Body(@transient var directory: String, path: String) extends Serializable {

  import java.io.FileInputStream

  def bind(node: Node): Unit = {
    directory = node.root
  }

  def send(conn: Socket): Unit = {
    val input = new BufferedInputStream(new FileInputStream(directory + path))
    val output = conn.getOutputStream

    new ObjectOutputStream(output).writeObject(this)

    val byteArray = new Array[Byte](4 * 1024)

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
    val filePath = directory + path

    // If the path does not exist yet, create the necessary parent folders
    if (!Files.exists(Paths.get(filePath))) {
      val file = new File(new File(filePath).getParent)
      file.mkdirs()
    }

    val output = new FileOutputStream(new File(filePath))

    val input = new BufferedInputStream(ds)

    val byteArray = new Array[Byte](4 * 1024)

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
