/*
 * Common code for twitter clients 
 */

import akka.actor.ActorSystem
import akka.actor.Props

object Client {

  def main(args: Array[String]) {
    var system = ActorSystem("TwitterClient")

    if (args.length > 2) {
      println("Starting client with ClientID = " + args(2))
      var totalUsers: Long = args(0).toLong
      var numClient: Int = args(1).toInt
      var clientID: Int = args(2).toInt
      var numUsers: Long = totalUsers / numClient
      println("numUsers = " + numUsers)
      if (clientID + 1 == numClient) {
        for (i: Int <- (clientID * numUsers + 1).toInt to totalUsers.toInt) {
          var actor = system.actorOf(Props(new ClientWorker(i.toLong, totalUsers)), name = i.toString)
        }
      } else {
        for (i: Int <- (clientID * numUsers + 1).toInt to (numUsers * (clientID + 1)).toInt) {
          var actor = system.actorOf(Props(new ClientWorker(i.toLong, totalUsers)), name = i.toString)
        }
      }

    } else {
      println("Usage : Client <Number of Users> <Number of Client Machines> <ClientID>")
    }
  }
}