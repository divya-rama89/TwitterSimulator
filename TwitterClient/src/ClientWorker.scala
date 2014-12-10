import akka.actor.Actor
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import akka.actor.ActorSystem
import scala.collection.mutable.Queue
import akka.remote.RemoteTransportExceptionNoStackTrace

//case class receiveBackTweetQueue(requiredQ :Queue[Tuple2[String, Int]])

class ClientWorker(UID: Int, totalUsers: Long, serIP: String, serPort: String, sys: ActorSystem, cliID:Int) extends Actor {

  var freq = Math.ceil(totalUsers * scala.math.pow(2, -UID / 2)) //var freq = Math.ceil(totalUsers*scala.math.pow(2, -UID/8))
  var numReq: Int = 0
  var reqRecvd: Int = 0
  val harryPotter: String = "'Filth! Scum! By-products of dirt and vileness! Half-breeds, mutants, freaks, begone from this place!How dare you befoul the house of my fathers -'Tonks apologised over and over again, dragging the huge, heavy troll's leg back off the floor; MrsWeasley abandoned the attempt to close the curtains and hurried up and down the hall, stunning all theother portraits with her wand; and a man with long black hair came charging out of a door facing Harry. 'Shut up, you horrible old hag, shut UP!' he roared, seizing the curtain Mrs Weasley had abandoned. The old woman's face blanched. 'Yoooou!' she howled, her eyes popping at the sight of the man. 'Blood traitor, abomination, shameof my flesh!''I said - shut - UP!' roared the man, and with a stupendous effort he and Lupin managed to force thecurtains closed again. The old woman's screeches died and an echoing silence fell. Panting slightly and sweeping his longdark hair out of his eyes, Harry's godfather Sirius turned to face him. 43'Hello, Harry, ' he said grimly, 'I see you've met my mother. 'Your -?''My dear old mum, yeah, ' said Sirius. 'We've been trying to get her down for a month but we thinkshe put a Permanent Sticking Charm on the back of the canvas. Let's get downstairs, quick, before theyall wake up again. ''But what's a portrait of your mother doing here?' Harry asked, bewildered, as they went through thedoor from the hall and led the way down a flight of narrow stone steps, the others just behind them. 'Hasn't anyone told you? This was my parents' house, ' said Sirius. 'But I'm the last Black left, so it'smine now. I offered it to Dumbledore for Headquarters - about the only useful thing I've been able todo."
  //println("Actor Created = "+ self.path+" "+UID + " " + freq)

  var randAssigner: String = Integer.toString(Random.nextInt(4))
  var url: String = "akka.tcp://TwitterServer@" + serIP + ":" + serPort + "/user/ServerRouter"+randAssigner 
  var server = sys.actorSelection(url)

  def receive = {

    case "start" => start()

    case (requiredQ: Queue[Tuple2[String, Int]]) => {
      if (!requiredQ.isEmpty) {
        requiredQ.foreach { i =>
          //println("For User:::::::"+UID+" Queue entry - owner:"+ i._2 +" Tweet:"+ i._1 )
          //println("-"*20)
        }
      }
    }

    case x => println("Default " + UID + " " + x + " " + sender.path)

  }

  def request = {
    try {
        server ! "request"
    }
        catch {
      case ex: RemoteTransportExceptionNoStackTrace =>{
           println("yayyy..its handled!")
        }
  }
  }

  def tweet = {
    var start: Int = Random.nextInt(1500)
    var text: String = harryPotter.substring(start, start + 140)
    //println(UID+":::::::"+text)
    try {
      server ! ("TweetReq", text)
    } catch {
      case ex: RemoteTransportExceptionNoStackTrace =>{
           println("yayyy..its handled!")
        }
    }
  }
  
  def start() = {

    if (freq != 1) {
      val tweetScheduler = context.system.scheduler.schedule(0 seconds, 10.toDouble / freq.toDouble seconds)(tweet)
    } else {
      //println("time / freq = "+ (UID.toDouble/totalUsers.toDouble*10).toInt)
      val tweetScheduler = context.system.scheduler.scheduleOnce((UID.toDouble / totalUsers.toDouble * 100.0).toInt seconds)(tweet)
    }
    val requestScheduler = context.system.scheduler.schedule(UID/100 seconds, 10 seconds)(request)
    val eventScheduler = context.system.scheduler.scheduleOnce(20 seconds)(tweet)
  }
}
