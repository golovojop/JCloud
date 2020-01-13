package data.dao;

import domain.Customer;

import java.util.Set;

public interface CustomerDao {
    Set<Customer> getAllCustomer();
    Customer getCustomerById(int id);
    Customer getCustomerByLogin(String login);
    Customer getCustomerByLoginAndPass(String login, String pass);
    boolean exists(Customer customer);
    boolean insertCustomer(Customer customer);
    boolean updateCustomer(Customer customer);
    boolean deleteCustomer(int id);
}
