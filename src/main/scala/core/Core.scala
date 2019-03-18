package core

import java.io._
import java.net.{InetAddress, ServerSocket, Socket, SocketException}
import java.nio.file._
import java.util.logging.{Level, Logger}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MethodRejection, RejectionHandler, Route}
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import akka.stream.ActorMaterializer
import helper.fileHelper
import invalidationlog.Log

import scala.concurrent.ExecutionContextExecutor

object Core {
  private val LOGGER = Logger.getLogger(core.Core.getClass.getName)
}

class Core(val node: Node, logLocation: String) extends Runnable {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val fallbackToRemoteNode = RejectionHandler.newBuilder()
    .handleNotFound({
      // TODO get the file from a remote node
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>FILE NOT FOUND AND FALLBACK NOT IMPLEMENTED</h1>"))
    })
    .result()

  private val route =

    get {
      handleRejections(fallbackToRemoteNode) {
        path(Remaining) { name: String =>
          fileHelper.checkSandbox(name)
          getFromFile(node.root + name) // uses implicit ContentTypeResolver
        }
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
  logMessage(s"Core Server running on ${node.getControllerPort}")

  private val httpServer = Http().bindAndHandle(route, interface = node.hostname, port = node.getHTTPPort)
  logMessage(s"HTTP Server running on ${node.getHTTPPort}")

  def logMessage(message: String, level: Level = null, logger: Logger = Core.LOGGER): Unit = {
    node.logMessage(s"$message", level, logger)
  }

  override def run() {
    while (true)
      ServerThread(acceptSocket.accept(), node).start()
  }


  def sendBody(virtualNode: VirtualNode, body: Body): Unit = {
    println(node + " Sending body to " + virtualNode + " for " + body.path)
    body.send(new Socket(virtualNode.hostname, virtualNode.getCorePort))
  }
}

case class ServerThread(socket: Socket, node: Node) extends Thread("ServerThread") {
  override def run(): Unit = {
    try {
      val ds = new DataInputStream(socket.getInputStream)
      val in = new ObjectInputStream(ds)

      in.readObject() match {
        case body: Body => {
          println(node + " Received body " + body.path)

          body.bind(node)
          body.receive(ds)
        }
        case _ =>
      }

    }
    catch {
      case e: SocketException =>
        () // avoid stack trace when stopping a client with Ctrl-C
      case e: IOException =>
        e.printStackTrace();
    }
  }

}