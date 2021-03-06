package com.nullpointerexception.collabmode.controller;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.nullpointerexception.collabmode.application.Main;
import com.nullpointerexception.collabmode.service.HTTPRequestManager;
import com.nullpointerexception.collabmode.service.Serializer;
import com.nullpointerexception.collabmode.util.EmailUtils;
import com.nullpointerexception.collabmode.util.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegisterController {
    // Matching variables
    @FXML private JFXTextField fullNameInput;
    @FXML private JFXTextField emailInput;
    @FXML private JFXPasswordField passwordInput;
    @FXML private JFXPasswordField passwordConfirmInput;
    @FXML private JFXCheckBox tosAgree;
    @FXML private JFXCheckBox newsletterAgree;
    @FXML private Button signUpButton;
    @FXML private WebView imageSlideShow;
    @FXML private Hyperlink tosHyperlink;
    @FXML private Hyperlink newsletterHyperlink;

    @FXML private Button switchToLogInButton;

    // NewsletterStatus set to false (default)
    private boolean newsletterStatus = false;

    @FXML public void initialize(){
        // Load the WebView for the image slideshow
        WebEngine webEngine = imageSlideShow.getEngine();
        webEngine.load(HTTPRequestManager.SERVER_LOCATION + "/imageSwitcher");

        switchToLogInButton.setOnAction(event -> {
            try {
                Main.openLoginStage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        tosHyperlink.setOnAction(event -> Main.getHostService().showDocument(HTTPRequestManager.SERVER_LOCATION +"/tos"));

        newsletterHyperlink.setOnAction(event -> Main.getHostService().showDocument(HTTPRequestManager.SERVER_LOCATION +"/newsletter"));

        // Data validation
        signUpButton.setOnAction(event -> {
            String fullName = fullNameInput.getText().trim();
            String email = emailInput.getText().trim();
            String password = passwordInput.getText().trim();
            String passwordConfirm = passwordConfirmInput.getText().trim();

            if(fullName.isEmpty()){
                fullNameInput.setFocusColor(Color.RED);
                fullNameInput.setUnFocusColor(Color.RED);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Name error");
                alert.setContentText("Full name field can't be empty!");
                alert.showAndWait();
                return;
            }
            if(!EmailUtils.isValid(email)){
                emailInput.setFocusColor(Color.RED);
                emailInput.setUnFocusColor(Color.RED);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Email address error");
                alert.setContentText("Invalid email address!");
                alert.showAndWait();
                return;
            }
            List<String> errors = new ArrayList<String>();
            if(!PasswordUtils.isSecure(password, passwordConfirm, errors)) {
                passwordInput.setUnFocusColor(Color.RED);
                passwordConfirmInput.setUnFocusColor(Color.RED);
                passwordInput.setFocusColor(Color.RED);
                passwordConfirmInput.setFocusColor(Color.RED);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Password error");
                alert.setContentText(errors.get(0));
                alert.showAndWait();
                return;
            }
            if(!tosAgree.isSelected()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Terms of service agreement error");
                alert.setContentText("You must accept CollabMode™'s  TOS before registering!");
                alert.showAndWait();
                return;
            }
            if(newsletterAgree.isSelected()){
                newsletterStatus = true;
            }

            password = PasswordUtils.hash(password);
            JSONObject json = new JSONObject();
            json.put("fullName", fullName);
            json.put("email", email);
            json.put("password", password);
            json.put("newsletterStatus", newsletterStatus);
            HTTPRequestManager httpRequestManager = new HTTPRequestManager();
            try {
                String response = httpRequestManager.sendJSONRequest(HTTPRequestManager.SERVER_LOCATION + "/register",  json.toString());
                JSONObject responseToJson = new JSONObject(response);
                if(!responseToJson.get("status").toString().equals("ok")){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Registration error");
                    alert.setContentText(responseToJson.get("errorMessage").toString());
                    alert.showAndWait();
                    return;
                }else{
                    Serializer serializer = new Serializer();
                    System.out.println("Here");
                    serializer.serializeToken(responseToJson.get("token").toString());
                    Main.openDashboardStage(responseToJson.get("token").toString(), "Java");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
    public Button getSwitchToLogInButton() {
        return switchToLogInButton;
    }
}
