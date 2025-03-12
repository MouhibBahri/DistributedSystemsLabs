# Setup Instructions

1. **Run the RabbitMQ Docker Image:**

      Start the RabbitMQ container by running the following command:

      ```bash
      sudo docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4.0-management
      ```

      This will run RabbitMQ with the management plugin enabled, which you can access through [http://localhost:15672](http://localhost:15672) (default username: `guest`, password: `guest`).

2. **Build the Project:**

      If you haven't built the project yet, use the following command to build it with Maven:

      ```bash
      mvn clean package
      ```

3. **Execute the Branch Office Scripts:**

      After building the project, execute the two branch office scripts using Maven:

      - For the first branch office:

           ```bash
           mvn exec:java -Dexec.mainClass="BO1Producer"
           ```

      - For the second branch office:
           ```bash
           mvn exec:java -Dexec.mainClass="BO2Producer"
           ```

4. **Execute the Head Office Script:**

      Finally, execute the head office script using Maven:

      ```bash
      mvn exec:java -Dexec.mainClass="HOConsumer"
      ```
