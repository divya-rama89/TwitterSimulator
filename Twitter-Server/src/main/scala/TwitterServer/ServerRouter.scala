import akka.actor._
import akka.actor.Actor
import akka.actor.ActorSystem
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class tweet(tweetText: String)

class ServerRouter(numUsers: Int, numClients: Int, ac: ActorSystem) extends Actor {

  //val ServerAssignService: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]

  // tweetTable : tweetID, text and owner's userID
  // var tweetTable = scala.collection.mutable.HashMap.empty[Int,Tuple2[Int,String]]
  var tweetIDCtr: Int = 0
  var tweetIDCumulativeCtr: BigInt = 0
  var requestCtr: BigInt = 0
  var requestCumulativeCtr: BigInt = 0
  var secondsCtr : Int = 0 
  
  var initCtr: Int = 0
  var initClientCtr: Int = 0
  var doneCtr: Int = 0
  var ClientList: List[ActorRef] = List()

  // constants
  val NUMBEROFSERVERASSIGNERS = 3
  val FOLLOWERSLIMIT = 10
  val DEBUG = false
  //val TWEETLIMIT = Int.MaxValue 

  // Will receive two types of messages from client 
  // Tweet message : senderNodeId. tweet
  // Read status : senderNodeId

  // Round Robin assign message

  def receive = {

    case "Init" => init() //generate actors + users/followers table
    case "init2" => init2()
    case "hi" =>
      println("Received Communication from some client")
      incClientCtr(sender)
    //case tweet(tweetText: String) => routeTweetMessage(sender,tweetText)
      
    case "done" => incInitCtr()

    case ("TweetReq", text: String) => 
     // println("Tweet Received:::::" + text)
      routeTweetMessage(sender, text)
      
    case "request" => routeStatusRequest(sender)
    
    /*case "test" => 
      println(sender.path)
      sender ! "ack"*/
    case "stopCode" =>  
      for (coordinator <- ClientList) {
        coordinator ! "die"
      }
      for (x <- 0 to NUMBEROFSERVERASSIGNERS - 1) {
       var selection = context.child(x.toString).getOrElse(null)
       if(selection != null)   {   
           selection ! "stop"
       }
     }
            
      Thread.sleep(1000)
      
      // print metrics
      println("Average Rate of tweet = "+tweetIDCumulativeCtr/secondsCtr + "\nAverage Rate of requests = "+requestCtr/secondsCtr )
      ac.shutdown
      
   // case "ThankYou" => incDoneCtr()  
    
    case _ => println("Received unknown communication from " + sender.path)
  }

  def init() {

    // Create NUMBEROFSERVERASSIGNERS ServerAssigners
    for (x <- 0 to NUMBEROFSERVERASSIGNERS - 1) {
      //ServerAssignService += ac.actorOf(Props(new ServerAssigner(numUsers, ac, x, NUMBEROFSERVERASSIGNERS, FOLLOWERSLIMIT)), "ServerAssigner" + x)
	  context.actorOf(Props(new ServerAssigner(numUsers, ac, x, NUMBEROFSERVERASSIGNERS, FOLLOWERSLIMIT)), x.toString)
      //	println("path is " + ServerAssignService(x).path)		
    }

    for (x <- 0 to NUMBEROFSERVERASSIGNERS - 1) {
   var selection = context.child(x.toString).getOrElse(null)
       if(selection != null)   {   
           selection ! "init"
//    	   println("selection ="+ selection.path)
       }

    }

    if (DEBUG) {
      println("server init done")
    }
    
     var timer = ac.actorOf(Props(new timerActor(self)), "timerActor")
     timer ! "start"
     
     /*// starting metric calculator scheduler
     println("tweetIDctr\ttweetIDCumulativeCtr\trequestCtr")
     val tweetScheduler = context.system.scheduler.schedule(0 seconds, 1 seconds)(metricReset)*/
  }
  
  def init2() {
   println("inside init2") 
    for (x <- 0 to NUMBEROFSERVERASSIGNERS - 1) {
    	var selection = context.actorSelection("akka://TwitterServer/user/ServerRouter0/"+x) //("../"+0)
    	selection ! "hi"
     }
  }

  def incInitCtr() {
    var x = 0
    initCtr += 1
    if (initCtr == NUMBEROFSERVERASSIGNERS) {
      if (DEBUG) {
        println("All assigners initialised!")
      }
/*
      // safe to send test tweet  
      if (DEBUG) {
        val dummy = ac.actorOf(Props(new TwitterClient(ac)), "3")
        println("path of dummy  is " + dummy.path)
        dummy ! "test" 
      } */
    }
  }

  def incClientCtr(sender: ActorRef) {
    var x = 0
    initClientCtr += 1
    if (!ClientList.contains(sender)) {
      ClientList = ClientList :+ sender
    }
    if (DEBUG) {
      println("Client: " + sender.path + " initialised!")
    }
    if (initClientCtr >= numClients) {
      println("All Clients have started ... " + ClientList.mkString("::::"))
      for (coordinator <- ClientList) {
        coordinator ! "start"
      }
    }
    
    // starting metric calculator scheduler
     println("tweetIDctr\ttweetIDCumulativeCtr\trequestCtr")
     val tweetScheduler = context.system.scheduler.schedule(0 seconds, 1 seconds)(metricReset)
  }
  
  /*
  def incDoneCtr() {
    doneCtr += 1
    if(doneCtr == numClients){
      println("="*20)
      println("I am done! Thank You!")
      println("="*20)
      Thread.sleep(1000)
      for (x <- 0 to NUMBEROFSERVERASSIGNERS - 1) {
       var selection = context.child(x.toString).getOrElse(null)
       if(selection != null)   {   
           selection ! "stop"
    //	   println("selection ="+ selection.path)
       }
     }
      
      //context.stop(self)
      ac.shutdown
    }
  }
  * 
  */
  
  def metricReset = {
    println(tweetIDCtr +",\t\t\t"+ tweetIDCumulativeCtr+",\t\t\t"+ requestCtr )
    
    tweetIDCtr = 0
    //requestCtr = 0
    secondsCtr += 1
  }

  def routeTweetMessage(sender: ActorRef, tweetText: String) {
    // generate tweet ID, put onto tweet table and pass to worker
    if (DEBUG) {
      println("tweet : " + tweetText)
    }
    tweetIDCtr += 1
    tweetIDCumulativeCtr += 1

    val senderpath = sender.path.toString()
    var n = senderpath.lastIndexOf("/");
    var ownerID = senderpath.substring(n + 1).toInt

    //tweetTable.put(tweetIDCtr, tweetInfo)
    
    var selection = context.actorSelection("akka://TwitterServer/user/ServerRouter0/"+(ownerID % NUMBEROFSERVERASSIGNERS)) 
    selection ! tweetReceived(tweetText, ownerID)

  }

  def routeStatusRequest(sender: ActorRef) {

    requestCtr += 1
    //var senderID = sender.path.toString().substring(26).toInt 
    val senderpath = sender.path.toString()
    var n = senderpath.lastIndexOf("/");
    var senderID = senderpath.substring(n + 1).toInt

    /*
    if(DEBUG){
      println("sender="+sender)
      println("sender ID="+senderID)
      println("NUMBEROFSERVERASSIGNERS="+NUMBEROFSERVERASSIGNERS)
      println("(senderID % NUMBEROFSERVERASSIGNERS)="+(senderID % NUMBEROFSERVERASSIGNERS))
    }
    * 
    */

    var selection = context.actorSelection("akka://TwitterServer/user/ServerRouter0/"+(senderID % NUMBEROFSERVERASSIGNERS)) 
   selection ! requestStatus(senderID, sender)
    if (DEBUG) {
      println("sent")
    }
  }

}
