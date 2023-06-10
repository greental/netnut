package org.loadbalancer;

import org.application.Main;
import org.client.Client;
import org.client.IClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Main.class)
public class LoadBalancerTests {


    Client clientAustin = new Client("United States", "Texas", "Austin");
    Client clientDallas = new Client("United States", "Texas", "Dallas");
    Client clientHuston = new Client("United States", "Texas", "Huston");
    Client clientLima = new Client("Peru", "Lima", "Lima");
    Client clientHoboken = new Client("United States", "New Jersey", "Hoboken");

    @Autowired
    LoadBalancer loadBalancer;

    @BeforeEach
    public void addData(){
        loadBalancer.reset();
        loadBalancer.addClient(clientAustin);
        loadBalancer.addClient(clientDallas);
        loadBalancer.addClient(clientHuston);
        loadBalancer.addClient(clientLima);
        loadBalancer.addClient(clientHoboken);
    }

    @Test
    public void selectClientFull(){
        String filter = "United States-Texas-Austin";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        assertEquals(foundClient, clientAustin);
    }

    @Test
    public void removeClient(){
        loadBalancer.removeClient(clientAustin);
        String filter = "United States-Texas-Austin";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        assertNull(foundClient);
    }

    @Test
    public void selectWildcard_Wildcard_City(){
        String filter = "*-*-Dallas";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        assertEquals(clientDallas,foundClient);
    }
    @Test
    public void selectWildcard_State_Wildcard(){
        String filter = "*-Texas-*";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);

        HashSet<IClient> texasSet = new HashSet<>();
        texasSet.add(clientAustin);
        texasSet.add(clientHuston);
        texasSet.add(clientDallas);

        assert(texasSet.contains(foundClient));

    }

    @Test
    public void selectCountry_Wildcard_City(){
        String filter = "United States-*-Huston";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        assertEquals(foundClient, clientHuston);

    }
    @Test
    public void selectWildcard(){
        String filter = "*-*-*";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        HashSet<IClient> allClientSet = new HashSet<>();
        allClientSet.add(clientAustin);
        allClientSet.add(clientHuston);
        allClientSet.add(clientDallas);
        allClientSet.add(clientLima);
        allClientSet.add(clientHoboken);

        assert(allClientSet.contains(foundClient));
    }
    @Test
    public void selectCountry(){
        String filter = "United States";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        HashSet<IClient> allClientSet = new HashSet<>();
        allClientSet.add(clientAustin);
        allClientSet.add(clientHuston);
        allClientSet.add(clientDallas);
        allClientSet.add(clientHoboken);

        assert(allClientSet.contains(foundClient));
    }
    @Test
    public void selectCountry_State(){
        String filter = "United States-New Jersey";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        assertEquals(clientHoboken,foundClient);
    }
    @Test
    public void selectNoNExisting(){
        String filter = "United States-Lima-Huston";
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        assertNull(foundClient);
    }
    @Test
    public void selectNullFilter(){
        IClient foundClient = loadBalancer.selectClient(null);
        printClient(foundClient, null);
        assertNull(foundClient);
    }
    @Test
    public void addDoubleClient() {
        String filter = "United States-Texas-Austin";
        loadBalancer.addClient(clientAustin);
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        HashSet<IClient> allClientSet = new HashSet<>();
        allClientSet.add(clientAustin);

        assertEquals (clientAustin, foundClient);
    }

    @Test
    public void addEqualClients(){
        Client clientAustinThe2nd = new Client("United States", "Texas", "Austin");
        String filter = "United States-Texas-Austin";
        loadBalancer.addClient(clientAustinThe2nd);
        IClient foundClient = loadBalancer.selectClient(filter);
        printClient(foundClient, filter);
        HashSet<IClient> allClientSet = new HashSet<>();
        allClientSet.add(clientAustin);
        allClientSet.add(clientAustinThe2nd);

        assert(allClientSet.contains(foundClient));
    }

    private void printClient(IClient foundClient, String filter) {
        if(foundClient == null){
            System.out.println("Client is null");
        } else {
            System.out.println("found client: \n" + foundClient + "\nfor filter: "+filter);
        }
    }

}
