package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREInitiator;

import java.util.ArrayList;

/**
 * Created by tobiaj on 2016-11-17.
 */
public class ArtistManager extends SuperAgent {

    private static ArrayList<AID> participants;

    public void setup() {
        super.setup();
        System.out.println("The Artist manager agent " + getLocalName() + " has started");

        getAllCuratorAID();
        startAuction();

    }

    public class Auction extends CyclicBehaviour {

        @Override
        public void action() {

            ACLMessage msg = new ACLMessage(ACLMessage.CFP);

            msg.setContent("100");

            participants.forEach(msg::addReceiver);
            send(msg);

            if (msg != null) {
                System.out.println("Meddelandet som Curator tog emot: " + msg.getContent());
            } else {
                block();
            }

        }
    }

    private void startAuction() {
        ACLMessage initiate = new ACLMessage(ACLMessage.INFORM);
        participants.forEach(initiate::addReceiver);
    }

    public void getAllCuratorAID() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("curatorAgent");
        template.addServices(serviceDescription);
        participants = new ArrayList<>();

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                for (DFAgentDescription temp : result)
                    participants.add(temp.getName());
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
