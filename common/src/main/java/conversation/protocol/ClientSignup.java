package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;
import domain.Customer;

public class ClientSignup extends ClientMessage {
    static final long serialVersionUID = 101L;
    private Customer customer;

    public ClientSignup(long id, Customer customer) {
        super(id, null, ClientRequest.SIGNUP);
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }
}
