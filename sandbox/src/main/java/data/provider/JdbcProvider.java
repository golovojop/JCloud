package data.provider;

import data.repository.jdbc.JdbcRepository;
import domain.Customer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcProvider {

    private JdbcRepository repository;

    public JdbcProvider() {
        this.repository =  JdbcRepository.getJdbc();
    }

    public void saveCustomer(Customer customer) {
        PreparedStatement prepStatement = null;

//        try {
//            prepStatement = connection.prepareStatement(sql[0]);
//
//            for(int i = 1; i < sql.length; i++) {
//                prepStatement.setString(i, sql[i]);
//            }
//
//            prepStatement.setInt(sql.length, id);
//
//            prepStatement.executeUpdate();
//        } catch (SQLException e) {e.printStackTrace();}
//

    }
}
