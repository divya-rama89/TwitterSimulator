import akka.actor.Actor
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class ClientWorker(UID:Long, totalUsers:Long) extends Actor{

  var freq = Math.ceil(totalUsers*scala.math.pow(2, -UID/2))//var freq = Math.ceil(totalUsers*scala.math.pow(2, -UID/8))
  var numReq:Int = 0
  var reqRecvd:Int = 0
  val tweetScheduler = context.system.scheduler.schedule(0 seconds, 1/freq seconds, self, "tweet")
  val requestScheduler = context.system.scheduler.schedule(0 seconds, 1 seconds, self, "request")
  println(self.path+" "+UID + " " + freq)
  
  def receive = {
    case "tweet" => 
      //println(UID + "tweet")
      var text:String = generateRandomTweet()
      self ! ("TweetReq", text)
    
    case ("TweetReq", text:String) => 
      println(UID + " tweeted = " + text)
    
    case "request" => 
      println(UID+"requesting")
      numReq += 1      
      
    case ("Reply", list:List[String]) =>
      println(list.mkString("\n"))
      reqRecvd += 1
      
    case _ => println("Default case for ClientWorker")
  }
  
  def generateRandomTweet(): String = {
    var start : Int = Random.nextInt(1500)
    val fileText = io.Source.fromFile("data.txt").mkString.substring(start, start+140)
    fileText
  }
    

}