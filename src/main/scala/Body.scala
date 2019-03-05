import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, FileOutputStream}
import java.net.Socket

import scala.concurrent.{ExecutionContext, Future}

case class Body(_path: String) extends Serializable {

  import java.io.FileInputStream

  def send(conn: Socket): Unit = {
    val input = new BufferedInputStream(new FileInputStream(path))
    val output = conn.getOutputStream


    val byteArray = Array[Byte]()

    Future {
      while (input.read(byteArray) != -1) {
        output.write(byteArray)
      }
      input.close()
      output.close()
      conn.close()
      println("DONE B")
    }(ExecutionContext.global)
  }

  def receive(ds: DataInputStream): Unit = {


    val output = new FileOutputStream(path)

    val input = new BufferedInputStream(ds)

    val byteArray = Array[Byte]()

    Future {
      while (input.read(byteArray) != -1) {
        output.write(byteArray)
      }
      input.close()
      output.close()
    }(ExecutionContext.global)
  }

  def path: String = {
    globals.root + _path
  }
}
