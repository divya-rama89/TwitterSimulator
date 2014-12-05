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
/*
      val ServerRouterService = system.actorOf(Props(new ServerRouter(numUsers, numCli, system)), "ServerRouter")
      println("path is " + ServerRouterService.path)
      ServerRouterService ! "Init"
  */  
    // create 4 server routers
    // can create a macro and make this configurable
      var i = 0
      
      for(i <- 0 to 3) {
      var ServerRouterService = system.actorOf(Props(new ServerRouter(numUsers, numCli, system)), "ServerRouter"+i)
      println("path is " + ServerRouterService.path)
      if(i == 0) ServerRouterService ! "Init"
    //  ServerRouterService ! "init2"
      }
    } else {
      println("Usage : Server.scala <Number of Users> <Number of Clients>")
    }
  }
}
