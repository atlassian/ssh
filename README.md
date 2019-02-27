[![Build Status](https://travis-ci.com/atlassian/ssh.svg?branch=master)](https://travis-ci.com/atlassian/ssh)

## SSH
You can use SSH lib to connect with an SSH server and execute remote commands. 
The lib extends [sshj](https://github.com/hierynomus/sshj/releases/tag/v0.23.0) and gives a higher level API.

## Features
  - connect to remote SSH servers even if they're in progress of starting up
  - execute remote ssh commands
  - print error and output messages to logs
  - fetch output of the command
  - create and work with detached processes
  - download files from the remote server
 
## Requirements
  - JRE 8 - 11
  - running SSH server running on port 22
  - private key, username, and IP that can be used to connect to the remote server

## Reporting issues

We track all the changes in a [public issue tracker](https://ecosystem.atlassian.net/secure/RapidBoard.jspa?rapidView=457&projectKey=JPERF).
All the suggestions and bug reports are welcome.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License
Copyright (c) 2018 Atlassian and others.
Apache 2.0 licensed, see [LICENSE.txt](LICENSE.txt) file.
