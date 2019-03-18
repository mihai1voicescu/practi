import core.Node

object main extends App {
  val node1 = new Node(9000, "./node-files/1/data/", "localhost", 1,"./node-files/1/log/log.txt")
  val node2 = new Node(9004, "./node-files/2/data/", "localhost", 2,"./node-files/2/log/log.txt")
  val node3 = new Node(9008, "./node-files/3/data/", "localhost", 3,"./node-files/3/log/log.txt")
  val node4 = new Node(9012, "./node-files/4/data/", "localhost", 4,"./node-files/4/log/log.txt")

  node1.addNeighbour(node2.getVirtualNode())
  node2.addNeighbour(node3.getVirtualNode())
  node2.addNeighbour(node1.getVirtualNode())
  node3.addNeighbour(node4.getVirtualNode())

  node1.controller.requestBody("very/deep/file.txt")
}
