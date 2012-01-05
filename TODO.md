1. Collect history log for stats within a circular fifo buffer, to allow clients to fetch. Sort of "tail -f" thing

2. Provide inputs from JMX:
 a. Stats: statistical data to be shown in some graph (flot)
 b. Actions: JMX operations. ie: Start something, Stop, Resume...
 c. Status: on/off? green/red, etc. For Sensei, would map to availability, for Kafka wouldn't make sense? For most, 
   would be just check if JMX is available. For others, could also be an attribute on an MBean.
 d. Settings: Allow to change some JMX attribute.
 
3. Authentication / Authorization:
 - Only authenticated users can enter app
 - User should set up monitors for its profile
 
4. Allow JMX credentials

5. Zookeeper special-monitor, could provide information for the assembly

6. Enhance charts using more Flot features...

7. Setup should be rest, Monitors ajax...

8. Allow changing stats freq while running (ie: statsActor ! 0.5)