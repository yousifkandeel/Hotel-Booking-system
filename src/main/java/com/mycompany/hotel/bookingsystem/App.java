package com.mycompany.hotel.bookingsystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;




public class App extends Application {

    private Hotel hotel = new Hotel();
    private ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private ObservableList<Room> availableRooms = FXCollections.observableArrayList();
    private ObservableList<Review> reviews = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        initializeSampleData();
        
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4e54c8, #8f94fb);");

        Tab bookingTab = createBookingTab();
        Tab roomsTab = createRoomsTab();
        Tab servicesTab = createServicesTab();
        Tab reviewsTab = createReviewsTab();
        Tab adminTab = createAdminTab();

        tabPane.getTabs().addAll(bookingTab, roomsTab, servicesTab, reviewsTab, adminTab);

        Scene scene = new Scene(tabPane, 900, 650);
        scene.setFill(Color.web("#f5f7fa"));

        primaryStage.setTitle("Hotel Booking System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeSampleData() {
        try {
            // Add sample rooms
            hotel.addRoom(new SingleRoom(101, 100.0, true));
            hotel.addRoom(new DoubleRoom(201, 150.0, true));
            hotel.addRoom(new SuiteRoom(301, 250.0, true));
            hotel.addRoom(new SingleRoom(102, 100.0, false));
            hotel.addRoom(new DoubleRoom(202, 150.0, true));

            // Initialize available rooms
            updateAvailableRooms();

            // Add sample reviews
            Customer sampleCustomer = new Customer("John Doe", "john@example.com", "password123");
            reviews.add(new Review(1, sampleCustomer, 5, "Excellent service!", new Date()));
            reviews.add(new Review(2, sampleCustomer, 4, "Very good experience", new Date()));
        } catch (Exception e) {
            showAlert("Error", "Failed to initialize sample data: " + e.getMessage());
        }
    }

    private void updateAvailableRooms() {
        try {
            availableRooms.setAll(hotel.searchAvailableRooms());
        } catch (Hotel.HotelOperationException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private Tab createBookingTab() {
        Tab tab = new Tab("Bookings");
        tab.setClosable(false);
        
        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);
        form.setStyle("-fx-background-color: #e6e9f0; -fx-border-radius: 10; -fx-background-radius: 10;");
        
        // Form components
        Label titleLabel = new Label("New Booking");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#4a47a3"));
        form.add(titleLabel, 0, 0, 2, 1);

        // Customer info
        form.add(new Label("Customer Name:"), 0, 1);
        TextField customerNameField = new TextField();
        form.add(customerNameField, 1, 1);

        form.add(new Label("Email:"), 0, 2);
        TextField emailField = new TextField();
        form.add(emailField, 1, 2);

        // Room selection
        form.add(new Label("Room Type:"), 0, 3);
        ComboBox<String> roomTypeCombo = new ComboBox<>();
        roomTypeCombo.getItems().addAll("Single", "Double", "Suite");
        form.add(roomTypeCombo, 1, 3);

        // Dates
        form.add(new Label("Check-in Date:"), 0, 4);
        DatePicker checkInPicker = new DatePicker();
        checkInPicker.setValue(LocalDate.now());
        form.add(checkInPicker, 1, 4);

        form.add(new Label("Check-out Date:"), 0, 5);
        DatePicker checkOutPicker = new DatePicker();
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
        form.add(checkOutPicker, 1, 5);

        // Services
        form.add(new Label("Additional Services:"), 0, 6);
        CheckBox roomServiceCheck = new CheckBox("Room Service");
        CheckBox laundryCheck = new CheckBox("Laundry");
        CheckBox spaCheck = new CheckBox("Spa");
        VBox servicesBox = new VBox(5, roomServiceCheck, laundryCheck, spaCheck);
        form.add(servicesBox, 1, 6);

        // Payment
        form.add(new Label("Payment Method:"), 0, 7);
        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton creditCardRadio = new RadioButton("Credit Card");
        creditCardRadio.setToggleGroup(paymentGroup);
        RadioButton paypalRadio = new RadioButton("PayPal");
        paypalRadio.setToggleGroup(paymentGroup);
        VBox paymentBox = new VBox(5, creditCardRadio, paypalRadio);
        form.add(paymentBox, 1, 7);

        // Submit button with event handler
        Button submitButton = new Button("Book Now");
        submitButton.setStyle("-fx-background-color: #4a47a3; -fx-text-fill: white;");
        submitButton.setOnAction(e -> handleBookingSubmission(
            customerNameField, emailField, roomTypeCombo, 
            checkInPicker, checkOutPicker, 
            roomServiceCheck, laundryCheck, spaCheck, 
            paymentGroup
        ));
        form.add(submitButton, 0, 8, 2, 1);

        // Booking list
        ListView<Booking> bookingList = new ListView<>(bookings);
        bookingList.setStyle("-fx-background-color: white; -fx-border-color: #4a47a3;");
        bookingList.setCellFactory(param -> new ListCell<Booking>() {
            @Override
            protected void updateItem(Booking booking, boolean empty) {
                super.updateItem(booking, empty);
                if (empty || booking == null) {
                    setText(null);
                } else {
                    setText(String.format("Booking #%d - %s - Room %d (%s) - %s to %s - $%.2f",
                        booking.getBookingId(),
                        booking.getCustomer().getName(),
                        booking.getRoom().getRoomNumber(),
                        booking.getRoom().getType(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getTotalPrice()));
                }
            }
        });

        // Cancel booking button
        Button cancelButton = new Button("Cancel Booking");
        cancelButton.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> {
            Booking selected = bookingList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    selected.cancel();
                    selected.getRoom().setAvailable(true);
                    bookings.remove(selected);
                    updateAvailableRooms();
                    showAlert("Success", "Booking #" + selected.getBookingId() + " cancelled successfully");
                } catch (Booking.BookingOperationException ex) {
                    showAlert("Error", ex.getMessage());
                }
            } else {
                showAlert("Error", "Please select a booking to cancel");
            }
        });

        VBox bookingListBox = new VBox(10, bookingList, cancelButton);
        bookingListBox.setPadding(new Insets(10));

        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to right, #e6e9f0, #eef2f5);");
        mainLayout.getChildren().addAll(form, bookingListBox);

        tab.setContent(mainLayout);
        return tab;
    }

    private void handleBookingSubmission(
        TextField customerNameField, TextField emailField, ComboBox<String> roomTypeCombo,
        DatePicker checkInPicker, DatePicker checkOutPicker,
        CheckBox roomServiceCheck, CheckBox laundryCheck, CheckBox spaCheck,
        ToggleGroup paymentGroup
    ) {
        try {
            // Validate inputs
            if (customerNameField.getText().isEmpty() || emailField.getText().isEmpty()) {
                throw new IllegalArgumentException("Customer name and email are required");
            }

            if (roomTypeCombo.getValue() == null) {
                throw new IllegalArgumentException("Please select a room type");
            }

            if (checkInPicker.getValue() == null || checkOutPicker.getValue() == null) {
                throw new IllegalArgumentException("Please select check-in and check-out dates");
            }

            if (checkInPicker.getValue().isAfter(checkOutPicker.getValue())) {
                throw new IllegalArgumentException("Check-in date must be before check-out date");
            }

            if (paymentGroup.getSelectedToggle() == null) {
                throw new IllegalArgumentException("Please select a payment method");
            }

            // Create customer
            Customer customer = new Customer(
                customerNameField.getText(), 
                emailField.getText(), 
                "tempPassword123"
            );

            // Find available room
            Room selectedRoom = availableRooms.stream()
                .filter(room -> room.getType().equalsIgnoreCase(roomTypeCombo.getValue()))
                .findFirst()
                .orElseThrow(() -> new Exception("No available rooms of selected type"));

            // Convert dates
            Date checkInDate = Date.from(checkInPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date checkOutDate = Date.from(checkOutPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Create booking
            Booking booking = new Booking(
                bookings.size() + 1,
                customer,
                selectedRoom,
                checkInDate,
                checkOutDate
            );

            // Add services
            if (roomServiceCheck.isSelected()) {
                booking.addService(new RoomService(1, "Room Service", "In-room dining", 15.0, "Dinner"));
            }
            if (laundryCheck.isSelected()) {
                booking.addService(new LaundryService(2, "Laundry", "Professional laundry", 10.0, 5));
            }
            if (spaCheck.isSelected()) {
                booking.addService(new SpaService(3, "Spa", "Relaxing treatments", 50.0, "Basic"));
            }

            // Process payment (simulated)
            if (paymentGroup.getSelectedToggle() != null) {
                selectedRoom.setAvailable(false);
                bookings.add(booking);
                updateAvailableRooms();
                
                // Send notification
                Email_Notification notification = new Email_Notification();
                notification.set_message("Thank you for your booking!\n" +
                    "Booking ID: " + booking.getBookingId() + "\n" +
                    "Room: " + selectedRoom.getRoomNumber() + " (" + selectedRoom.getType() + ")\n" +
                    "Check-in: " + checkInPicker.getValue() + "\n" +
                    "Check-out: " + checkOutPicker.getValue() + "\n" +
                    "Total: $" + booking.getTotalPrice());
                notification.send();
                
                showAlert("Success", "Booking confirmed! Total: $" + booking.getTotalPrice());
            }
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private Tab createRoomsTab() {
        Tab tab = new Tab("Rooms");
        tab.setClosable(false);
        
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #a1c4fd, #c2e9fb);");
        
        Label title = new Label("Available Rooms");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#3a4a6d"));
        
        // Filter controls
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter by Type:");
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All", "Single", "Double", "Suite");
        filterCombo.setValue("All");
        
        filterCombo.setOnAction(e -> {
            String selectedType = filterCombo.getValue();
            if (selectedType.equals("All")) {
                updateAvailableRooms();
            } else {
                availableRooms.setAll(hotel.getRooms().stream()
                    .filter(room -> room.getType().equalsIgnoreCase(selectedType) && room.isAvailable())
                    .collect(Collectors.toList()));
            }
        });
        
        filterBox.getChildren().addAll(filterLabel, filterCombo);
        
        // Room cards
        FlowPane roomCards = new FlowPane(20, 20);
        roomCards.setAlignment(Pos.CENTER);
        
        // Bind to available rooms
        availableRooms.addListener((javafx.collections.ListChangeListener.Change<? extends Room> c) -> {
            roomCards.getChildren().clear();
            availableRooms.forEach(room -> {
                VBox card = createRoomCard(room);
                roomCards.getChildren().add(card);
            });
        });
        
        // Initial population
        availableRooms.forEach(room -> {
            VBox card = createRoomCard(room);
            roomCards.getChildren().add(card);
        });
        
        layout.getChildren().addAll(title, filterBox, roomCards);
        tab.setContent(layout);
        return tab;
    }
    
    private VBox createRoomCard(Room room) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 5, 0, 0);");
        card.setPrefWidth(250);
        
        Label roomLabel = new Label("Room #" + room.getRoomNumber());
        roomLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        roomLabel.setTextFill(Color.web("#3a4a6d"));
        
        Label typeLabel = new Label("Type: " + room.getType());
        Label priceLabel = new Label("Price: $" + room.getPrice() + "/night");
        Label capacityLabel = new Label("Capacity: " + room.getCapacity() + " person(s)");
        Label statusLabel = new Label(room.isAvailable() ? "Available" : "Occupied");
        statusLabel.setTextFill(room.isAvailable() ? Color.GREEN : Color.RED);
        
        Button bookButton = new Button("Book Now");
        bookButton.setStyle("-fx-background-color: #4a47a3; -fx-text-fill: white;");
        bookButton.setDisable(!room.isAvailable());
        
        bookButton.setOnAction(e -> {
            TabPane tabPane = (TabPane) bookButton.getScene().getRoot();
            tabPane.getSelectionModel().select(0); // Switch to booking tab
            showAlert("Info", "Please complete your booking for Room #" + room.getRoomNumber());
        });
        
        card.getChildren().addAll(roomLabel, typeLabel, priceLabel, capacityLabel, statusLabel, bookButton);
        return card;
    }

    private Tab createServicesTab() {
        Tab tab = new Tab("Services");
        tab.setClosable(false);
        
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #f6d365, #fda085);");
        
        Label title = new Label("Our Services");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#d35400"));
        
        // Service cards
        VBox serviceCards = new VBox(15);
        
        // Room Service card
        HBox roomServiceCard = createServiceCard("Room Service", "Enjoy meals in your room", 150.0, "Meal Type:");
        HBox laundryCard = createServiceCard("Laundry Service", "Professional laundry service", 100.0, "Clothes Count:");
        HBox spaCard = createServiceCard("Spa Service", "Relaxing spa treatments", 50.0, "Package:");
        
        serviceCards.getChildren().addAll(roomServiceCard, laundryCard, spaCard);
        layout.getChildren().addAll(title, serviceCards);
        tab.setContent(layout);
        return tab;
    }
    
    private HBox createServiceCard(String name, String description, double price, String detailLabel) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");
        card.setAlignment(Pos.CENTER_LEFT);
        
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web("#d35400"));
        
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        Label priceLabel = new Label("$" + price);
        
        infoBox.getChildren().addAll(nameLabel, descLabel, priceLabel);
        
        VBox detailBox = new VBox(5);
        detailBox.setPadding(new Insets(0, 0, 0, 20));
        detailBox.getChildren().add(new Label(detailLabel));
        
        TextField detailField = new TextField();
        detailField.setPromptText("Enter details...");
        detailBox.getChildren().add(detailField);
        
        Button addButton = new Button("Add to Booking");
        addButton.setStyle("-fx-background-color: #d35400; -fx-text-fill: white;");
        
        addButton.setOnAction(e -> {
            TabPane tabPane = (TabPane) addButton.getScene().getRoot();
            tabPane.getSelectionModel().select(0); // Switch to booking tab
            showAlert("Service Added", name + " service will be added to your booking");
        });
        
        card.getChildren().addAll(infoBox, detailBox, addButton);
        return card;
    }

    private Tab createReviewsTab() {
        Tab tab = new Tab("Reviews");
        tab.setClosable(false);
        
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #84fab0, #8fd3f4);");
        
        Label title = new Label("Customer Reviews");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#1e8449"));
        
        // Review form
        VBox reviewForm = new VBox(10);
        reviewForm.setPadding(new Insets(15));
        reviewForm.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        Label formTitle = new Label("Write a Review");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        
        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
        ratingCombo.setPromptText("Rating (1-5)");
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Your review...");
        commentArea.setPrefRowCount(3);
        
        Button submitButton = new Button("Submit Review");
        submitButton.setStyle("-fx-background-color: #1e8449; -fx-text-fill: white;");
        submitButton.setOnAction(e -> {
            try {
                if (nameField.getText().isEmpty()) {
                    throw new IllegalArgumentException("Please enter your name");
                }
                if (ratingCombo.getValue() == null) {
                    throw new IllegalArgumentException("Please select a rating");
                }
                if (commentArea.getText().isEmpty()) {
                    throw new IllegalArgumentException("Please write your review");
                }
                
                Customer customer = new Customer(nameField.getText(), "guest@example.com", "password123");
                Review review = new Review(
                    reviews.size() + 1,
                    customer,
                    ratingCombo.getValue(),
                    commentArea.getText(),
                    new Date()
                );
                
                hotel.addReview(review);
                reviews.add(review);
                
                showAlert("Thank You", "Your review has been submitted!");
                
                // Clear form
                nameField.clear();
                ratingCombo.getSelectionModel().clearSelection();
                commentArea.clear();
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });
        
        reviewForm.getChildren().addAll(formTitle, nameField, ratingCombo, commentArea, submitButton);
        
        // Review list
        ListView<Review> reviewList = new ListView<>(reviews);
        reviewList.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        reviewList.setCellFactory(param -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(createReviewItem(review));
                }
            }
        });
        
        layout.getChildren().addAll(title, reviewForm, reviewList);
        tab.setContent(layout);
        return tab;
    }
    
    private VBox createReviewItem(Review review) {
        VBox reviewItem = new VBox(5);
        reviewItem.setPadding(new Insets(10));
        reviewItem.setStyle("-fx-border-color: #d5f5e3; -fx-border-width: 1; -fx-border-radius: 5;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(review.getCustomer().getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Label ratingLabel = new Label("Rating: " + review.getRating() + "/5");
        ratingLabel.setTextFill(Color.web("#1e8449"));
        
        Label dateLabel = new Label(review.getDate().toString());
        dateLabel.setStyle("-fx-text-fill: #666;");
        
        header.getChildren().addAll(nameLabel, ratingLabel, dateLabel);
        
        Label commentLabel = new Label(review.getComment());
        commentLabel.setWrapText(true);
        
        reviewItem.getChildren().addAll(header, commentLabel);
        return reviewItem;
    }

    private Tab createAdminTab() {
        Tab tab = new Tab("Admin");
        tab.setClosable(false);
        
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(20));
        layout.setHgap(15);
        layout.setVgap(15);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");
        
        Label title = new Label("Administration Panel");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        GridPane.setConstraints(title, 0, 0, 2, 1);
        
        // Login form
        VBox loginBox = new VBox(10);
        loginBox.setPadding(new Insets(15));
        loginBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");
        
        Label loginTitle = new Label("Admin Login");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white;");
        loginButton.setOnAction(e -> {
            if ("admin".equals(usernameField.getText()) && "admin123".equals(passwordField.getText())) {
                loginBox.setVisible(false);
                showAlert("Success", "Login successful!");
            } else {
                showAlert("Error", "Invalid username or password");
            }
        });
        
        loginBox.getChildren().addAll(loginTitle, usernameField, passwordField, loginButton);
        GridPane.setConstraints(loginBox, 0, 1);
        
        layout.getChildren().addAll(title, loginBox);
        tab.setContent(layout);
        return tab;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }

}