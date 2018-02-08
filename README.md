**Blocking Queue Service**

You task is to create application with single publisher and several consumers consuming messages
from the queue. 

Application will read message from console and publish it in the queue. Consumer should print 
message together with current thread information. 

Use must write your own implementation of java.util.concurrent.BlockingQueue and ua.danit.queue
.PubSubApplication.

Solution should supplied with next tasks:
<ol>
   <li>Concurrent queue implentation using monitors (synchronized) </li>
   <li>Blocking queue implentation using java build-in monitors (wait, notify)</li>
   <li>Blocking queue implentation using java.util.concurrent.locks.Lock, 
   java.util.concurrent.locks.Condition</li>
   <li>Consumers should run using thread pool.Implementation of consumer should 1-1 as 
   thread-consumer.</li>   
   <li>(Optional) Blocking queue "two-way lock" implentation using java.util.concurrent.locks.Lock, 
   java.util.concurrent.locks.Condition</li>
   <li>(Optional) Add possibility to run consumers sa N-M: N theads per M consumers. Use poll 
   method with timeout.</li>
</ol>

You should write also tests for queues. Implementation should satisfy tests provided: 
LockQueueTest, SingleBlockingQueueTest, SynchronyzedBlockingQueueTest, SynronizedQueueTest.

After implemented both queues ConcurrentLockQueue and SingleLockBlockingQueue set phase of maven
plugin 'exec-maven-plugin' to 'verify' and verify both stress tests :

<ol>
  <li>ConcurrentQueueStressTest</li>
  <li>SingleLockQueueStressTest</li>    
</ol>


Application should follow OOP and SOLID principles.
Code should be properly formatted and follow Google code checkstyle conventions. 

Good luck!

