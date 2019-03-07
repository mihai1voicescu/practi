import java.net.InetAddress

object main extends App {
  val node1 = new Node(9000, "./node-files/1/")
  val node2 = new Node(9001, "./node-files/2/")

  node1.setNeighbours(List(Address(InetAddress.getByName("localhost"), node2.port)))
  node2.setNeighbours(List())

  val body = node1.createBody("file.txt")

  node1.sendToAllNeighbours(body)
}