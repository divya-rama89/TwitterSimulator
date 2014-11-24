import akka.actor._
import akka.actor.Actor
import akka.actor.ActorSystem
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Queue

class mugdha(que : Queue[Tuple2[Int, Int]])

class TwitterClient(ac: ActorSystem)  extends Actor{


  def receive = {

    // DEBUG
    case "test" => callTest(sender)
  }

  
  def callTest(sender: ActorRef) {
    println("I am alive")
    sender ! tweet("Hi")
    sender ! readStatus()
  }
  
}
