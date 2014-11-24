import akka.actor._
import akka.actor.Actor
import akka.actor.ActorSystem
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.math.BigInt



case class tweet(tweetText : String)
case class readStatus()
case class sendMeTweets()

class ServerRouter(numUsers: Int, ac: ActorSystem)  extends Actor{

  val ServerAssignService:ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
  
  // followerTable: userID, follower ID
  var followerTable = scala.collection.mutable.HashMap.empty[Int,ListBuffer[Int]]
  // tweetTable : tweetID, text and owner's userID
  var tweetTable = scala.collection.mutable.HashMap.empty[Int,Tuple2[Int,String]]
  var tweetIDCtr : Int = 0
 
  // constants
  val NUMBEROFSERVERASSIGNERS = 4 
  val TWEETLIMIT = Int.MaxValue 
  
  // Will receive two types of messages from client 
  // Tweet message : senderNodeId. tweet
  // Read status : senderNodeId
    
  // Round Robin assign message
  
  def receive = {
    case "Init" => init() //generate actors + users/followers table
    case tweet(tweetText: String) => routeTweetMessage(sender,tweetText)
    case readStatus() => routeStatusRequest(sender)
    case sendMeTweets() => sendingTweets(sender)
  }
  
  
  def init() { 
    var x:Int = 1
    var y:Int = 0
    var rnd = new scala.util.Random
    
    for(x <- 1 to numUsers) {
      // simulate twitter statistics for followers
      // call function returnFollowers()
      var quantity = rnd.nextInt(10)
      var seq: ListBuffer[Int] = new ListBuffer
      
      for(y <- 0 to quantity) {
        var randomNumber = rnd.nextInt(numUsers)
        while((randomNumber < 0) || (seq.contains(randomNumber))) {
        randomNumber = rnd.nextInt(numUsers)
       }
      seq += randomNumber
      
      }  
      seq += x
      
      // DEBUG
      /*
      var z = 0
     for(z<-0 to seq.length-1) {
       println(seq(z))
     } 
      */
      // fill up hash map
      followerTable.put(x, seq)
   }
    
       // DEBUG
      val dummy = ac.actorOf(Props(new TwitterClient(ac)), "3")
      println("path of dummy  is " + dummy.path)
      dummy ! "test" 
    
    // Create 4 ServerAssigners
    for(x <- 0 to NUMBEROFSERVERASSIGNERS-1) {
    	ServerAssignService += ac.actorOf(Props(new ServerAssigner(followerTable, ac)), "ServerAssigner"+x)
    	println("path is " + ServerAssignService(x).path)		
    }
  }
  
  
  def routeTweetMessage(sender:ActorRef, tweetText:String) {
	  // generate tweet ID, put onto tweet table and pass to worker
    println(tweetText)
    tweetIDCtr += 1
    println("inside routeTweet "+ sender.path.toString().substring(26).toInt)
    
    val ownerID = (sender.path.toString).substring(26).toInt
    var tweetInfo = new Tuple2(ownerID, tweetText) 
    
    // TODO: add a queue to make it fifo
    if(tweetIDCtr < TWEETLIMIT) {
    tweetTable.put(tweetIDCtr, tweetInfo)
    }
  
    followerTable(ownerID);
    println(followerTable .mkString(","))
    
    var retrievedFollowers:ListBuffer[Int] = followerTable(ownerID)
    println("retrievedFollowers for given ownerID "+ownerID)   
    //retrievedFollowers.foreach { i =>println(i)
      println(retrievedFollowers.mkString(","))
    
    var partitionedFollowers = retrievedFollowers.groupBy(x=>x % NUMBEROFSERVERASSIGNERS)
    println("partitioned followers for given ownerID "+ownerID)
      partitionedFollowers.foreach { i => println(i) }
    
    // Send the related assigner/worker
    
    for(x <- 0 to NUMBEROFSERVERASSIGNERS-1) {
    	if(((partitionedFollowers.get(x % (NUMBEROFSERVERASSIGNERS))).size != 0)) {
    		ServerAssignService(x % (NUMBEROFSERVERASSIGNERS)) !  updateTimeline(tweetIDCtr, ownerID, partitionedFollowers(x % (NUMBEROFSERVERASSIGNERS)))			
    	}
    }   
  }

  def routeStatusRequest(sender:ActorRef) {
    
    var senderID = sender.path.toString().substring(26).toInt 
    
    // check whether to send sender or senderID
    ServerAssignService(senderID % NUMBEROFSERVERASSIGNERS) ! requestStatus(senderID)
  }
  
  def sendingTweets(sender: ActorRef) {
    sender ! tweetTableSend(tweetTable)
  }
  
}
