package org.example;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Navigator extends Agent {
    private AID speleologistAgent;

    @Override
    protected void setup() {

        addBehaviour(new WakerBehaviour(this, 2000) {
            private static final long serialVersionUID = 1L;

            protected void handleElapsedTimeout() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("speleologist");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    System.out.println("Found " + result.length + " speleologist agent:");
                    if (result.length > 0) {
                        speleologistAgent = result[0].getName();
                        System.out.println(speleologistAgent.getName());

                        myAgent.addBehaviour(new RequestPerformer());
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }

    private class RequestPerformer extends Behaviour {

        private String location;
        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    System.out.println("Preparing the request for the speleologist location");
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.addReceiver(speleologistAgent);
                    request.setContent("Current location?");
                    request.setConversationId("location-request");
                    request.setReplyWith("request" + System.currentTimeMillis());

                    myAgent.send(request);
                    System.out.println("Request for the speleologist location is sent");
                    mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("location-request"),
                        MessageTemplate.MatchInReplyTo(request.getReplyWith())
                    );
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            location = reply.getContent();
                            System.out.println("Navigator received Location response," +
                                "saved the speleologist location:" + location);
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage decision = new ACLMessage(ACLMessage.PROPOSE);
                    decision.addReceiver(speleologistAgent);
                    decision.setContent("Go to tunnel 1");
                    decision.setConversationId("action-decision");
                    decision.setReplyWith("decision" + System.currentTimeMillis());
                    myAgent.send(decision);
                    System.out.println("Propose with speleologist action decision is sent");

                    mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("action-decision"),
                        MessageTemplate.MatchInReplyTo(decision.getReplyWith()));
                    step = 3;
                    break;
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }
}
