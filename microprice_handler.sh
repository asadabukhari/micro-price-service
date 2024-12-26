export BLVDR_HOME=/c/blvdr
export CP=.:$BLVDR_HOME/lib/micro-price-service-1.0.jar:$BLVDR_HOME/lib/agrona-1.14.0.jar
#args: multicast group address, port, exchange-A tcp server host and port; last argument: 100000 (optional) for test_only run to generate .dat files for comparison
java -cp $CP com.microprice.MicropriceMDHandler 239.100.0.1 7445 localhost 5001 100000
