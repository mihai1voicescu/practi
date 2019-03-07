import java.io._
import java.net.{ServerSocket, Socket, SocketException}


class Core(socket: ServerSocket, val node: Node) extends Runnable {
  override def run() {
    while (true)
      ServerThread(socket.accept(), node).start()
  }
}

case class ServerThread(socket: Socket, node: Node) extends Thread("ServerThread") {
  override def run(): Unit = {
    try {
      val ds = new DataInputStream(socket.getInputStream)
      val in = new ObjectInputStream(ds)


      val body = in.readObject().asInstanceOf[Body]
      println(this.socket.toString + " Received body " + body.path)

      body.bind(node)
      body.receive(ds)
    }
    catch {
      case e: SocketException =>
        () // avoid stack trace when stopping a client with Ctrl-C
      case e: IOException =>
        e.printStackTrace();
    }
  }

}