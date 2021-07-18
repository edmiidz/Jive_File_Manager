package com.a.e.qurbanzada;

/**
 * Created by aqn3130 on 11/01/2018.
 */

import javafx.application.Application;
import java.awt.event.ActionListener;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Login extends Application {

    private String user = null;
    private int response_code = 0;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25,25,25,25));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setAlignment(Pos.CENTER);

        Label community_url = new Label("Community URL");
        TextField community_url_textField = new TextField();
        community_url_textField.setPromptText("For example: community.company.com");
        community_url_textField.setPrefWidth(300);
        community_url_textField.setFocusTraversable(false);

        Label username_label = new Label("Username");
        TextField username_textField = new TextField();
        username_textField.setPrefWidth(300);

        Label password_label = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(300);

        Button btn_login = new Button("Login");

        HBox hBox = new HBox();
        hBox.setStyle("-fx-background-color: DAE6F3;");
        hBox.setMaxWidth(400);
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(12));
        Label label_pin = new Label("PIN");
        hBox.getChildren().add(label_pin);

        TextField[] pin = new TextField[6];
        for (int j=0;j<pin.length;j++){
            pin[j] = new TextField();
            pin[j].setMaxSize(40,40);
            JiveFileManager.textLimiter(pin[j]);
            hBox.getChildren().add(pin[j]);
        }

        gridPane.add(community_url,0,0);
        gridPane.add(community_url_textField,1,0,2,1);

        gridPane.add(username_label,0,1);
        gridPane.add(username_textField,1,1,2,1);

        gridPane.add(password_label,0,2);
        gridPane.add(passwordField,1,2,2,1);

        gridPane.add(btn_login,1,5);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(20));
        borderPane.setLeft(gridPane);
        borderPane.setRight(hBox);

//        pin[5].setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                StringBuilder stringBuilder = getPin(pin);
//                ProgressDialog.ProgressForm pinLogin_progress = new ProgressDialog.ProgressForm();
//                Task<Void> pinLogin_task = new Task<Void>() {
//                    @Override
//                    protected Void call() throws Exception {
//                        if (stringBuilder.length() == 6){
//                            updateProgress(-1,-1);
//                            String line;
//                            String key = null;
//                            String username = null;
//                            String password = null;
//                            String communityUrl = null;
//                            try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/Users/"+System.getProperty("user.name")+"/pin.txt"))){
//                                while ( (line = bufferedReader.readLine() ) != null ) {
//                                    String[] parts = line.split(":", 4 );
//                                    if ( parts.length >= 4 ) {
//                                        key = parts[0];
//                                        username = parts[1];
//                                        password = parts[2];
//                                        communityUrl = parts[3];
//
//                                        if (key.equals(stringBuilder)){
//                                            break;
//                                        }
//                                    } else {
//                                        System.out.println( "ignoring line: " + line);
//                                    }
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            if (stringBuilder.toString().equals(key)){
//                                authenticate(username,password,communityUrl);
//                                launchApp(communityUrl,username,password,primaryStage);
//                            }
//                            else {
//                                Alert alert = new Alert(Alert.AlertType.ERROR);
//                                alert.setTitle("Pin login error");
//                                alert.setContentText("Incorrect PIN");
//                                alert.show();
//                            }
//                        }
//                        updateProgress(1,1);
//                        return null;
//                    }
//                };
//                pinLogin_progress.activateProgressBar(pinLogin_task);
//                pinLogin_task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//                    @Override
//                    public void handle(WorkerStateEvent event) {
//                        pinLogin_progress.getDialogStage().close();
//                        btn_login.setDisable(false);
//                        for (int i = 0; i < pin.length; i++){
//                            pin[i].setDisable(false);
//                        }
//                    }
//                });
//                pinLogin_task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
//                    @Override
//                    public void handle(WorkerStateEvent event) {
//
//                        pinLogin_progress.getDialogStage().close();
//                        btn_login.setDisable(false);
//                        for (int i = 0; i < pin.length; i++){
//                            pin[i].setDisable(false);
//                        }
//                    }
//                });
//
//                btn_login.setDisable(true);
//                for (int i = 0; i < pin.length; i++){
//                    pin[i].setDisable(true);
//                }
//                pinLogin_progress.getDialogStage().show();
//                Thread pinLogin_thread = new Thread(pinLogin_task);
//                pinLogin_thread.start();
//            }
//        });

        pin[5].setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                StringBuilder stringBuilder = getPin(pin);
                if (stringBuilder.length() == 6){
                    String line;
                    String key = null;
                    String username = null;
                    String password = null;
                    String communityUrl = null;
                    try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/Users/"+System.getProperty("user.name")+"/pin.txt"))){
                        while ( (line = bufferedReader.readLine() ) != null ) {
                            String[] parts = line.split(":", 4 );
                            if ( parts.length >= 4 ) {
                                key = parts[0];
                                username = parts[1];
                                password = parts[2];
                                communityUrl = parts[3];

                                if (key.equals(stringBuilder)){
                                    break;
                                }
                            } else {
                                System.out.println( "ignoring line: " + line);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (stringBuilder.toString().equals(key)){
                        authenticate(username,password,communityUrl);
                        launchApp(communityUrl,username,password,primaryStage);
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Pin login error");
                        alert.setContentText("Incorrect PIN");
                        alert.show();
                    }
                }
            }
        });

        btn_login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if(username_textField.getText().isEmpty() && passwordField.getText().isEmpty()){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Username Password or PIN field can not be blank");
                    alert.setTitle("Login Information");
                    alert.show();
                }
                else{
                    authenticate(username_textField.getText(), passwordField.getText(), community_url_textField.getText());

                    if(user == null){
                        Alert failed = new Alert(Alert.AlertType.INFORMATION);
                        failed.setTitle("Login Failed");
                        if(response_code == 401){
                            failed.setHeaderText("Not authorised, please contact Jive Support");
                        }
                        else if(response_code == 404){
                            failed.setHeaderText("No such account, please contact Jive Support");
                        }
                        else{
                            failed.setHeaderText("Please contact Jive Support "+ response_code);
                        }
                        failed.show();
                    }
                    else
                    {
                        launchApp(community_url_textField.getText(), username_textField.getText(), passwordField.getText(), primaryStage);
                    }
                }
            }
        });

        Scene scene = new Scene(borderPane,800,500);
        primaryStage.setTitle("Login to Jive File Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void authenticate(String username, String password, String community_url) {
        new TempFiles();
        Get authenticate = new Get();
        try {
            Get.setUsername(username);
            Get.setPassword(password);

            authenticate.get("https://" + community_url + "/api/core/v3/people/username/" + username,null);

            user = authenticate.getUser();
            response_code = authenticate.getResponse_code();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private StringBuilder getPin(TextField[] pin) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<pin.length;i++){
            stringBuilder.append(pin[i].getText());
        }
        return stringBuilder;
    }

    private void launchApp(String community_url, String username, String password, Stage primaryStage) {
        JiveFileManager.setCommUrl(community_url);
        MultiPartFileUploader.setCommunity_url(community_url);
        Helper.setCommunityURL(community_url);

        Get.setUsername(username);
        Get.setPassword(password);

        MultipartUtility.setUsername(username);
        MultipartUtility.setPassword(password);

        DeleteFilesInJive.setUsername(username);
        DeleteFilesInJive.setPassword(password);

        JiveFileManager jiveFileManager = new JiveFileManager();
        try {
            jiveFileManager.start(primaryStage);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
