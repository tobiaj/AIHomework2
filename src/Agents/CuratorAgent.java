package Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import userAndArtifacts.Artifacts;

import java.util.Random;

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

        MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate messageTemplateWinner = MessageTemplate.MatchOntology("Winner");


        MessageReceiver messageReceiver = new MessageReceiver(this, messageTemplate, Long.MAX_VALUE, null, null);
        WinnerMessage winnerMessage = new WinnerMessage(this, messageTemplateWinner, Long.MAX_VALUE, null, null);

        addBehaviour(winnerMessage);
        addBehaviour(messageReceiver);

    }

    public class MessageReceiver extends MsgReceiver {
        public MessageReceiver(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            super.handleMessage(msg);
            System.out.println("Kommer inform message till curator, message content: " + msg.getContent());
            Auction auction = new Auction();
            addBehaviour(auction);
        }


    }

    public class WinnerMessage extends MsgReceiver {
        public WinnerMessage(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            super.handleMessage(msg);
            System.out.println("Jag vann : " + myAgent.getLocalName());
        }

        @Override
        public void reset() {
            super.reset();
        }
    }

    public class Auction extends Behaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.blockingReceive(mt);

            int rand = new Random().nextInt(100+1);

            if (msg != null) {
                System.out.println("Meddelandet som Curator: " + getLocalName() + " tog emot: " + msg.getContent());
                ACLMessage reply = msg.createReply();
                reply.setContent("Skicka tillbaka från curator: " + getLocalName());
                if (rand > 90)
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                else
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);

                send(reply);
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }


}
