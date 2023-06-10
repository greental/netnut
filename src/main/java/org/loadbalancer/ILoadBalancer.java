package org.loadbalancer;

import org.client.IClient;

public interface ILoadBalancer {
    public void addClient(IClient client);
    public void removeClient(IClient client);
    public IClient selectClient(String filter);
}
