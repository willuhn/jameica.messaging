#!/usr/bin/perl

use strict;
use RPC::XML;
use RPC::XML::Client;


my $url = "https://localhost:8080/xmlrpc/";


################################################################################
## Alternativ kann die URL auch mittels Multicast-Lookup
## automatisch ermittelt werden:

#use IO::Socket::Multicast; ## Das Modul gibts bei search.cpan.org
#my $s = IO::Socket::Multicast->new(LocalPort=>6789);
#$s->mcast_add('224.0.0.1');
#$s->mcast_send('jameica.xmlrpc.queue','224.0.0.1:6789');
#do
#{
#  $s->recv($url,1024);
#}
#while ($url eq "jameica.xmlrpc.queue"); # Das ist das Echo von uns selbst
#$s->mcast_drop('224.0.0.1');
################################################################################



my $cli = RPC::XML::Client->new($url);
$cli->credentials("XML-RPC","admin","test");

################################################################################
# Test 1:
# Nachricht senden

my @msg;
push(@msg,RPC::XML::string->new("foo.bar"));   # Name des Channels
push(@msg,RPC::XML::base64->new("Das ist die eigentliche Nachricht")); # Inhalt der Nachricht
push(@msg,RPC::XML::struct->new("filename" => "foobar.doc",
                                "size"     => "300k")); # zusaetzliche Meta-Daten
 
print "Sende Nachricht\n";
my $resp = $cli->send_request("jameica.messaging.connector.xmlrpc.put",@msg);
my $uuid = $resp->value;
print "Empfangene UUID: ".$uuid."\n";
##
################################################################################

################################################################################
# Test 2:
# Nachricht abrufen

my @msg;
push(@msg,RPC::XML::string->new($uuid));   # von Test 1 zurueckgelieferte UUID

print "Rufe Nachricht ab, UUID: ".$uuid."\n";
my $resp = $cli->send_request("jameica.messaging.connector.xmlrpc.get",@msg);
print "Empfangene Nachricht: ".$resp->value."\n";
##
################################################################################

################################################################################
# Test 3:
# Nachricht loeschen

my @msg;
push(@msg,RPC::XML::string->new($uuid));   # von Test 1 zurueckgelieferte UUID

print "Loesche Nachricht, UUID: ".$uuid."\n";
$cli->send_request("jameica.messaging.connector.xmlrpc.delete",@msg);
##
################################################################################

