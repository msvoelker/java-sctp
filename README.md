# java-sctp

Short example that uses SCTP in Java. I used the code from [Oracle's SCTP example](http://www.oracle.com/technetwork/articles/javase/index-139946.html).

## Ubuntu

For a first test, I used a Ubuntu 17.10 64 Bit VM, since [OpenJDK officially supports SCTP on Linux](http://openjdk.java.net/projects/sctp/html/gettingstarted.html). I installed OpenJDK and a required libsctp packet.

```
$ sudo apt install openjdk-8-jdk
$ sudo apt install libsctp-dev
```

Now I could compile and run the example (see [ubuntu.pcap](ubuntu.pcap) for a network packet trace).

```
$ javac DaytimeClient.java DaytimeServer.java 
$ java DaytimeServer &
$ java DaytimeClient
New association setup with 10 outbound streams, and 10 inbound streams.
(US) 10:16:26 AM Thu 15 Mar 18, Central European Time
(FR) 10:16:26 AM jeu. 15 mars 18, Heure d'Europe centrale
The association has been shutdown.
```

I did not added SCTP support to the Ubuntu kernel. However, after I started the DaytimeServer, Ubuntu has automatically loaded the SCTP Kernel module.

```
$ cat /proc/modules | grep sctp
sctp 299008 7 - Live 0x0000000000000000
libcrc32c 16384 1 sctp, Live 0x0000000000000000
```

## FreeBSD

For my next test, I used a FreeBSD 12.0-CURRENT amd64 VM. As with Ubuntu, I installed OpenJDK and compiled the code.

```
% sudo pkg install openjdk8
% javac DaytimeClient.java DaytimeServer.java 
```

Unfortunately, running the example on FreeBSD was not that successful.

```
% java DaytimeServer &
[1] 2281
% java DaytimeClient
Exception in thread "main" New association setup with 10 outbound streams, and 10 inbound streams.
java.net.SocketException: Invalid argument
	at sun.nio.ch.sctp.SctpChannelImpl.send0(Native Method)
	at sun.nio.ch.sctp.SctpChannelImpl.sendFromNativeBuffer(SctpChannelImpl.java:1045)
	at sun.nio.ch.sctp.SctpChannelImpl.send(SctpChannelImpl.java:997)
	at sun.nio.ch.sctp.SctpChannelImpl.send(SctpChannelImpl.java:977)
	at DaytimeServer.main(DaytimeServer.java:41)
Exception in thread "main" java.net.SocketException: Connection reset by peer
	at sun.nio.ch.sctp.SctpChannelImpl.receive0(Native Method)
	at sun.nio.ch.sctp.SctpChannelImpl.receiveIntoNativeBuffer(SctpChannelImpl.java:859)
	at sun.nio.ch.sctp.SctpChannelImpl.receive(SctpChannelImpl.java:835)
	at sun.nio.ch.sctp.SctpChannelImpl.receive(SctpChannelImpl.java:778)
	at sun.nio.ch.sctp.SctpChannelImpl.receive(SctpChannelImpl.java:740)
	at DaytimeClient.main(DaytimeClient.java:27)
```

The `truss` utility revealed that the Invalid argument error comes from `sendmsg`.

```
% truss java DaytimeServer &
% java DaytimeClient
...
sendmsg(7,0x7fffdfffd600,0)			 ERR#22 'Invalid argument'
...
```

Since OpenJDK seems to do not support SCTP on FreeBSD, I did not further investigate this issue.
