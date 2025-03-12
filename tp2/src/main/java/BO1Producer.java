import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import java.sql.*;
import com.google.gson.Gson;

public class BO1Producer {
    private final static String QUEUE_NAME = "BO1_to_HO";

    public static void main(String[] argv) throws Exception {
        // Connect to MySQL (BO1_DB)
        Connection mysqlConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/BO1_DB", "root", "");
        Statement statement = mysqlConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM product_sales");

        // Connect to RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (com.rabbitmq.client.Connection rabbitmqConnection = factory.newConnection();
             Channel channel = rabbitmqConnection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // Fetch data and send to RabbitMQ
            Gson gson = new Gson();
            while (resultSet.next()) {
                String date = resultSet.getString("date");
                String region = resultSet.getString("region");
                String product = resultSet.getString("product");
                int qty = resultSet.getInt("qty");
                double cost = resultSet.getDouble("cost");
                double amt = resultSet.getDouble("amt");
                double tax = resultSet.getDouble("tax");
                double total = resultSet.getDouble("total");

                // Create JSON object
                String json = gson.toJson(new ProductSale(date, region, product, qty, cost, amt, tax, total));
                channel.basicPublish("", QUEUE_NAME, null, json.getBytes());
                System.out.println(" [x] Sent '" + json + "'");
            }
        }
    }

    // ProductSale class for JSON serialization
    static class ProductSale {
        String date, region, product;
        int qty;
        double cost, amt, tax, total;

        public ProductSale(String date, String region, String product, int qty, double cost, double amt, double tax, double total) {
            this.date = date;
            this.region = region;
            this.product = product;
            this.qty = qty;
            this.cost = cost;
            this.amt = amt;
            this.tax = tax;
            this.total = total;
        }
    }
}