package Agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.SubscriptionInitiator;
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


        createSubscription();


    }

    private void createListOfArtifacts() {

        listOfArtifacts = new java.util.HashMap<>();
        for (int i = 0; i < 20; i++) {
            Artifacts artifact = new Artifacts();
            listOfArtifacts.put(artifact.getId(), artifact);
        }
    }

    private void createSubscription() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Auction");
        SearchConstraints search = new SearchConstraints();

        template.addServices(serviceDescription);

        Subscribe subscribe = new Subscribe(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, search));

        addBehaviour(subscribe);

    }

    public class Subscribe extends SubscriptionInitiator {

        public Subscribe(Agent agent, ACLMessage message){
            super(agent, message);
        }

        protected void handleInform(ACLMessage inform){
            try {
                DFAgentDescription[] result = DFService.decodeNotification(inform.getContent());
                if (result.length > 0) {
                    System.out.println("Profiler agent " + getLocalName() + " received a subscription message from SuperAgent with name " + getDefaultDF());

                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }


}
