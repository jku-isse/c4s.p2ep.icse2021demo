ECHO reset token
cmd < reset_token.txt

ECHO try to start axon server
cd C:\Data\dev\axonquickstart-4.4.1\AxonServer
rmdir /s "./data"
java -jar axonserver-4.4.1.jar