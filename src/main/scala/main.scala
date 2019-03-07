import java.net.InetAddress

object main extends App {
  val node1 = new Node(9000, "./node-files/1/data/", "localhost", "./node-files/1/log/log.txt")
  val node2 = new Node(9001, "./node-files/2/data/", "localhost", "./node-files/2/log/log.txt")

  node1.setNeighbours(List(Address(InetAddress.getByName("localhost"), 9001)))

  val body = node1.createBody("very/deep/structured/file.txt")

  node1.sendToAllNeighbours(body)
}