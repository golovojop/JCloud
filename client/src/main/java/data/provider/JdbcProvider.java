/**
 * Materials:
 * https://dzone.com/articles/building-simple-data-access-layer-using-jdbc
 */

package data.provider;

import data.dao.CustomerDao;
import data.repository.jdbc.JdbcRepository.ConnectionHolder;
import domain.Customer;

import java.sql.*;
import java.util.Set;

public class JdbcProvider implements CustomerDao {

    public JdbcProvider() {
    }

    @Override
    public Set<Customer> getAllCustomer() {
        return null;
    }

    @Override
    public boolean exists(Customer customer) {
        return getCustomerByLogin(customer.getLogin()) != null;
    }

    @Override
    public Customer getCustomerById(int id) {
        try {
            Connection connection = ConnectionHolder.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM customer WHERE id='" + id + "';");

            if (rs.next()) {
                return extractCustomer(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Customer getCustomerByLogin(String login) {
        try {
            Connection connection = ConnectionHolder.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM customer WHERE login='" + login + "';");
            if (rs.next()) {
                return extractCustomer(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Customer getCustomerByLoginAndPass(String login, String pass) {
        try {
            Connection connection = ConnectionHolder.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM customer WHERE login=? AND pass=?");
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return extractCustomer(rs);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insertCustomer(Customer customer) {
        if(exists(customer)) {
            System.out.println("Customer " + customer.getLogin() + " already exists");
            return false;
        }

        try {
            Connection connection = ConnectionHolder.getConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO customer VALUES (NULL, ?, ?)");
            ps.setString(1, customer.getLogin());
            ps.setString(2, customer.getPass());

            int i = ps.executeUpdate();
            if (i == 1) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        try {
            Connection connection = ConnectionHolder.getConnection();
            PreparedStatement ps = connection.prepareStatement("UPDATE customer SET login=?, pass=? WHERE id=?");
            ps.setString(1, customer.getLogin());
            ps.setString(2, customer.getPass());
            ps.setInt(3, customer.getId());

            int i = ps.executeUpdate();
            if (i == 1) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteCustomer(int id) {
        try {
            Connection connection = ConnectionHolder.getConnection();
            Statement statement = connection.createStatement();
            int i = statement.executeUpdate("DELETE FROM customer WHERE id='" + id + "';");
            if(i == 1) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private Customer extractCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setLogin(rs.getString("login"));
        customer.setPass(rs.getString("pass"));
        return customer;
    }
}