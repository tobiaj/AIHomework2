package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SubscriptionInitiator;
import jade.proto.states.MsgReceiver;
import jade.util.leap.ArrayList;
import userAndArtifacts.Artifacts;
import userAndArtifacts.User;

import java.io.IOException;

/**
 * Created by tobiaj on 2016-11-09.
 */
public class ProfilerAgents extends SuperAgent{
    private User user;
    private ArrayList tourGuides;
    private ArrayList iDsOfArtifacts;
    private ArrayList listOfArtifactsWithInformation;

    @Override
    protected void setup() {
        super.setup();
        System.out.println("The Profiler guide agent " + getLocalName() + " has started");

        ParallelBehaviour paralell = new ParallelBehaviour();

        paralell.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                createSubscription();

            }
        });

        paralell.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                user = new User();
                System.out.println("The user wanting to create a virtual tour is,  \n");

                System.out.println("User: " + user.getName() + " " + user.getAge() + " " + user.getGender() + " " + user.getOccupation()
                        + " " + user.getInterest() + " " + user.getYearInterest());
            }
        });

        addBehaviour(paralell);

        MessageTemplate messageTemplate = MessageTemplate.MatchOntology("reply");

        ReplyReceiver replyReceiver = new ReplyReceiver(this, messageTemplate, Long.MAX_VALUE, null, null);

        addBehaviour(replyReceiver);

    }

    private void createSubscription() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("TourGuideAgent");
        SearchConstraints search = new SearchConstraints();

        template.addServices(serviceDescription);

        Subscribe subscribe = new Subscribe(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, search));

        addBehaviour(subscribe);

    }

    private void requestATour() {
        System.out.println("Profiler agent " + this.getLocalName() + " is creating a request message to the tour guide");

        ACLMessage requestATourGuideMessage = new ACLMessage(ACLMessage.REQUEST);
        requestATourGuideMessage.addReceiver((AID) tourGuides.get(0));//BEHÖVER FIXAS ?????
        try {
            requestATourGuideMessage.setContentObject(user);//This should be the user.
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestATourGuideMessage.setOntology("tour");//Message that you match on in tourGuide


        TickerBehaviour messageSpam = new TickerBehaviour(ProfilerAgents.this, 10000) {
            @Override
            protected void onTick() {
                send(requestATourGuideMessage);
            }
        };

        addBehaviour(messageSpam);

    }

    private void addTourGuide(DFAgentDescription[] result) {
        tourGuides = new ArrayList();
        for (int i = 0; i < result.length ; i++){
            tourGuides.add(result[i].getName());
        }
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
                    addTourGuide(result);
                    requestATour();
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    public class ReplyReceiver extends MsgReceiver{

        public ReplyReceiver(Agent myAgent, MessageTemplate messageTemplate, long deadline, DataStore ds, Object msgKey) {
            super(myAgent, messageTemplate, deadline, ds, msgKey);

        }

        @Override
        protected void handleMessage(ACLMessage message) {
            System.out.println("The profiler agent " + myAgent.getLocalName() + " received a reply from tour agent with a virtual tour \n");
            try {
                iDsOfArtifacts = (ArrayList) message.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            AID AID = getCuratorAID(ProfilerAgents.this);

            System.out.println("Creating a ACL to request information about each artifacts in the tour");
            ACLMessage requestToCurator = new ACLMessage(ACLMessage.REQUEST);
            requestToCurator.setOntology("artifactsInfo");
            requestToCurator.addReceiver(AID);
            try {
                requestToCurator.setContentObject(iDsOfArtifacts);
            } catch (IOException e) {
                e.printStackTrace();
            }

            GetArtifacts getArtifacts = new GetArtifacts(ProfilerAgents.this, requestToCurator);

            addBehaviour(getArtifacts);
        }

        @Override
        public int onEnd() {
            myAgent.addBehaviour(this);
            return super.onEnd();
        }
    }


    public class GetArtifacts extends SimpleAchieveREInitiator {

        public GetArtifacts(Agent agent, ACLMessage requestToCurator) {
            super(agent, requestToCurator);
        }

        @Override
        protected ACLMessage prepareRequest(ACLMessage msg) {
            return super.prepareRequest(msg);
        }

        @Override
        protected void handleInform(ACLMessage message){
            super.handleInform(message);
            System.out.println("The profiler agent " + myAgent.getLocalName() + " received a reply from curator agent with information about the artifacts \n");

            try {
                listOfArtifactsWithInformation = (ArrayList) message.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            printInformationAboutEachArtifact(listOfArtifactsWithInformation);

        }

        private void printInformationAboutEachArtifact(ArrayList listOfArtifactsWithInformation) {
            for (int i = 0; i < listOfArtifactsWithInformation.size(); i++) {
                Artifacts artifact = (Artifacts) listOfArtifactsWithInformation.get(i);
                System.out.println("Artifact: " + artifact.getId() + " " + artifact.getName()  + " " + artifact.getCreator()  + " " +
                artifact.getGenre()  + " " + artifact.getPlaceOfCreation() + " " + artifact.getDateOfCreation() + "\n");
            }
        }
    }
}
