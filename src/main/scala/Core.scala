import java.io._
import java.net.{InetAddress, ServerSocket, Socket, SocketException}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import helper.fileHelper

import scala.concurrent.ExecutionContextExecutor
import invalidationlog.Log
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default

import akka.http.scaladsl.server.Directives._

import java.nio.file._
import akka.http.scaladsl.model.StatusCodes

class Core(val node: Node, logLocation: String) extends Runnable {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val route =

    get {
      path(Remaining) { name: String =>
        fileHelper.checkSandbox(name)
        getFromFile(node.root + name) // uses implicit ContentTypeResolver
      }
    } ~ withSizeLimit(200 * 1024 * 1024) {
      put {
        path(Remaining) { name: String =>
          fileHelper.checkSandbox(name)
          storeUploadedFile("file", fileHelper.tempDestination) {
            case (meta, file) =>
              val objectId = name + "/" + meta.getFileName
              fileHelper.checkSandbox(objectId)

              Files.move(file.toPath, Paths.get(node.root + objectId), StandardCopyOption.REPLACE_EXISTING)
              node.invalidate(objectId)

              complete(StatusCodes.OK)
          }
        }
      }
    }


  private val log = new Log(logLocation)

  private val acceptSocket = new ServerSocket(node.getCorePort, 50, InetAddress.getByName(node.hostname))
  private val httpServer = Http().bindAndHandle(route, interface = node.hostname, port = node.getHTTPPort)

  override def run() {
    while (true)
      ServerThread(acceptSocket.accept(), node).start()
  }


  def sendBody(virtualNode: VirtualNode, body: Body): Unit = {
    println(this.acceptSocket.toString + " Sending body " + body.path)
    body.send(new Socket(virtualNode.hostname, virtualNode.getCorePort))
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