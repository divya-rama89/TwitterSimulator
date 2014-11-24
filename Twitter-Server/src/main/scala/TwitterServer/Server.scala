import akka.actor.{Props, ActorSystem}


object Server {
def main(args: Array[String]): Unit = {
     
     // 2 args : numUsers
     val numUsers = (args(0).toInt)
     
     println("args are "+ numUsers)
     
     val system = ActorSystem("TwitterServer")

     val ServerRouterService = system.actorOf(Props(new ServerRouter(numUsers, system)), "ServerRouter")
     println("path is " + ServerRouterService.path)
     ServerRouterService ! "Init"
     ServerRouterService ! "test"
  
    
     
   }
}
