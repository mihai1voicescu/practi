import java.io._
import java.net.{InetAddress, ServerSocket, Socket, SocketException}

import log.Log

//import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.HttpMethods._
//import akka.http.scaladsl.model._

class Core(val node: Node, logLocation: String) extends Runnable {
  private val log = new Log(new File(logLocation))

  private val acceptSocket = new ServerSocket(node.port, 50, InetAddress.getByName(node.hostname))
  //  private val httpServer = Http().bind(interface = node.hostname, port = node.port)
  //
  //
  //  val requestHandler: HttpRequest => HttpResponse = {
  //    case HttpRequest(GET, uri, _, _, _) =>
  //      val body = Body(node.root, uri.path.toString())
  //      body.send(httpSocket, false)
  //
  //    case r: HttpRequest =>
  //      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
  //      HttpResponse(404, entity = "Unknown resource!")
  //  }

  override def run() {
    while (true)
      ServerThread(acceptSocket.accept(), node).start()
  }


  def sendBody(address: Address, body: Body): Unit = {
    println(this.acceptSocket.toString + " Sending body " + body.path)
    body.send(new Socket(address.host, address.port))
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