package Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.SubscriptionInitiator;
import jade.proto.states.MsgReceiver;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import userAndArtifacts.Artifacts;
import userAndArtifacts.User;

import java.io.IOException;

/**
 * Created by tobiaj on 2016-11-09.
 */
public class CuratorAgent extends SuperAgent {
    private java.util.HashMap<Integer, Artifacts> listOfArtifacts;


    public void setup() {
        super.setup();
        System.out.println("The Curator agent " + getLocalName() + " has started");

        String service = "bidder";

        registerService(this, service);

        MessageTemplate messageTemplate = MessageTemplate.MatchOntology("Inform");

        MessageReceiver messageReceiver = new MessageReceiver(this, messageTemplate, Long.MAX_VALUE, null, null);
        addBehaviour(messageReceiver);


    }

    public class MessageReceiver extends MsgReceiver {
        public MessageReceiver(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            super.handleMessage(msg);
            Auction auction = new Auction();
            addBehaviour(auction);
        }


    }

    public class Auction extends Behaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                System.out.println("Meddelandet som Curator tog emot: " + msg.getContent());
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }

    private void createListOfArtifacts() {

        listOfArtifacts = new java.util.HashMap<>();
        for (int i = 0; i < 20; i++) {
            Artifacts artifact = new Artifacts();
            listOfArtifacts.put(artifact.getId(), artifact);
        }
    }

}
