---
cloud-install script
---

### running the script
This script is meant to run on a unix system and has only been tested on Mac OSX


or:

using leiningen run:
```
lein install-stack <<install directory>> <<accumulo_instance_name>> <<accumulo_root_password>>
```

Sometimes it locks up while formating the name node.  If that happens, delete the directory you are installing into and run the script again.

 **Note:**  You will also probably have to add your username and machine hostname to your /etc/hosts file such that hadoop will work properly.  The following is an example:
127.0.0.1   localhost Vincent-Mac.local vincent-mac.home vrussell
