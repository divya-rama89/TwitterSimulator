import akka.actor._
import akka.actor.Actor
import akka.actor.ActorSystem
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Queue

// This file is purely for testing purposes

case class receiveBackTweetQueue(que : Queue[Tuple2[String, Int]])

class TwitterClient(ac: ActorSystem)  extends Actor{


  def receive = {

    // DEBUG
    case "test" => callTest(sender)
    case receiveBackTweetQueue(que : Queue[Tuple2[String, Int]])=>
      {print("receiving back the tweets from server yahoooooo")
        for(x<-que)
        {
          print(x+",")
        }
      }
  }

  
  def callTest(sender: ActorRef) {
    println("I am a pseudo tweeter! :)")
    sender ! tweet("Hi")
    sender ! tweet("Is this 140 characters long or do I need to go on and on?")
    Thread.sleep(500)
    sender ! readStatus()
  }
  
}
