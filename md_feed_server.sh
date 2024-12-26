export BLVDR_HOME=/c/blvdr
export CP=.:$BLVDR_HOME/lib/micro-price-service-1.0.jar:$BLVDR_HOME/lib/agrona-1.14.0.jar
#args: multicast group address, port, network-interface, 100000 (optional) for test_only run to generate .dat files for comparison
java -cp $CP com.microprice.MarketDataFeedServer 239.100.0.1 7445 NetworkInterface 100000
