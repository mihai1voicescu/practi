import java.io._
import java.net.{ServerSocket, Socket, SocketException}

import scala.io.BufferedSource

class Core(socket: ServerSocket) extends Runnable {
  override def run() {
    ServerThread(socket.accept()).start()
  }
}

case class ServerThread(socket: Socket) extends Thread("ServerThread") {
  override def run(): Unit = {
    try {
      val out = new BufferedOutputStream(socket.getOutputStream)
      val in = new BufferedSource(socket.getInputStream).getLines()

      while (true) {

        if (in.hasNext) {
          println(in.next())
        }
      }

      out.close()
      socket.close()
    }
    catch {
      case e: SocketException =>
        () // avoid stack trace when stopping a client with Ctrl-C
      case e: IOException =>
        e.printStackTrace();
    }
  }

}