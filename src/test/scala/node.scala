import core.Node

class node extends TestBase {
  // test that seeds checkpoint with test data for arbitrary node.
  test("node.seedCheckpoint") {
    val node1 = new Node(9100, "./testing/1/data/", "localhost", 1, "./node-files/1/log/log.txt")
    node1.seedCheckpoint()
    assert(node1.checkpoint.items.size == 2)
  }
}
