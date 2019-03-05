object Client extends App {

  import java.net._
  import java.io._
  import scala.io._

  val s = new Socket(InetAddress.getByName("localhost"), 9999)
  val s1 = new Socket(InetAddress.getByName("localhost"), 9999)

  val b1 = Body("/file.txt")
  val b2 = Body("/file.txt")

  b1.send(s)
  b2.send(s1)


  lazy val in = new BufferedSource(s.getInputStream()).getLines()
  val out = new PrintStream(s.getOutputStream())

  out.println("Hello, world")
  out.flush()
  println("Received: " + in.next())

  s.close()
}// Simple client
