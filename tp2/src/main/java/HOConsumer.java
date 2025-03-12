import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.sql.*;
import com.google.gson.Gson;

public class HOConsumer {
    private final static String QUEUE_BO1 = "BO1_to_HO";
    private final static String QUEUE_BO2 = "BO2_to_HO";

    public static void main(String[] argv) throws Exception {
        // Connect to MySQL (HO_DB)
        Connection mysqlConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/HO_DB", "root", "");

        // Connect to RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        com.rabbitmq.client.Connection rabbitmqConnection = factory.newConnection();
        Channel channel = rabbitmqConnection.createChannel();

        // Declare both queues
        channel.queueDeclare(QUEUE_BO1, false, false, false, null);
        channel.queueDeclare(QUEUE_BO2, false, false, false, null);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallbackBO1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received from BO1: '" + message + "'");
            try {
                insertIntoDB(mysqlConnection, message);
            } catch (SQLException e) {
                System.err.println("Error inserting data into database: " + e.getMessage());
            }
        };
        
        DeliverCallback deliverCallbackBO2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received from BO2: '" + message + "'");
            try {
                insertIntoDB(mysqlConnection, message);
            } catch (SQLException e) {
                System.err.println("Error inserting data into database: " + e.getMessage());
            }
        };

        // Start consuming from both queues
        channel.basicConsume(QUEUE_BO1, true, deliverCallbackBO1, consumerTag -> { });
        channel.basicConsume(QUEUE_BO2, true, deliverCallbackBO2, consumerTag -> { });
    }

    // Method to insert data into HO_DB
    private static void insertIntoDB(Connection mysqlConnection, String message) throws SQLException {
        Gson gson = new Gson();
        ProductSale productSale = gson.fromJson(message, ProductSale.class);

        PreparedStatement preparedStatement = mysqlConnection.prepareStatement(
            "INSERT INTO product_sales (date, region, product, qty, cost, amt, tax, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setString(1, productSale.date);
        preparedStatement.setString(2, productSale.region);
        preparedStatement.setString(3, productSale.product);
        preparedStatement.setInt(4, productSale.qty);
        preparedStatement.setDouble(5, productSale.cost);
        preparedStatement.setDouble(6, productSale.amt);
        preparedStatement.setDouble(7, productSale.tax);
        preparedStatement.setDouble(8, productSale.total);
        preparedStatement.executeUpdate();
    }

    // ProductSale class for JSON deserialization
    static class ProductSale {
        String date, region, product;
        int qty;
        double cost, amt, tax, total;
    }
}