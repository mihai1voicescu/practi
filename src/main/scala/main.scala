import java.net.InetAddress

object main extends App {
  val node1 = new Node(9000, "./node-files/1/", "145.94.190.219")
  val node2 = new Node(9001, "./node-files/2/", "145.94.190.219")

  node1.setNeighbours(List(Address(InetAddress.getByName("145.94.190.219"), node2.port)))
  node2.setNeighbours(List())

  val body = node1.createBody("file.txt")

  node1.sendToAllNeighbours(body)
}