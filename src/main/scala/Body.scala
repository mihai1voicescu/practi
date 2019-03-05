import java.io.BufferedInputStream
import java.net.Socket


import scala.concurrent.{ExecutionContext, Future}

case class Body(path: String) extends Serializable {

  import java.io.FileInputStream

  def send(conn: Socket): Unit = {


    val input = new BufferedInputStream(new FileInputStream(globals.root + path))
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
}
