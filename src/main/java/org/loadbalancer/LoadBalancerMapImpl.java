package org.loadbalancer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.client.IClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @deprecated - was not an efficient way for selectClient or return random for any scenario. scrapped.
 * Implements ILoadBalancer.
 * Allows Clients to register and\or be removed.
 * {@code @Author} - Tal Greenblat
 */
@Component
@Getter
@AllArgsConstructor
public class LoadBalancerMapImpl implements ILoadBalancer{

    //Country, State, City
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<IClient>>>> registeredClients;

    private static final String WILDCARD = "*";

    public LoadBalancerMapImpl() {
        this.registeredClients = new HashMap<>();
    }

    public HashMap<String, HashMap<String, HashMap<String, ArrayList<IClient>>>> getRegisteredClients() {
        return registeredClients;
    }

    @Override
    public void addClient(IClient client) {
        registeredClients.merge(client.getCountry(), new HashMap<>(Map.of(client.getState(),
                                                        new HashMap<>(Map.of(client.getCity(),
                                                                new ArrayList<>(Arrays.asList(client)))))),
                (mapCountryAsKey, newCountryMap) -> {
                    mapCountryAsKey.merge(client.getState(), new HashMap<>(Map.of(client.getCity(),
                            new ArrayList<>(Arrays.asList(client)))),
                            (mapStateAsKey, newStateMap) -> {
                                mapStateAsKey.merge(client.getCity(), new ArrayList<>(Arrays.asList(client)),
                                        (listOfCityClients, newListOfCityClients) -> {
                                            listOfCityClients.add(client);
                                            return listOfCityClients;
                                        });
                            return mapStateAsKey;
                            });
                    return mapCountryAsKey;
                });
    }

    @Override
    public void removeClient(IClient clientToRemove) {

        HashMap<String, HashMap<String, ArrayList<IClient>>> countryMap = registeredClients.get(clientToRemove.getCountry());
        HashMap<String, ArrayList<IClient>> StateMap = countryMap.get(clientToRemove.getState());
        ArrayList<IClient> clientList = StateMap.get(clientToRemove.getCity());

        clientList.remove(clientToRemove);

        if(clientList.isEmpty()){
            StateMap.remove(clientToRemove.getCity());

            if(StateMap.isEmpty()){
                countryMap.remove(clientToRemove.getState());

                if(countryMap.isEmpty()){
                    registeredClients.remove(clientToRemove.getCountry());
                }
            }
        }
    }

    @Override
    public IClient selectClient(String filter) {
        //filter: <country>-<state>-<city>
        return null;
    }

    private boolean isFullWildcard(String countryStateCity) {
        return countryStateCity.equals(WILDCARD+WILDCARD+WILDCARD);
    }

    private boolean checkIfUnlikelyPattern(String countryStateCity) {
        String pattern = "\\*\\w";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(countryStateCity);

        if (m.find()){
            return true;
        } else {
            return false;
        }
    }

    private static ArrayList<String> parseFilter(String filter) {
        String pattern = "<(\\w+)>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(filter);

        ArrayList<String> clientAsArray = new ArrayList<>();

        while (m.find()){
            clientAsArray.add(m.group(1));
        }
        return clientAsArray;
    }

    public void reset() {
        registeredClients = new HashMap<>();
    }
}
