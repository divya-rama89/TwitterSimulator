import akka.actor._
import akka.actor.Actor
import akka.actor.ActorSystem
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

case class tweet(tweetText : String)
case class readStatus()
case class sendMeTweets()

class ServerRouter(numUsers: Int, ac: ActorSystem)  extends Actor{

  val ServerAssignService:ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
  
  // tweetTable : tweetID, text and owner's userID
  // var tweetTable = scala.collection.mutable.HashMap.empty[Int,Tuple2[Int,String]]
  var tweetIDCtr : Int = 0
  var initCtr : Int = 0
  
  // constants
  val NUMBEROFSERVERASSIGNERS = 64 
  val FOLLOWERSLIMIT = 10
  val DEBUG = true
  //val TWEETLIMIT = Int.MaxValue 
  
  // Will receive two types of messages from client 
  // Tweet message : senderNodeId. tweet
  // Read status : senderNodeId
    
  // Round Robin assign message
  
  def receive = {
    case "Init" => init() //generate actors + users/followers table
    case "done" => incInitCtr()
    case tweet(tweetText: String) => routeTweetMessage(sender,tweetText)
    case readStatus() => routeStatusRequest(sender)
    //case sendMeTweets() => sendingTweets(sender)
  }
  
  
  def init() { 
    
    // Create NUMBEROFSERVERASSIGNERS ServerAssigners
    for(x <- 0 to NUMBEROFSERVERASSIGNERS-1) {
    	ServerAssignService += ac.actorOf(Props(new ServerAssigner(numUsers, ac, x, NUMBEROFSERVERASSIGNERS, FOLLOWERSLIMIT)), "ServerAssigner"+x)
    	println("path is " + ServerAssignService(x).path)		
    }
    
    for(x <- 0 to NUMBEROFSERVERASSIGNERS-1) {
      ServerAssignService(x) ! "init"	
      ServerAssignService(x) ! AssignersListSend(ServerAssignService)
    }
    
    if(DEBUG) {
    	println("server init done")
    }
  }
  
  def incInitCtr() {
    var x = 0
    initCtr += 1
    if(initCtr == NUMBEROFSERVERASSIGNERS) {
        if(DEBUG) {
          println("All assigners initialised!")
        }
     
      // safe to send test tweet  
      if(DEBUG) {
      val dummy = ac.actorOf(Props(new TwitterClient(ac)), "3")
      println("path of dummy  is " + dummy.path)
      dummy ! "test" 
    }
    }
  }
  
  def routeTweetMessage(sender:ActorRef, tweetText:String) {
	  // generate tweet ID, put onto tweet table and pass to worker
    if(DEBUG) {
    	println("tweet : " + tweetText)
    }
    tweetIDCtr += 1
    
    if(DEBUG) {
    	println("inside routeTweet of router..ownerOfTweet- "+ sender.path.toString().substring(26).toInt)
    }
    
    val ownerID = (sender.path.toString).substring(26).toInt
   // var tweetInfo = new Tuple2(ownerID, tweetText) 
    
    // TODO: add a queue to make it fifo
   
    //tweetTable.put(tweetIDCtr, tweetInfo)
   
    ServerAssignService(ownerID % NUMBEROFSERVERASSIGNERS) ! tweetReceived(tweetText, ownerID)
  
  }

  def routeStatusRequest(sender:ActorRef) {
    
    var senderID = sender.path.toString().substring(26).toInt 
    /*
    if(DEBUG){
      println("sender="+sender)
      println("sender ID="+senderID)
      println("NUMBEROFSERVERASSIGNERS="+NUMBEROFSERVERASSIGNERS)
      println("(senderID % NUMBEROFSERVERASSIGNERS)="+(senderID % NUMBEROFSERVERASSIGNERS))
    }
    * 
    */
   
    ServerAssignService(senderID % NUMBEROFSERVERASSIGNERS) ! requestStatus(senderID, sender)
    if(DEBUG){
      println("sent")
    }
  }

}
