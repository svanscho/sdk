sdk
===

OpenMotics SDK for communication with the OpenMotics Gateway.


Java
====
The openmotics cloud api uses a CA authority for its certificates which is NOT included in the JDK/JRE default trust store when using a java version < 1.8.0_101. Hence a java based client using an older version of the JDK will not trust the certificates out of the box.

To overcome this limitation this SDK bundles the root certificate as part of the SDK. Another option would be to upgrade to a newer JDK version or install the certificates JDK using the following command:

```bash
wget https://letsencrypt.org/certs/lets-encrypt-x3-cross-signed.der
sudo keytool -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt -importcert -alias lets-encrypt-x3-cross-signed -file lets-encrypt-x3-cross-signed.der``` 
