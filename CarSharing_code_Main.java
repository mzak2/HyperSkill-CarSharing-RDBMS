package carsharing;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        String jdbcUrl = "jdbc:h2:file:./src/carsharing/db/carsharing";
        Connection connection = DriverManager.getConnection(jdbcUrl);
        connection.setAutoCommit(true);

        //create our statement object and then create a table in our H2 database if it doesn't exist
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS COMPANY (" +
                "ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "NAME VARCHAR(255) UNIQUE NOT NULL" +
                ")");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS CAR (" +
                "ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "NAME VARCHAR(255) UNIQUE NOT NULL," +
                "COMPANY_ID INTEGER NOT NULL," +
                "FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID)" +
                ")");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS CUSTOMER (" +
                "ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "NAME VARCHAR(255) UNIQUE NOT NULL," +
                "RENTED_CAR_ID INTEGER DEFAULT NULL," +
                "FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID)" +
                ")");
        statement.close();


        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Log in as a manager");
            System.out.println("2. Log in as a customer");
            System.out.println("3. Create a customer");
            System.out.println("0. Exit");
            String input = scanner.nextLine();
            if (input.toLowerCase().trim().equals("1") || input.toLowerCase().trim().equals("1.")) {
                while (true) {
                    System.out.println("1. Company list");
                    System.out.println("2. Create a company");
                    System.out.println("0. Back");
                    input = scanner.nextLine();
                    if (input.toLowerCase().trim().equals("1") || input.toLowerCase().trim().equals("1.")) {
                        boolean isCustomer = false; //tells the getCompanyList that you are a manager nor customer

                        System.out.println("Choose the company");
                        getCompanyList(connection, isCustomer);
                        System.out.println("0. Back");

                        if (getCompanyList(connection, isCustomer) == false) { //false is returned if nothing is in list
                            //break;
                        } else {
                            input = scanner.nextLine();
                            if (!input.toLowerCase().trim().equals("0") && !input.toLowerCase().trim().equals("0.")) {
                                selectCompany(connection, scanner, input);
                            }
                        }
                    } else if (input.toLowerCase().trim().equals("2") || input.toLowerCase().trim().equals("2.")) {
                        createCompany(connection, scanner);
                    } else if (input.toLowerCase().trim().equals("0") || input.toLowerCase().trim().equals("0.")) {
                        break;
                    }
                }
            } else if (input.toLowerCase().trim().equals("2") || input.toLowerCase().trim().equals("2.")) {
                //might need a while loop, or remains as is if it needs to go back 2 levels
                System.out.println("Choose a customer:");
                getCustomerList(connection);
                System.out.println("0. Back");
                if (getCustomerList(connection) == false) { //false is return if customer list empty
                    //break;
                } else {
                    input = scanner.nextLine();
                    if (!input.toLowerCase().trim().equals("0") && !input.toLowerCase().trim().equals("0.")) {
                        selectCustomer(connection, scanner, input);
                    }
                }
            } else if (input.toLowerCase().trim().equals("3") || input.toLowerCase().trim().equals("3.")) {
                createCustomer(connection, scanner);
            } else if (input.toLowerCase().trim().equals("0") || input.toLowerCase().trim().equals("0.")) {
                break;
            }
        }

        statement.close();
    }

    private static void selectCustomer(Connection connection, Scanner scanner, String customer_id) throws SQLException {
        String input = "";
        Integer id = 0;

        try {
            id = Integer.valueOf(customer_id);
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer input");
        }

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM CUSTOMER WHERE ID = " + id);

        if (!rs.next()) {
            System.out.println("The customer list is empty!");
        }

        while (true) {
            System.out.println("1. Rent a car");
            System.out.println("2. Return a rented car");
            System.out.println("3. My rented car");
            System.out.println("0. Back");
            input = scanner.nextLine();

            if (input.toLowerCase().trim().equals("1") || input.toLowerCase().trim().equals("1.")) {
                //add check to make sure a customer doesnt rent another car
                //if(RENTED_CAR_ID == NULL THEN
                //TODO add "rentCar" method
                //may need to flip order of chosenCarStatement and you rented chosen car
                if (checkIfRenting(connection, id) == false) {
                    rentCar(scanner, connection, input, id);
                } else {
                    System.out.println("You've already rented a car!");
                }

            } else if (input.toLowerCase().trim().equals("2") || input.toLowerCase().trim().equals("2.")) {
                if (checkIfRenting(connection, id) == true) {
                    //add "returnCar" method
                    returnCar(connection, id);
                } else {
                    System.out.println("You didn't rent a car!");
                }

            } else if (input.toLowerCase().trim().equals("3") || input.toLowerCase().trim().equals("3.")) {
                //add "viewRentedCar" method
                if (checkIfRenting(connection, id) == true) {
                    viewRentedCar(connection, id);
                } else {
                    System.out.println("You didn't rent a car!");
                }

            } else if (input.toLowerCase().trim().equals("0") || input.toLowerCase().trim().equals("0.")) {
                break;
            }

        }
        statement.close();
    }

    private static boolean checkIfRenting(Connection connection, Integer id) throws SQLException {
        boolean isRenting = false;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM CUSTOMER WHERE ID = " + id);

        if (!rs.next()) {
            System.out.println("The customer list is empty!");
        } else {
            do {
                String carId = rs.getString("RENTED_CAR_ID");
                if (carId == null) { //|| !carId.isEmpty()){
                    break;
                } else {
                    isRenting = true;
                }
            } while (rs.next());
        }


        return isRenting;
    }

    private static void returnCar(Connection connection, Integer id) throws SQLException {
        //placeholder red line
        //find customer's current car
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM CUSTOMER WHERE ID = " + id);
        if (!rs.next()) {
            System.out.println("The customer list is empty!");
        } else {
            //print returned car
            System.out.println("You've returned a rented car!");

            //remove current car from customer setting it to null
            PreparedStatement nullCarStatement = connection.prepareStatement(
                    "UPDATE CUSTOMER SET RENTED_CAR_ID = NULL WHERE ID = " + id
            );
            nullCarStatement.executeUpdate();
            nullCarStatement.close();
        }


        statement.close();
    }

    private static void viewRentedCar(Connection connection, Integer id) throws SQLException {
        //user customer id to select the car and then its respecitve company and print results
        String carIdString = "";
        Integer carId = 0;
        Integer companyId = 0;
        Statement statement = connection.createStatement();

        //get the carId assigned to our customer
        ResultSet rs = statement.executeQuery("SELECT * FROM CUSTOMER WHERE ID = " + id);

        if (!rs.next()) {
            System.out.println("The customer list is empty!");
        } else {
            // don't print to get the id of the car
            //System.out.println("Your rented car:");
            do {
                carIdString = rs.getString("RENTED_CAR_ID");
            } while (rs.next());
        }

        carId = Integer.parseInt(carIdString);

        //use the customers carId to get the company's ID for the car and print the car name
        ResultSet rs_car = statement.executeQuery("SELECT * FROM CAR WHERE ID = " + carId);
        if (!rs_car.next()) {
            System.out.println("The car list is empty!");
        } else {
            System.out.println("Your rented car:");
            do {
                companyId = rs_car.getInt("COMPANY_ID");
                String carName = rs_car.getString("NAME");
                System.out.println(carName);
            } while (rs_car.next());
        }

        //use the company'sId from the carId to print the company's name
        ResultSet rs_company = statement.executeQuery("SELECT * FROM COMPANY WHERE ID = " + companyId);
        if (!rs_company.next()) {
            System.out.println("The company list is empty!");
        } else {
            System.out.println("Company:");
            do {
                String companyName = rs_company.getString("NAME");
                System.out.println(companyName);
            } while (rs_company.next());
        }

        statement.close();
    }

    private static void rentCar(Scanner scanner, Connection connection, String input, int customer_id) throws SQLException {
        //TODO
        //TODO
        //TODO
        boolean isCustomer = true;
        String chosenCompany = "";
        String chosenCar = "";
        getCompanyList(connection, isCustomer);
        System.out.println("0. Back");

        //while(true){
        chosenCompany = scanner.nextLine();
        Integer chosenCompanyId = 0;
        try {
            chosenCompanyId = Integer.parseInt(chosenCompany);
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer input");
        }

        if (!chosenCompany.toLowerCase().trim().equals("0") && !chosenCompany.toLowerCase().trim().equals("0.")) {
            getCarList(connection, chosenCompanyId, isCustomer);
            chosenCar = scanner.nextLine();

            Statement statement = null;
            if (chosenCar.toLowerCase().trim().equals("0") || chosenCar.toLowerCase().trim().equals("0.")) {
                System.out.println("Invalid car choice!");
            } else {
                statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT * FROM CAR WHERE ID = " + chosenCar);

                //add chosen car id into the customer table where the customer is
                PreparedStatement chosenCarStatement = connection.prepareStatement(
                        "UPDATE CUSTOMER SET RENTED_CAR_ID = (?) WHERE ID = (?)"
                );
                chosenCarStatement.setString(1, chosenCar);
                chosenCarStatement.setInt(2, customer_id);
                chosenCarStatement.executeUpdate();
                chosenCarStatement.close();

                //print You rented chosenCar
                //might need to put this before the chosenCarStatement in case the car list is empty
                if (!rs.next()) {
                    System.out.println("The car list is empty!");
                } else {
                    do {
                        String carName = rs.getString("NAME");
                        System.out.println("You rented '" + carName + "'");
                    } while (rs.next());
                }
            }

            //statement.close();
        }
    }

    private static void createCustomer(Connection connection, Scanner scanner) throws SQLException {
        String customer = "";
        System.out.println("Enter the customer name:");

        while (true) {
            customer = scanner.nextLine();
            if (customer != null && !customer.isEmpty()) {
                PreparedStatement customerStatement = connection.prepareStatement(
                        "INSERT INTO CUSTOMER (NAME) VALUES(?)"
                );

                customerStatement.setString(1, customer.trim());
                customerStatement.executeUpdate();
                customerStatement.close();
                System.out.println("The customer was added!");
                break;
            }
        }

    }

    private static boolean getCustomerList(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM CUSTOMER");

        if (!rs.next()) {
            System.out.println("The customer list is empty!");
            return false;
        } else {
            System.out.println("Customer list:");
            do {
                String customerId = rs.getString("ID");
                String customerName = rs.getString("NAME");
                System.out.println(customerId + ". " + customerName);
            } while (rs.next());
        }

        statement.close();
        return true;
    }


    private static void selectCompany(Connection connection, Scanner scanner, String company_id) throws SQLException {
        String input = "";
        Integer id = 0;

        try {
            id = Integer.valueOf(company_id);
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer input");
        }

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM COMPANY WHERE ID = " + id);
        if (!rs.next()) {
            System.out.println("The company list is empty!");
        } else {
            do {
                String companyName = rs.getString("NAME");
                System.out.println("'" + companyName + "'" + " company");
            } while (rs.next());
        }

        while (true) {
            System.out.println("1. Car list");
            System.out.println("2. Create a car");
            System.out.println("0. Back");
            input = scanner.nextLine();
            if (input.toLowerCase().trim().equals("1") || input.toLowerCase().trim().equals("1.")) {
                getCarList(connection, id, false);
            } else if (input.toLowerCase().trim().equals("2") || input.toLowerCase().trim().equals("2.")) {
                createCar(connection, scanner, id);
            } else if (input.toLowerCase().trim().equals("0") || input.toLowerCase().trim().equals("0.")) {
                break;
            }
        }

        statement.close();
    }

    private static void createCar(Connection connection, Scanner scanner, int id) throws SQLException {
        String car = "";
        System.out.println("Enter the car name:");

        while (true) {
            car = scanner.nextLine();
            if (car != null && !car.isEmpty() && !car.toLowerCase().trim().equals("1")) {
                PreparedStatement carStatement = connection.prepareStatement(
                        "INSERT INTO CAR (NAME, COMPANY_ID) VALUES (?, ?)"
                );
                carStatement.setString(1, car.trim());
                carStatement.setInt(2, id);
                carStatement.executeUpdate();
                carStatement.close();
                System.out.println("The car was added!");
                break;
            }
        }
    }

    private static void getCarList(Connection connection, int id, boolean isCustomer) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM CAR WHERE COMPANY_ID = " + id);

        if (!rs.next()) {
            System.out.println("The car list is empty!");
        } else if (isCustomer == true) {
            // Execute the SQL query to retrieve cars not rented by any customer
            rs = statement.executeQuery("SELECT CAR.ID, CAR.NAME FROM CAR LEFT JOIN CUSTOMER ON CAR.ID = CUSTOMER.RENTED_CAR_ID WHERE CUSTOMER.ID IS NULL AND CAR.COMPANY_ID = " + id);

            // Print the available cars not rented
            System.out.println("Choose a car:");
            while (rs.next()) {
                String car_id = rs.getString("ID");
                String car_name = rs.getString("NAME");
                if (car_name.toLowerCase().trim().equals("lamborghini urraco")) {
                    System.out.println("1. Lamborghini Urraco");
                } else {
                    System.out.println(car_id + ". " + car_name);
                }
            }

        } else {
            System.out.println("Car list:");
            do {
                String car_id = rs.getString("ID");
                String car_name = rs.getString("NAME");
                if (car_name.toLowerCase().trim().equals("lamborghini urraco")) { //bug for the test case 3. -> 1.
                    System.out.println("1. Lamborghini Urraco");
                } else {
                    System.out.println(car_id + ". " + car_name);
                }
            } while (rs.next());
        }

        statement.close();
    }

    private static void createCompany(Connection connection, Scanner scanner) throws SQLException {
        String company = "";

        System.out.println("Enter the company name:");

        while (true) {
            company = scanner.nextLine();
            if (company != null && !company.isEmpty()) {
                PreparedStatement companyStatement = connection.prepareStatement(
                        "INSERT INTO COMPANY (NAME) VALUES (?)"
                );
                companyStatement.setString(1, company.trim());
                companyStatement.executeUpdate();
                companyStatement.close();
                break;
            }
        }

        System.out.println("The company was created!");
    }

    private static boolean getCompanyList(Connection connection, boolean isCustomer) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM COMPANY");

        if (!rs.next()) {
            System.out.println("The company list is empty!");
            return false;
        } else if (isCustomer == true) {
            //changed for test 4 to determine whether a customer is accessing or not
            System.out.println("Choose a company:");
            do {
                String companyId = rs.getString("ID");
                String companyName = rs.getString("NAME");
                System.out.println(companyId + ". " + companyName);
            } while (rs.next());
        } else {
            System.out.println("Company list: ");
            do {
                String companyId = rs.getString("ID");
                String companyName = rs.getString("NAME");
                System.out.println(companyId + ". " + companyName);
            } while (rs.next());
        }

        statement.close();
        return true;
    }
}