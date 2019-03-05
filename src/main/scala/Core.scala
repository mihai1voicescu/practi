import java.io._
import java.net.{ServerSocket,Socket,SocketException}

import scala.concurrent.Future

class Core(_port: Int) extends Runnable{
  val port: Int = _port
  val acceptSocket = new ServerSocket(port)

  override def run() {
    ServerThread(acceptSocket.accept()).start()
  }
}


case class ServerThread(socket: Socket) extends Thread("ServerThread") {

  override def run(): Unit = {
    try {
      val out = new DataOutputStream(socket.getOutputStream)
      val ds = new DataInputStream(socket.getInputStream)
      val in = new ObjectInputStream(ds)


      val body = in.readObject().asInstanceOf[Body]

      body.receive(ds)



      while (true) {
        var succeeded = false;
        do {
          val x = rand.nextInt(1000);
          succeeded = filter(x);
          if (succeeded) out.writeInt(x)
        } while (! succeeded);
        Thread.sleep(100)
      }

      out.close();
      in.close();
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