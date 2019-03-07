import java.io._

class Log(file: File) {

  def insert(update: Update): Unit = {
    val fileOutputStream = new FileOutputStream(this.file)
    val objectOutputStream = new ObjectOutputStream(fileOutputStream)
    objectOutputStream.writeObject(update)
  }

  def readLatest: Update = {
    val fileInputStream = new FileInputStream(this.file)
    val objectInputStream = new ObjectInputStream(fileInputStream)
    val update = objectInputStream.readObject().asInstanceOf[Update]
    update
  }
}
