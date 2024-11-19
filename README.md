# Java Proxy Fix

This is a Java agent that remaps all calls to `java.net.URL.openConnection(java.net.Proxy proxy)` to `java.net.URL.openConnection()`, meaning that `URL.openConnection` will always ignore the proxy supplied to it and use the system proxy instead (e.g. the proxy specified by the `http.proxyHost` and `https.proxyHost` system properties.

This is useful for MITMing communication between Minecraft servers and Minecraft authentication servers (such as Drasl). Minecraft often calls `URL.openConnection(Proxy.NO_PROXY)` when communicating with authentication servers (see usages of `YggdrasilAuthenticationService` in the Minecraft source). So we can use this Java agent to get the Minecraft server to respect `-Dhttps.proxyHost` and inspect the traffic using mitmproxy or similar.

I could have sworn I'd seen something like this before, but I couldn't find it, so I wrote another one with some help from the LLM.

## Example usage

```
java -Xmx512M -Xms512M \
	-Dminecraft.api.env=custom \
	-Dminecraft.api.auth.host=https://drasl.unmojang.org/auth \
	-Dminecraft.api.account.host=https://drasl.unmojang.org/account \
	-Dminecraft.api.session.host=https://drasl.unmojang.org/session \
	-Dminecraft.api.services.host=https://drasl.unmojang.org/services \
	-Djavax.net.ssl.trustStore=/home/user/cacerts -Djavax.net.ssl.trustStorePassword=changeit -Dhttp.proxyHost=localhost -Dhttp.proxyPort=8080 -Dhttps.proxyHost=localhost -Dhttps.proxyPort=8080 \
	-javaagent:ProxyFix-1.0-SNAPSHOT-jar-with-dependencies.jar \
	-jar server.jar nogui
```

See also https://github.com/unmojang/drasl/blob/master/doc/troubleshooting.md.
