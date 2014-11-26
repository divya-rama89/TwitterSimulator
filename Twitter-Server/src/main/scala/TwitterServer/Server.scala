import akka.actor.{ Props, ActorSystem }
import com.typesafe.config.ConfigFactory

object Server {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem("TwitterServer")

    var numUsers: Int = 0
    var numCli: Int = 0
    if (args.length > 1) {
      numUsers = (args(0).toInt)
      numCli = args(1).toInt

      val ServerRouterService = system.actorOf(Props(new ServerRouter(numUsers, numCli, system)), "ServerRouter")
      println("path is " + ServerRouterService.path)
      ServerRouterService ! "Init"
    } else {
      println("Usage : Server.scala <Number of Users> <Number of Clients>")
    }
  }
}
