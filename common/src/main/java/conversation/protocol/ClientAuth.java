package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;
import domain.Customer;

public class ClientAuth extends ClientMessage {
    static final long serialVersionUID = 101L;
    private Customer customer;

    public ClientAuth(long id, Customer customer) {
        super(id, ClientRequest.AUTH);
        this.customer = customer;
    }

    @Override
    public ClientRequest getRequest() {
        return super.getRequest();
    }

    @Override
    public long getId() {
        return super.getId();
    }

    public Customer getCustomer() {
        return customer;
    }
}
