An implementation of Farsite DFS. Did as a part of my UG summer research project in IITM.. The code from circa AD 2009 is several years old. Probably a lot of the Java packages used are deprecated now. 

There are two packages - dos.common and dos.farsite ( dos stands for distributed object systems )
The dos.common package defines a standard library for creating a distributed file system. The dos.farsite package can be viewed as an example implementation using dos.common.

I am not proud of some sections of the code. I am proud of a few others. In all, it was my very first 'real' project and I am happy I did it.

How To Run
===========

The bin folder contains all the classes. The best thing to do is to import this as 
an eclipse project. Compilation is done automatically by eclipse and we just need to run 
from the bin folder

	<> - denotes compulsory arguments
	[] - denotes optional arguments

Go to bin folder

Starting nameserver:

	./myjava dos.dfs.farsite.Driver server start <cluster root namespace> <intra cluster id> [nameserver listening port] [intra cluster consistency port]

	Eg : ./myjava dos.dfs.farsite.Driver server start farsite/root1 000

Intra cluster id is used to give the nameserver a unique id for intra cluster communication. This should be a string of fixed length( since it is used in paxos too )
The optional arguments are server listening port and intra cluster communication port.
We can set these if we don't want to used default ports. This is useful to run multiple servers
from a single machine

	./myjava dos.dfs.farsite.Driver server start standby [nameserver listening port] [intra cluster consistency port]


- The nameserver starts in standby mode waiting for some cluster to delegate responsibility to it

Starting filehost

	./myjava dos.dfs.farsite.Driver filehost start [fileHostPort] [fileHostListenerPort] [block Storage path]

Starting client

	./myjava dos.dfs.farsite.Driver client start interactive

 starts in interactive mode printing command prompt

	./myjava dos.dfs.farsite.Driver client start test <testFilePath>

runs a test specified by testFilePath
The testFile path should just list a series of commands as you would give from the command line itself

Commands:
=========

In all the commands to follow the entire dfsFilePath should be used. Relative naming has not been done. But can be done easily

	mkdir <dfsFilePath>
	ls <dfsResource>
	put <localFilePath> <dfsFileName> (dfsFilePath should contain name also. Just specifying directory won't do)
	get <dfsFileName> <localFileName>

Local folders to be created:

	/tmp/farsite
	/tmp/farsite/blocks - blocks are stored by file host here
	/tmp/farsite/resources
	/tmp/farsite/data
	/tmp/paxos

The foll files should exists in /tmp/farsite/resources
In all the files to follow the address should be in the form of an ip:port pair. Port is included because this helps us to test this 
dfs easily on a single machine.

1. namespace_map - contains the mapping of clusterRoots to the responsible addresses

2. file_hosts - list of file hosts (with their listening port addresses (where they listen for file requests) not the file transfer address (which they use to get files actually) )
3. cluster_mates_farsite_root1 - a file containing the list of all servers taking care of farsite/root1
							  Name the file this way for whatever cluster this node is running and include all the cluster nodes in this file
							  Addresses should contain ip and cluster consistency port 
4. hosts						-	List of all potential servers that could be asked to 
								become name servers in future. These should all be running in standby mode

