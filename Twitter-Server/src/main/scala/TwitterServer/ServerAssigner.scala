import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Queue

case class updateTimeline(tweetID: Int, ownerID: Int, followerSubList: ListBuffer[Int])
case class requestStatus(requestorID: Int)
case class tweetTableSend(tweetTableLatest: HashMap[Int, Tuple2[Int, String]])

class ServerAssigner(followersTable: HashMap[Int, ListBuffer[Int]], ac: ActorSystem) extends Actor {

  var tweetTable = HashMap.empty[Int, Tuple2[Int, String]]
  var pagesTable = HashMap.empty[Int, Queue[Tuple2[Int, Int]]]

  // TODO
  // schedule to request for tweets and act on them
  // sendMeTweets

  def receive = {
    case "tweet" => println("tweet")
    case updateTimeline(tweetID: Int, ownerID: Int, followerSubList: ListBuffer[Int]) => updateTweetQueue(tweetID: Int, ownerID: Int, followerSubList: ListBuffer[Int])
    case requestStatus(requestorID: Int) => returnQueue(requestorID: Int)
    case tweetTableSend(tweetTableLatest: HashMap[Int, Tuple2[Int, String]]) => updateTweetTable(tweetTableLatest: HashMap[Int, Tuple2[Int, String]])
  }

  def updateTweetQueue(tweetID: Int, ownerID: Int, followerSubList: ListBuffer[Int]) {
    //update queue for each user in followerSubList
    println("I received data : " + followerSubList)
    // for every member of the followerSubList, add new tweet to the queue
    for (member <- followerSubList) {
      var newMember = new Tuple2(tweetID, ownerID)

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
    }
  }

 def returnQueue(requestorID: Int) {
    //return the queue for requestorID
    var requiredQ = pagesTable.getOrElse(requestorID, null)
    if (requiredQ != null) {
      
      // var recieveBckQueue = context.actorSelection("akka://TwitterServer/user/" + requestorID)
       var recieveBckQueueActorRef=context.actorFor("../"+(requestorID))
       recieveBckQueueActorRef ! receiveBackTwitQueue(requiredQ)
       
       // var x : ActorRef = context.actor("../"+(requestorID)) 
      //x ! receiveBackTwitQueue(requestorID)*/
    }
  }

  def updateTweetTable(tweetTableLatest: HashMap[Int, Tuple2[Int, String]]) {
    if (!(tweetTable.equals(tweetTableLatest)) && !(tweetTableLatest.isEmpty)) {
      tweetTable = tweetTableLatest.clone
    }
  }

}
