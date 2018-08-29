## SSH

You can use ssh lib to connect with an ssh server and execute remote commands. 
The lib extends [sshj](https://github.com/hierynomus/sshj/releases/tag/v0.23.0) and gives a higher level API.

## Features
  - connect to remote SSH servers even if they're in progress of starting up
  - execute remote ssh commands,
  - print error and output messages to logs,
  - fetch output of the command,
  - create and work with detached processes,
  - download files from the remote server,
 
## Requirements
  - JRE 8 - 11
  - running SSH server running on port 22
  - private key, username, and IP that can be used to connect to the remote server


### Version and release management
This module uses [gradle-release](https://bitbucket.org/atlassian/gradle-release/src/master/) plugin help with 
version and release management. 
Please refer to [plugin docs](https://bitbucket.org/atlassian/gradle-release/src/release-0.3.0/README.md) for more information.


##License
Copyright (c) 2018 Atlassian and others.
Apache 2.0 licensed, see [LICENSE.txt](LICENSE.txt) file.


