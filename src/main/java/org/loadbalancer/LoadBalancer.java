package org.loadbalancer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.client.IClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 *
 * Implements ILoadBalancer.
 * Allows Clients to register and\or be removed.
 * Returns a random Client matching a filter pattern.
 * Allows concurrency.
 * Allows safe usage of multiple readers/writers.
 * {@code @Author} - Tal Greenblat
 *
 */
@Component
@AllArgsConstructor
@NoArgsConstructor
public class LoadBalancer implements ILoadBalancer{


    //Country -> List<IClient>
    private HashMap<String, ArrayList<IClient>> countryToClientMap;

    //State -> List<IClient>
    private HashMap<String, ArrayList<IClient>> stateToClientMap;

    //City -> List<IClient>
    private HashMap<String, ArrayList<IClient>> cityToClientMap;

    //All clients
    //This data structure is for wildcard use.
    private ArrayList<IClient> clients;
    private static final String WILDCARD = "*";

    private ReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * @param client
     * add client to LoadBalancer.
     * write lock.
     */
    @Override
    public void addClient(IClient client) {
        lock.writeLock().lock();
        try{
            clients.add(client);
            //HashMap.merge() is a neat way of not having to initialize each arraylist in map
            countryToClientMap.merge(client.getCountry(), new ArrayList<>(Arrays.asList(client)),
                    (clientList, newClientList) -> {clientList.add(client);return clientList;});

            stateToClientMap.merge(client.getState(), new ArrayList<>(Arrays.asList(client)),
                    (clientList, newClientList) -> {clientList.add(client);return clientList;});

            cityToClientMap.merge(client.getCity(), new ArrayList<>(Arrays.asList(client)),
                    (clientList, newClientList) -> {clientList.add(client);return clientList;});
        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * @param clientToRemove
     * remove clientToRemove from LoadBalancer.
     * Assumption - remove client will happen rarely. This is why the time complexity is O(n).
     */
    @Override
    public void removeClient(IClient clientToRemove) {
        lock.writeLock().lock();
        try {
            clients.remove(clientToRemove);
            countryToClientMap.get(clientToRemove.getCountry()).remove(clientToRemove);
            stateToClientMap.get(clientToRemove.getState()).remove(clientToRemove);
            cityToClientMap.get(clientToRemove.getCity()).remove(clientToRemove);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @param filter - in the form of country-state-city i.e. U.S.-NY-NY.
     * @return - returns a random client matching the filter param.
     * filter may contain '*' as wildcard to match all city/state/country.
     */
    @Override
    public IClient selectClient(String filter) {
        lock.readLock().lock();
        try {
            //filter: <country>-<state>-<city>
            ArrayList<String> clientAsArray = parseFilter(filter);

            List<IClient> clientList;

            String country = clientAsArray.get(0);
            String state;
            String city;

            if(clientAsArray.size()>1){
                state = clientAsArray.get(1);
            } else {
                state = WILDCARD;
            }

            if(clientAsArray.size()>2){
                city = clientAsArray.get(2);
            } else {
                city = WILDCARD;
            }

            if(!city.equals(WILDCARD)){
                return handleCity(country, state, city);
            }

            if(!state.equals(WILDCARD)){
                clientList = stateToClientMap.get(state);
                if(!country.equals(WILDCARD)){
                    clientList = filterByCountry(country, clientList);
                }
                return getRandomClientFromList(clientList);
            }

            if(!country.equals(WILDCARD)){
                return getRandomClientFromList(countryToClientMap.get(country));
            } else {
                //All are wildcards
                return getRandomClientFromList(clients);
            }
        } catch (NoSuchElementException ex){
            System.out.println("No client was found matching filter: "+filter);
            return null;
        }finally {
            lock.readLock().unlock();
        }
    }

    private IClient handleCity(String country, String state, String city) {
        List<IClient> clientList;
        clientList = cityToClientMap.get(city);

        if(!state.equals(WILDCARD)){
            clientList = filterByState(state, clientList);
        }
        if(!country.equals(WILDCARD)){
            clientList = filterByCountry(country, clientList);
        }
        return getRandomClientFromList(clientList);
    }

    private static List<IClient> filterByCountry(String country, List<IClient> clientList) {
        clientList = clientList.stream().
                filter(currClient -> currClient.getCountry().equals(country))
                .collect(Collectors.toList());
        return clientList;
    }

    private static List<IClient> filterByState(String state, List<IClient> clientList) {
        clientList = clientList.stream().
                filter(currClient -> currClient.getState().equals(state))
                .collect(Collectors.toList());
        return clientList;
    }

    private IClient getRandomClientFromList(List<IClient> clientList) {
        if(clientList.isEmpty()){
            throw new NoSuchElementException();
        }
        return clientList.get((int) (Math.random() * clientList.size()));
    }

    private static ArrayList<String> parseFilter(String filter) {
        if(filter == null || filter.isEmpty()){
            throw new NoSuchElementException();
        }
        String[] segments = filter.split("-");
        return new ArrayList<>(Arrays.asList(segments));
    }

    /**
     * Resets the LoadBalancer to factory settings.
     */
    public void reset() {
        this.countryToClientMap = new HashMap<>();
        this.stateToClientMap = new HashMap<>();
        this.cityToClientMap = new HashMap<>();
        this.clients = new ArrayList<>();
    }
}
