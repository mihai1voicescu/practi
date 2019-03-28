package core

import java.io._
import java.net.{InetAddress, ServerSocket, Socket, SocketException}
import java.nio.file._
import java.util.logging.{Level, Logger}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import akka.stream.ActorMaterializer
import controller.ReqBody
import helper.fileHelper
import invalidationlog.Log

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}

object Core {
  private val LOGGER = Logger.getLogger(core.Core.getClass.getName)
}

class Core(val node: Node) extends Runnable {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val listeners = new mutable.ParHashMap[String, ListBuffer[Promise[Unit]]]()


  private val route =

    get {
      path(Remaining) { name: String =>
//        handleRejections(RejectionHandler.newBuilder()
//          .handleNotFound({
//            logMessage("Body not found")
//            node.controller.requestBody(name)
//
//
//            val p = Promise[Unit]()
//            listeners.get(name) match {
//              case None =>
//                val l = ListBuffer[Promise[Unit]]()
//                listeners.+=(name -> l)
//                l += p
//              case Some(r) => r += p
//            }
//
//
//            onComplete(p.future) {
//              case Success(_) => println("COMPLETED")
//                getFromFile(node.dataDir + name)
//              // todo Failure is actually on top
//              case Failure(_) => complete(StatusCodes.NotFound)
//            }
//          })
//          .result())

      {
          fileHelper.checkSandbox(name)
          if (node.hasValidBody(name)) {
            logMessage("File valid")
            getFromFile(node.dataDir + name) // uses implicit ContentTypeResolver
          }

          else {
            val p = Promise[Unit]()
            listeners.get(name) match {
              case None =>
                val l = ListBuffer[Promise[Unit]]()
                listeners.+=(name -> l)
                l += p
              case Some(r) => r += p
            }
            onComplete(p.future) {
              case Success(_) => println("COMPLETED")
                getFromFile(node.dataDir + name)
              // todo Failure is actually on top
              case Failure(_) => complete(StatusCodes.NotFound)
            }
          }
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

              val dest = new File(node.dataDir + objectId)

              if (!dest.exists()) {
                dest.getParentFile.mkdirs()
              }

              Files.move(file.toPath, Paths.get(node.dataDir + objectId), StandardCopyOption.REPLACE_EXISTING)
              node.invalidate(objectId)

              complete(StatusCodes.OK)
          }
        }
      }
    }

  private val acceptSocket = new ServerSocket(node.getCorePort, 50, InetAddress.getByName(node.hostname))
  logMessage(s"Core Server running on ${node.getControllerPort}")

  private val httpServer = Http().bindAndHandle(route, interface = node.hostname, port = node.getHTTPPort)
  logMessage(s"HTTP Server running on ${node.getHTTPPort}")

  def logMessage(message: String, level: Level = null, logger: Logger = Core.LOGGER): Unit = {
    node.logMessage(s"$message", level, logger)
  }

  override def run() {
    while (true) {
      val s = acceptSocket.accept()

      Future[Unit] {
        receiveBody(s)
      }(ExecutionContext.global)
    }
  }

  def receiveBody(socket: Socket): Unit = {
    val ds = new DataInputStream(socket.getInputStream)
    val in = new ObjectInputStream(ds)

    try {
      in.readObject() match {
        case body: Body =>
          println(node + " Received body " + body.path)

          if (node.checkpoint.isNewer(body)) {
            body.bind(node)
            body.receive(ds, node.checkpoint).onComplete(_ => {
              listeners.remove(body.path) match {
                case Some(l) => l.foreach(_.success())
                case None =>
              }
            })
          } else {
            println("BODY IS OLD")
          }

        case reqBody: ReqBody =>
          reqBody.body.bind(this.node)
          sendBody(reqBody.virtualNode, reqBody.body)
        case _ =>
      }
    } catch {
      case e: SocketException =>
        e.printStackTrace();
      case e: IOException =>
        e.printStackTrace();
    }

  }

  def sendBody(virtualNode: VirtualNode, body: Body): Unit = {
    val a = node.checkpoint.getById(body.path)
    a match {
      case Some(checkpointItem) =>
        if (checkpointItem.invalid) {
          logMessage(node + " not sending body " + body.path + " to " + virtualNode + " as it is invalid")
        } else {
          logMessage(node + " Sending body to " + virtualNode + " for " + body.path)
          body.send(new Socket(virtualNode.hostname, virtualNode.getCorePort))
        }
    }
  }
}
