import akka.actor._
import akka.actor.Actor
import akka.actor.ActorSystem
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

case class tweet(tweetText: String)

class ServerRouter(numUsers: Int, numClients: Int, ac: ActorSystem) extends Actor {

  val ServerAssignService: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]

  // tweetTable : tweetID, text and owner's userID
  // var tweetTable = scala.collection.mutable.HashMap.empty[Int,Tuple2[Int,String]]
  var tweetIDCtr: Int = 0
  var initCtr: Int = 0
  var initClientCtr: Int = 0
  var ClientList: List[ActorRef] = List()

  // constants
  val NUMBEROFSERVERASSIGNERS = 64
  val FOLLOWERSLIMIT = 10
  val DEBUG = false
  //val TWEETLIMIT = Int.MaxValue 

  // Will receive two types of messages from client 
  // Tweet message : senderNodeId. tweet
  // Read status : senderNodeId

  // Round Robin assign message

  def receive = {
    case "Init" => init() //generate actors + users/followers table
    case "done" => incInitCtr()
    case "hi" =>
      println("Received Communication from some client")
      incClientCtr(sender)
    //case tweet(tweetText: String) => routeTweetMessage(sender,tweetText)
    case ("TweetReq", text: String) => println("Tweet Received " + text + sender.path)
    case "request" => routeStatusRequest(sender)
    /*case "test" => 
      println(sender.path)
      sender ! "ack"*/
    case _ => println("Received communication from " + sender.path)
  }

  def init() {

    // Create NUMBEROFSERVERASSIGNERS ServerAssigners
    for (x <- 0 to NUMBEROFSERVERASSIGNERS - 1) {
      ServerAssignService += ac.actorOf(Props(new ServerAssigner(numUsers, ac, x, NUMBEROFSERVERASSIGNERS, FOLLOWERSLIMIT)), "ServerAssigner" + x)
      //	println("path is " + ServerAssignService(x).path)		
    }

    for (x <- 0 to NUMBEROFSERVERASSIGNERS - 1) {
      ServerAssignService(x) ! "init"
      ServerAssignService(x) ! AssignersListSend(ServerAssignService)
    }

    if (DEBUG) {
      println("server init done")
    }
  }

  def incInitCtr() {
    var x = 0
    initCtr += 1
    if (initCtr == NUMBEROFSERVERASSIGNERS) {
      if (DEBUG) {
        println("All assigners initialised!")
      }

      // safe to send test tweet  
      if (DEBUG) {
        //val dummy = ac.actorOf(Props(new TwitterClient(ac)), "3")
        //println("path of dummy  is " + dummy.path)
        //dummy ! "test" 
      }
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
      println("All Clients have started ... " + ClientList .mkString("::::"))
      for( coordinator <- ClientList ){
    	coordinator ! "start"
      }
    }
  }

  def routeTweetMessage(sender: ActorRef, tweetText: String) {
    // generate tweet ID, put onto tweet table and pass to worker
    if (DEBUG) {
      println("tweet : " + tweetText)
    }
    tweetIDCtr += 1

    if (DEBUG) {
      println("inside routeTweet of router..ownerOfTweet- " + sender.path.toString().substring(26).toInt)
    }

    val senderpath = sender.path.toString()
    var n = senderpath.lastIndexOf("/");
    var ownerID = senderpath.substring(n + 1).toInt

    // TODO: add a queue to make it fifo

    //tweetTable.put(tweetIDCtr, tweetInfo)

    ServerAssignService(ownerID % NUMBEROFSERVERASSIGNERS) ! tweetReceived(tweetText, ownerID)

  }

  def routeStatusRequest(sender: ActorRef) {

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

    ServerAssignService(senderID % NUMBEROFSERVERASSIGNERS) ! requestStatus(senderID, sender)
    if (DEBUG) {
      println("sent")
    }
  }

}
