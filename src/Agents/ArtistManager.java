package Agents;

/**
 * Created by tobiaj on 2016-11-17.
 */
public class ArtistManager extends SuperAgent{

    public void setup(){
        super.setup();
        System.out.println("The Artist manager agent " + getLocalName() + " has started");

        String service = "Auction";
        registerService(this, service);


    }
}
