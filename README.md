# Akka app
This is a toy project in which I explore Akka.

Run `app.singlenode.Main` or `app.cluster.ClusterMain`.  
To use the app:  
`curl -v -X POST http://localhost:5000/accounts/demo/NL1234`  
`curl -v -X POST http://localhost:5000/accounts/create/ -d "NL1234`  
`curl -v -X POST http://localhost:5000/accounts/processtransaction/ -d '{"transactionId":"1234","amount":300.5,"accountNumber":"NL1234","time":1526724383307}' -H "Content-Type: application/json"`  
`curl -v -X GET http://localhost:5000/accounts/NL1234/transactiontotal/monthly`

## Notes
- What should be a method of an actor and what should be an actor in it self?
- Seems more reasonable to return a generic "failure" message to the router rather than having a failure-case for each message 

- Actor supervision: default Strategy is OK
- decoupling routes / endpoints through HoF of service logic

## Operations
`cd ansible`  
Check that raspberry pis are reachable: `./pingrps.sh`  
Run deployment playbook: `ansible-playbook -i inventory deploy.yml -k`

## Issues encountered
- code would fail at runtime claiming that "reference.conf" cannot be found in the akka-http jar. Fix: rename actor system and recompile
- Testing: Within a suite, the testActor is stateful (if you use the `ImplicitSender` trait). The `expectMsg` function just de-queues the next message from the testActor. Hence, if you do not call this function in other tests, then later tests in the suite will de-queue messages from earlier tests. Fix: expect all messages all the time (1 to 1 relation of messages and `expectMsg`) or use `org.scalatest.OneInstancePerTest` in the suite (creates new actor system for each test).
- error reading config for remote specification ((run-main-0) java.lang.ClassNotFoundException: scala.Int). Fix: update sbt 1.1.6
- cluster: Not quite clear wrt. to testing that the multi-jvm plugins is required (?)