import java.io._
import java.net.Socket

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex


case class Body(@transient var directory: String, path: String) extends Serializable {
  val numberPattern: Regex = "(?:^|/)\\.\\.(/|$)".r

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
    val m = numberPattern.findFirstMatchIn(path)
    if (m.isDefined)
      throw Exceptions.SecurityException("SANDBOX_ERROR", "Body with potential sandbox injection detected.")

    val output = new FileOutputStream(directory + path)

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
