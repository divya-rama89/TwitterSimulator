TwitterSimulator
================
Team members: Mugdha (UFID:54219168), Palak Shah (UFID:55510961), Divya Ramachandran (UFID:46761308)

A program written as part of Distributed Operating Systems course to design and simulate the Twitter system.

Input :: On the server side: Number of Users, Number of Client Machines
Input :: On the client side: Number of Users, Number of Client Machines, ClientID, Server IP, Server Port
Output :: Number of Tweets and Timeline requests per second are printed and the average value of tweets/sec and requests/second are calculated.
Functionality: This code emulates Twitter. Clients from client machines act as logged in users. They send tweets that are updated on their own and their followers' timelines. When a client requests for its timeline (emulating the backend client requests), the prestored generated timeline maintained by the server for that client is returned.

----------------------------------------------------------------------------------------------------------------------------------------------------------

Instructions {How to run}
----------------------------------------------------------------------------------------------------------------------------------------------------------
Tested for the following versions:
Scala : 2.11.2
Akka : 2.3.6
SBT : 0.13.6

----------------------------------------------------------------------------------------------------------------------------------------------------------
Inside root folder 'TwitterSimulator', copy the folder 'TwitterClient' on client machine(s) and 'Twitter-Server' on the server machine. 
Go to project rootFolder by doing cd TwitterSimulator/<respective client/server foldername>
For example, on client => type cd  TwitterSimulator/TwitterClient
on server => type cd  TwitterSimulator/Twitter-Server

Do the following commands on client and server machines
------------------------------------------------------------------
Open the following file:
src/main/resources/application.conf

In the file, change the ip address(hostname field) and port (port field) for each machine to reflect their own IP address and port.


Now from the initial outer folder (Twitter-Server or TwitterClient) do:

sbt publishLocal

Run command:
On server run this
sbt "runMain Server <Number of Users> <Number of Clients>"

On client run this 
sbt "runMain Client <Number of Users> <Number of Client Machines> <ClientID> <Server IP> <Server Port>"


Note
-----------------------------------------------------------------------------------------------------------------------------------------------------------
Please refer to report document and excel sheet in the project root folder for our test results.


Details:
-----------------------------------------------------------------------------------------------------------------------------------------------------------
1. The code simulates user characteristics observed of Twitter and its basic functionality.
2. The number of tweets and requests and number of followers have been scaled for our system.
3. The largest number of users the code has been tested for is 1000000 users with a single client machine. The average tweets/second observed for these many users was 1917 tweets/second. The reflected results are influenced by number of users, their tweeting frequency, the frequency at which the timeline is requested from the server and the network connecting the server-client machines. 
4. An event (such as a sports event or big news) that leads to a peak in the tweet rate has also been simulated.
