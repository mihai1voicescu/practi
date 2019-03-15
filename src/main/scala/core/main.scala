package core

object main extends App {
  val node1 = new Node(9000, "./node-files/1/data/", "localhost", 1,"./node-files/1/log/log.txt")
  val node2 = new Node(9004, "./node-files/2/data/", "localhost", 2,"./node-files/2/log/log.txt")

  node1.addNeighbour(node2)

  val body = node1.createBody("very/deep/file.txt")

  node1.sendToAllNeighbours(body)
}
