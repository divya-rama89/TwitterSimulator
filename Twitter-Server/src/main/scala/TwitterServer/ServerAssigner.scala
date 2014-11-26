
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Queue
import scala.collection.mutable.ArrayBuffer

case class tweetReceived(tweetText: String, ownerID: Int)
case class updateTimeline(tweetText: String, ownerID: Int, followerSubList: ListBuffer[Int])
case class requestStatus(requestorID: Int, senderActor: ActorRef)
case class followersListSend(user: Int, followersList: ListBuffer[Int])
case class tweetTableSend(tweetTableLatest: HashMap[Int, Tuple2[Int, String]])
case class AssignersListSend(ServerAssignService : ArrayBuffer[ActorRef])

class ServerAssigner(numUsers: Int, ac: ActorSystem, myID:Int, NUMBEROFSERVERASSIGNERS:Int, FOLLOWERSLIMIT:Int) extends Actor {

  var tweetTable = HashMap.empty[Int, Tuple2[Int, String]]
  var pagesTable = HashMap.empty[Int, Queue[Tuple2[String, Int]]]
  var followersTable = HashMap.empty[Int,ListBuffer[Int]]
  var myServerAssignList = new ArrayBuffer[ActorRef]
  
  // constants
  val DEBUG = false
  
  // TODO
  // schedule to request for tweets and act on them
  // sendMeTweets

  def receive = {
    case "init" => init(sender)
    case "begin" => println("Initialisation done...")
    case updateTimeline(tweetText: String, ownerID: Int, followerSubList: ListBuffer[Int]) => updateTweetQueue(tweetText: String, ownerID: Int, followerSubList: ListBuffer[Int])
    case requestStatus(requestorID: Int, senderActor: ActorRef) => returnQueue(requestorID: Int, senderActor: ActorRef)
    case tweetTableSend(tweetTableLatest: HashMap[Int, Tuple2[Int, String]]) => updateTweetTable(tweetTableLatest: HashMap[Int, Tuple2[Int, String]])
    case followersListSend(user: Int, followersList: ListBuffer[Int]) => updateFollowersTable(user: Int, followersList: ListBuffer[Int])
    case AssignersListSend(serverAssignService : ArrayBuffer[ActorRef]) => buildServerAssignRef(serverAssignService:ArrayBuffer[ActorRef])
    case tweetReceived(tweetText: String, ownerID: Int) => sendAllFollowersInfo(tweetText: String, ownerID: Int)
  }

  def init(router: ActorRef) {
    var x:Int = 0
    var y:Int = 0
    var rnd = new scala.util.Random
   
    // followerTable: userID, follower ID
    var followerTable : ListBuffer[ListBuffer[Int]] = new ListBuffer
    
    for(x <- 0 to numUsers-1) {

      if((x % NUMBEROFSERVERASSIGNERS) == myID) {
      // simulate twitter statistics for followers
      // call function returnFollowers()
      var quantity = rnd.nextInt(FOLLOWERSLIMIT)
      var seq: ListBuffer[Int] = new ListBuffer
      
      
      for(y <- 0 to quantity) {
        var randomNumber = rnd.nextInt(numUsers-1)
        while((randomNumber < 0) || (seq.contains(randomNumber))) {
        randomNumber = rnd.nextInt(numUsers)
       }
      seq += randomNumber
      
      }  
      if(!seq.contains(x)){
    	  seq += x
      }
      
      if(DEBUG) {
      println("for owner : "+x)
      var z = 0
      for(z <- 0 to seq.length) {
      print(" "+z)  
      }
      }
      updateFollowersTable(x, seq)
      }
   }
    if(DEBUG) {
    println("Followers table")
      println(followersTable.keys + " " +followersTable.values)
    }
    router ! "done"
  }
  
  def sendAllFollowersInfo(tweetText: String, ownerID: Int) {
    
    var retrievedFollowers:ListBuffer[Int] = followersTable.getOrElse(ownerID, null)
    
    if(true) {
    	if(retrievedFollowers != null) {
    		println("retrievedFollowers for given ownerID "+ownerID)   
    		//retrievedFollowers.foreach { i =>println(i)
    		println(retrievedFollowers.mkString(","))
    	}
    }
    
    var partitionedFollowers = retrievedFollowers.groupBy(x=>x % NUMBEROFSERVERASSIGNERS)
    
    if(true) {
    	println("partitioned followers for given ownerID "+ownerID)
    	partitionedFollowers.foreach { i => println(i) }
    }
    
    // Send the related assigner/worker
    
    for(x <- 0 to NUMBEROFSERVERASSIGNERS-1) {
    	var temp = partitionedFollowers.getOrElse((x),null)
    	
    	if (temp!= null) {
    	  if(DEBUG){
    		  println(temp)
    	  }  
    	  
    	myServerAssignList(x) !  updateTimeline(tweetText, ownerID, temp)			
    	}
    }  
    
  }
  
  def updateTweetQueue(tweetText: String, ownerID: Int, followerSubList: ListBuffer[Int]) {
    //update queue for each user in followerSubList
    //println("I received this follower list : " + followerSubList)
    // for every member of the followerSubList, add new tweet to the queue
    
    var newMember = new Tuple2(tweetText, ownerID)
    for (member <- followerSubList) {

      //get queue from pagesTable
      var tweetsQ = pagesTable.getOrElse(member, null)

      if (tweetsQ == null) {
        // Add new entry to queue
        tweetsQ = Queue(newMember);

      } else {
        tweetsQ.enqueue(newMember)

        // Limit queue length to 100
        if (tweetsQ.length > 100) {
          tweetsQ.dequeue
        }

      }
      // insert back into hashmap
      pagesTable.put(member, tweetsQ)
      
      if(DEBUG)
      {
        println("="*20)
        println("inserted text: " + newMember+" into timeline of :" + member)
        println("="*20)
      }
    }
  }
  

 def returnQueue(requestorID: Int, senderActor: ActorRef) {
    //return the queue for requestorID
    if(true){
      println("#"*200)
      println("inside returnQueue")
    }
   
   var requiredQ = pagesTable.getOrElse(requestorID, null)

    if (false) {
      if (requiredQ != null) {
        println("requiredQ")
        println(requiredQ.mkString("\n"))
      }
    }
    
    if (requiredQ != null) {
      
      senderActor ! (requiredQ)
    }
  }

  def updateTweetTable(tweetTableLatest: HashMap[Int, Tuple2[Int, String]]) {
    if (!(tweetTable.equals(tweetTableLatest)) && !(tweetTableLatest.isEmpty)) {
      tweetTable = tweetTableLatest.clone
    }
  }
  
  def updateFollowersTable(user: Int, followersList: ListBuffer[Int]) {
    if (!(followersList.isEmpty)) { 
    	followersTable.put(user,followersList)
    }
  }
  
  def buildServerAssignRef(serverAssignList:ArrayBuffer[ActorRef]) {
    if (!(serverAssignList.isEmpty)) { 
    	myServerAssignList = serverAssignList.clone
    }
    if(DEBUG){
      println("="*20)
      println("myServerAssignList")
      myServerAssignList.foreach {
        i => println(i)
      }
      println("="*20)
    }
  }

}
