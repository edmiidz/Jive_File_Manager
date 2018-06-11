package com.a.e.qurbanzada;

import com.sun.jersey.core.util.Base64;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.json.JSONException;
//import sun.nio.ch.IOUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

public class JiveFileManager extends Application {

    private String plcID=null;
    private File sourceDir=null;
    private String moveFrom_placeID=null;
    private String moveTo_placeID=null;
    private Map<String,String> mapID = new HashMap<>();
    private Helper helperDelete,helperMove = null;
    private Button uploadToJive;
    private TextField categoryField;
    private TextField tagField;
    private TextField moveFromTextField;
    private Button btn_move;
    private TextField delField;
    private Button btn_deleteFiles;
    private GridPane pane_get;
    private GridPane root;
    private TextField plc_field;
    private ComboBox comboBox;
    private ObservableList<String> categories;
    private static String community_url;
    private Get getPlaceURI;
    static void setCommUrl(String url){
        community_url = url;
    }
    private Label response = new Label("");
    private TableView<Content> tvContents;

    @Override
    public void start(Stage primaryStage) throws JSONException, IOException {
        //***************************** GridPane for Tab-Delete *****************************

        root = new GridPane();
        root.setAlignment(Pos.TOP_CENTER);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(25,25,25,25));
        root.gridLinesVisibleProperty().setValue(false);

        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.TOP_LEFT);

        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.TOP_LEFT);

        HBox hBox1 = new HBox(10);
        hBox1.setAlignment(Pos.TOP_LEFT);


        Label delPlaceName = new Label("Place Name");
        delField = new TextField();
        delField.setPromptText("Name of a Jive place");

        //Get files button and handler
        Button btn_getFiles = new Button();
        btn_getFiles.setText("View Content");

        Button btn_searchDel = new Button("Find");

        //Search files to be deleted button and handler
        btn_searchDel.setOnAction( event -> searchPlaceName( delField ) );

        delField.setOnKeyReleased(event -> delField.setStyle( "-fx-control-inner-background: white" ) );

        getFiles( response , root , plcID);
        //View content to be deleted button and handler
        btn_getFiles.setOnAction(event -> getFiles( response , root , plcID) );


        btn_deleteFiles = new Button();
        btn_deleteFiles.setText("Delete");
        btn_deleteFiles.setDisable(true);

        Label totalDeleted = new Label("");

        btn_deleteFiles.setOnAction(event -> {

            Alert alert_del = new Alert(Alert.AlertType.CONFIRMATION);
            alert_del.setTitle("Bulk remove confirmation!");
            alert_del.setContentText("Are you sure!");
            Optional<ButtonType> buttonTypeOptional_del = alert_del.showAndWait();

            delete(buttonTypeOptional_del);
        });

        //************************** Move files Tab *******************************

        GridPane moveGrid = new GridPane();
        moveGrid.setAlignment(Pos.CENTER);
        moveGrid.setHgap(10);
        moveGrid.setVgap(10);
        moveGrid.setPadding(new Insets(25,25,25,25));
        moveGrid.gridLinesVisibleProperty().setValue(false);

        Label moveFrom = new Label("Move From");

        moveFromTextField = new TextField();
        moveFromTextField.setPromptText("Jive place name to move from");
        moveFromTextField.setOnKeyReleased(event -> moveFromTextField.setStyle( "-fx-control-inner-background: white" ) );

        Button btn_moveFrom = new Button("Find");

        Label moveTo = new Label("Move To");
        TextField moveToTextField = new TextField();
        moveToTextField.setPromptText("Jive place name to move to");
        moveToTextField.setOnKeyReleased(event -> moveToTextField.setStyle( "-fx-control-inner-background: white" ) );

        Button btn_moveTo = new Button("Find");
        btn_move = new Button("Move");
        Label label_status = new Label("");

        moveGrid.add(moveFrom,0,0);
        moveGrid.add(moveFromTextField,1,0);
        moveGrid.add(btn_moveFrom,2,0);

        moveGrid.add(moveTo,0,1);
        moveGrid.add(moveToTextField,1,1);
        moveGrid.add(btn_moveTo,2,1);

        moveGrid.add(btn_move,0,2);
        moveGrid.add(label_status,2,2);

        //Find place for move from
        btn_moveFrom.setOnAction(event -> moveFrom_placeID = searchPlaceName( moveFromTextField ) );

        //Find place for move to
        btn_moveTo.setOnAction(event -> moveTo_placeID = searchPlaceName( moveToTextField ) );

        //Move button handler
        btn_move.setOnAction(event -> move());

        //*********************** GridPane for Tab-Upload *************************

        pane_get = new GridPane();
        pane_get.setAlignment(Pos.CENTER);
        pane_get.setHgap(10);
        pane_get.setVgap(10);
        pane_get.setPadding(new Insets(25,25,25,25));
        pane_get.gridLinesVisibleProperty().setValue(false);

        Label plc_name = new Label("Place Name");
        plc_field = new TextField();
        plc_field.setOnKeyReleased(event -> plc_field.setStyle("-fx-control-inner-background: white"));

        pane_get.add(plc_name,0,0);
        pane_get.add(plc_field,1,0);

        Button btn_getPlace = new Button("Find");
        pane_get.add(btn_getPlace,2,0);

        Label plcFound = new Label("");
        pane_get.add(plcFound,2,7);

        setCategoriesComboBox(null);

        Button createCategory = new Button("Add");
        categoryField = new TextField();
        categoryField.setPromptText("Enter new category name");
        categoryField.setOnMouseClicked(event -> comboBox.setValue( null ) );

        Label new_cat_label = new Label("New Category");
        pane_get.add(new_cat_label,0,3);
        pane_get.add(createCategory,2,3);
        pane_get.add(categoryField,1,3);

        Label tag = new Label("Tags");
        pane_get.add(tag,0,4);

        tagField = new TextField();
        tagField.setPromptText("Enter tags");
        pane_get.add(tagField,1,4);

        Separator separator = new Separator();
        separator.setHalignment(HPos.CENTER);
        GridPane.setConstraints(separator,0,5);
        GridPane.setColumnSpan(separator,8);
        pane_get.add(separator,0,5);

        //********************************** Create New Category ********************

        createCategory.setOnAction(event -> {
            if ( categoryField.getText().isEmpty() ){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setContentText( "A name required for new category" );
                alert.showAndWait();
            }
            else {
                if( plcID != null ){
                    try {
                        MultipartUtility multipartUtility = new MultipartUtility("https://" + community_url + "/api/core/v3/places/" + plcID + "/categories",101);
                        multipartUtility.addJSONData("{ 'name' : " + categoryField.getText() + " }");
//                        String response1 = multipartUtility.finish();
                        if ( multipartUtility.getHttpConn().getResponseCode() == 201 ){
                            categoryField.setStyle( "-fx-control-inner-background: green" );
                            comboBox.setValue(null);
                        }
                        else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setContentText(multipartUtility.getHttpConn().getResponseMessage());
                            alert.showAndWait();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle( "Information Dialog" );
                    alert.setContentText( "Select a place first" );
                    alert.showAndWait();
                }
            }
        });

        //********************** Search and find upload place in Jive ***************

        btn_getPlace.setOnAction(event -> {
            if ( plc_field.getText().equals( "" ) ) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle( "Information Dialog" );
                alert.setHeaderText( null );
                alert.setContentText( "Type a valid Jive place name!" );
                alert.showAndWait();
            }
            else{
                searchPlaceName(plc_field);
            }
        });

        //*********************** Get file source Button ************************************

        Button btn_getDirectory = new Button("Source Directory");
        btn_getDirectory.setOnAction(event -> {
            if(plcID == null){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Search and find a Jive place first");
                alert.showAndWait();
            }
            else{
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Upload to Jive");
                File defaultDir = new File("/Users/" + System.getProperty("user.name") + "/Downloads");
                chooser.setInitialDirectory(defaultDir);

                sourceDir = chooser.showDialog(primaryStage);
                System.out.println(sourceDir);
            }

        });
        pane_get.add(btn_getDirectory,0,6);

        //*********************** Upload to Jive Button ***************************************

        uploadToJive = new Button("Upload");
        uploadToJive.setOnAction(event -> upload());

        pane_get.add(uploadToJive,0,7);

        //**************************** Tab Analytics ******************************************

        GridPane grid_people_content = new GridPane();
        grid_people_content.setAlignment(Pos.TOP_LEFT);
//        grid_people_content.setVgap(20);
        grid_people_content.setHgap(20);
        grid_people_content.setPadding(new Insets(20,20,20,20));

        ComboBox comboBox = new ComboBox();
        List<String> options_combo = new ArrayList<>();
        options_combo.add("Select");
        options_combo.add("Actors");
        options_combo.add("Places");
        ObservableList<String> observableList_options = FXCollections.observableArrayList(options_combo);
        comboBox.setItems(observableList_options);
        comboBox.getSelectionModel().selectFirst();

        List<String> filters_list = new ArrayList<>();
        filters_list.add("Filter");
        filters_list.add("all content");
        filters_list.add("file");
        filters_list.add("document");
        filters_list.add("blog");
        ObservableList<String> observableList_filters = FXCollections.observableArrayList(filters_list);

        ChoiceBox choiceBox_filter = new ChoiceBox();
        choiceBox_filter.setItems(observableList_filters);
        choiceBox_filter.getSelectionModel().selectFirst();
        choiceBox_filter.setDisable(true);

        ListView analytics_listView = new ListView();
        analytics_listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        analytics_listView.setVisible(false);

        Label people_selected_row = new Label("");
        Button content_button = new Button("View Content");
        content_button.setVisible(false);

        Label selected_row = new Label("");

        Button export_content_people = new Button("Export");
        export_content_people.setVisible(false);

        ListView<String> listView_places = new ListView<>();
        listView_places.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView_places.setMaxSize(200,300);
        listView_places.setEditable(false);

        Button download_button = new Button("Download");
        download_button.setVisible(false);
        TextField filterInput = new TextField();
//        filterInput.setVisible(false);
        List<Content> dummyList = new ArrayList<>();

        getTableView(selected_row,grid_people_content,export_content_people,download_button,dummyList);

        Get get = new Get();
        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Actors")){
                    choiceBox_filter.setDisable(false);

                    ProgressDialog.ProgressForm progressForm = new ProgressDialog.ProgressForm();
                    progressForm.getDialogStage().setTitle("Identifying actors");
                    Task<Void> task_actors = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {

                            try {
                                get.get("https://" + Helper.getCommunityURL() + "/api/core/v3/people/@all?count=100",null);
                                updateProgress(-1,-1);
                                List<String> people_list = new ArrayList<>();

                                for(String s : get.getPeople_map().keySet()){
                                    people_list.add(s);
                                }
                                ObservableList<String> people_observableList = FXCollections.observableArrayList(people_list);
                                FilteredList<String> filteredData = new FilteredList<>(people_observableList, s -> true);
                                filterInput.textProperty().addListener(obs->{
                                    String filter = filterInput.getText();
                                    if(filter == null || filter.length() == 0) {
                                        filteredData.setPredicate(s -> true);
                                    }
                                    else {
                                        filteredData.setPredicate(s -> s.contains(filter));
                                    }
                                });
                                analytics_listView.setItems(filteredData.sorted());

                                content_button.setVisible(true);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            updateProgress(1,1);
                            return null;
                        }
                    };

                    progressForm.activateProgressBar(task_actors);
                    task_actors.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            progressForm.getDialogStage().close();
                            comboBox.setDisable(false);
                        }
                    });
                    task_actors.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            comboBox.setDisable(false);
                            progressForm.getDialogStage().close();
                        }
                    });
                    comboBox.setDisable(true);
                    progressForm.getDialogStage().show();
                    Thread people_thread = new Thread(task_actors);
                    people_thread.start();

                }
                else {
                    choiceBox_filter.setDisable(false);
                    List<String> allPlacesList = new ArrayList<>();

                    if(mapID.isEmpty()){
                        getCache();
                    }

                    for ( String key : mapID.keySet() ){
                        allPlacesList.add(key);
                    }

                    ObservableList<String> all_places_observableList = FXCollections.observableArrayList(allPlacesList);
                    FilteredList<String> filteredList = new FilteredList<>(all_places_observableList, s -> true);
                    filterInput.textProperty().addListener(observable -> {
                        String filter = filterInput.getText();
                        if (filter == null || filter.length() == 0){
                            filteredList.setPredicate(s -> true);
                        }
                        else {
                            filteredList.setPredicate(s -> s.contains(filter));
                        }
                    });
                    analytics_listView.setItems(filteredList.sorted());
                    content_button.setVisible(true);

                }
            }
        });

        analytics_listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String row = String.valueOf(analytics_listView.getSelectionModel().getSelectedIndex());
                people_selected_row.setText("Row " + row);

                if (!analytics_listView.getSelectionModel().getSelectedItems().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("View Content");
                    alert.setContentText("Confirm viewing content");

                    Optional<ButtonType> confirm_view = alert.showAndWait();

                    if(confirm_view.get() == ButtonType.OK){
                        getTableView_CP(selected_row, analytics_listView, get, grid_people_content, export_content_people, choiceBox_filter , download_button);
                    }
                    else{

                    }
                }
            }
        });


        download_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm content download");
                alert.setContentText("Confirm download");
                Optional<ButtonType> option = alert.showAndWait();

                if (option.get() == ButtonType.OK){
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Download from Jive");
                    directoryChooser.setInitialDirectory(new File("/Users/" + System.getProperty("user.name") + "/Downloads"));
                    File directory = directoryChooser.showDialog(primaryStage);
                    download(directory , download_button);
                }
                else {

                }
            }
        });

        grid_people_content.add(comboBox,0,0);
        grid_people_content.add(choiceBox_filter,1,0);
        grid_people_content.add(filterInput,2,0, 4,1);
        grid_people_content.add(analytics_listView,2,1, 4,4);
        grid_people_content.add(people_selected_row,2,5);

        grid_people_content.add(export_content_people,0,8);
        grid_people_content.add(download_button,1,8);
        grid_people_content.add(selected_row,2,13);

        //**************************** Tab Account *********************************

        GridPane account_gridPane = new GridPane();
        account_gridPane.setAlignment(Pos.TOP_LEFT);
        account_gridPane.setHgap(10);
        account_gridPane.setPadding(new Insets(25,25,25,25));
        Label userName = new Label(Get.getUser());
        userName.setTextFill(Color.WHITESMOKE);

        List<Label> labels = new ArrayList<>();

        if ( labels.isEmpty()){
            for (int i=0; i< Get.getUserDetail().size();i++){
                labels.add(i,new Label());
                labels.get(i).setText(Get.getUserDetail().get(i));
                labels.get(i).setPadding(new Insets(5));

            }
        }

        for (Node n : account_gridPane.getChildren()){
            for ( Label l : labels){
                if ( n == l){
                    account_gridPane.getChildren().remove(n);
                }
            }

        }
        for (int l=0; l<labels.size();l++){
            account_gridPane.add(labels.get(l),0,l+1);
        }

        Button logOff_btn = new Button("Log Off");
        logOff_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Login login = new Login();
                login.start(primaryStage);
            }
        });

        HBox account_hBox_top = new HBox();
        account_hBox_top.setPadding(new Insets(15,12,15,12));
        account_hBox_top.setSpacing(20);
        account_hBox_top.setStyle("-fx-background-color: #336699;");

        File file = getAvatar(Get.getAvatarUrl());
        Image image = new Image(file.toURI().toString(),100,100,true,true);

        ImageView avatar = new ImageView();
        avatar.setImage(image);
        Circle circle = new Circle(50);
        circle.setFill(Color.valueOf("#336699"));
        circle.setFill(new ImagePattern(image,0,0,1,1,true));

        account_hBox_top.getChildren().add(circle);
        account_hBox_top.getChildren().add(userName);

        HBox account_hBox_bottom = new HBox();
        account_hBox_bottom.setPadding(new Insets(15,12,15,12));
        account_hBox_bottom.setSpacing(10);
        account_hBox_bottom.setStyle("-fx-background-color: #336699;");
        account_hBox_bottom.getChildren().add(logOff_btn);

        HBox pin_hBox = new HBox();
        pin_hBox.setPadding(new Insets(10));
        pin_hBox.setSpacing(10);
        pin_hBox.setStyle("-fx-background-color: DAE6F3;");
        pin_hBox.setMaxWidth(400);

        Button pin_btn = new Button("Set PIN");

        TextField[] pin = new TextField[6];
        for (int j=0;j<pin.length;j++){
            pin[j] = new TextField();
            pin[j].setMaxSize(40,40);
            textLimiter(pin[j]);
            pin_hBox.getChildren().add(pin[j]);
        }
        pin_hBox.getChildren().add(pin_btn);

        BorderPane account_borderPane = new BorderPane();
        account_borderPane.setTop(account_hBox_top);
        account_borderPane.setLeft(account_gridPane);
        account_borderPane.setBottom(account_hBox_bottom);
        account_borderPane.setRight(pin_hBox);

        pin_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                StringBuilder pin_string = new StringBuilder();
                for (TextField tf : pin){
                    pin_string.append(tf.getText());
                }

                try (FileWriter fileWriter = new FileWriter( "/Users/"+System.getProperty("user.name")+"/pin.txt",true )){
                    fileWriter.write(String.valueOf(pin_string) + ":" + MultipartUtility.getUsername() + ":" + MultipartUtility.getPassword() + ":" + community_url + "\n");
                    fileWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //**************************** Tabs javafx *********************************

        TabPane tabPane = new TabPane();

        Tab tab_get = new Tab("Upload");
        tab_get.setContent(pane_get);
        tab_get.setClosable(false);

        Tab tab_delete = new Tab("Edit");
        tab_delete.setContent(root);
        tab_delete.setClosable(false);

        Tab tab_move = new Tab("Move");
        tab_move.setContent(moveGrid);
        tab_move.setClosable(false);

        Tab tab_analytics = new Tab("Content");
        tab_analytics.setContent(grid_people_content);
        tab_analytics.setClosable(false);

        Tab tab_account = new Tab("Account");
        tab_account.setContent(account_borderPane);
        tab_account.setClosable(false);

        tabPane.getTabs().add(tab_get);
        tabPane.getTabs().add(tab_delete);
        tabPane.getTabs().add(tab_move);
        tabPane.getTabs().add(tab_analytics);
        tabPane.getTabs().add(tab_account);

        Scene scene = new Scene(tabPane,900,700);

        hBox.getChildren().add(delPlaceName);
        hBox.getChildren().add(delField);
        hBox.getChildren().add(btn_searchDel);

        vBox.getChildren().add(btn_getFiles);
//        vBox.getChildren().add(btn_deleteFiles);

        root.add(hBox1,0,0);
        root.add(hBox,0,1);
        root.add(vBox,0,2,1,1);
        root.add(btn_deleteFiles,0,6);
        root.add(response,0,4,1,1);

        primaryStage.setTitle( "Jive File Manager [logged in as " + Get.getUser() + "]");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    static void textLimiter(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (textField.getText().length() > 1){
                    String s = textField.getText().substring(0,1);
                    textField.setText(s);
                }
            }
        });
    }

    private void getTableView_CP(Label label, ListView analytics_listView, Get get, GridPane gridPane, Button export_btn, ChoiceBox<String> filters , Button download_btn) {

        Helper helper = null;

        Set<String> placeId_list = new HashSet<>();
        List<String> selectedPeople_list;
        selectedPeople_list = analytics_listView.getSelectionModel().getSelectedItems();

        List <Content> list = new ArrayList<>();

        Map<String,String> people_map;
        people_map = get.getPeople_map();

        List<String> filters_list = new ArrayList<>();

        for(String s : filters.getItems()){
            if (s.equals(filters.getSelectionModel().getSelectedItem())){
                filters_list.add(s);
            }
        }

        if(!analytics_listView.getItems().isEmpty()){

            for (String key : mapID.keySet()){

                if ( analytics_listView.getSelectionModel().getSelectedItems().contains(key) ) {
                    placeId_list.add(mapID.get(key));
                }
            }
        }

        if (!people_map.isEmpty()){
            for (String key : people_map.keySet()){
                if ( selectedPeople_list.contains(key) ) {
                    list.addAll(getTableViewData(helper, people_map.get(key), key, filters_list));
                }
            }
        }
        if ( !placeId_list.isEmpty() ) {
            for (Object placeID : placeId_list) {
                list.addAll(getTableViewData(placeID.toString(), helper, filters_list));
            }
        }

        getTableView(label, gridPane, export_btn, download_btn, list);
    }

    private void getTableView(Label label, GridPane gridPane, Button export_btn, Button download_btn, List<Content> list) {
        ObservableList<Content> contents_people = FXCollections.observableArrayList(list);
        TableView<Content> tvContents = new TableView<>( contents_people );

        TableColumn<Content, String> docName = new TableColumn<>("Content Title" );
        docName.setCellValueFactory(new PropertyValueFactory<>( "fileName" ) );
        tvContents.getColumns().add( docName );

        TableColumn<Content, String> view_count = new TableColumn<>("View Count" );
        view_count.setCellValueFactory(new PropertyValueFactory<>( "viewCount" ) );
        tvContents.getColumns().add( view_count );

        TableColumn<Content, String> docSize = new TableColumn<>("File Size" );
        docSize.setCellValueFactory( new PropertyValueFactory<>( "fileSize" ) );
        tvContents.getColumns().add( docSize );

        TableColumn<Content, String> docType = new TableColumn<>("Content Type" );
        docType.setCellValueFactory(new PropertyValueFactory<>( "fileType" ) );
        tvContents.getColumns().add( docType );

        TableColumn<Content, String> jivePlace = new TableColumn<>("Place Name" );
        jivePlace.setCellValueFactory(new PropertyValueFactory<>( "placeName" ) );
        tvContents.getColumns().add( jivePlace );

        TableColumn<Content, String> placeType = new TableColumn<>("Place Type" );
        placeType.setCellValueFactory(new PropertyValueFactory<>( "placeType" ) );
        tvContents.getColumns().add( placeType );

        TableColumn<Content, String> person = new TableColumn<>("Author" );
        person.setCellValueFactory(new PropertyValueFactory<>( "person" ) );
        tvContents.getColumns().add( person );

        tvContents.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableView.TableViewSelectionModel<Content> tableViewSelectionModel = tvContents.getSelectionModel();

        ObservableList<Content> finalContents = contents_people;
        tableViewSelectionModel.selectedIndexProperty().addListener(( observableValue , oldValue , newValue ) -> {
            int index = (int) newValue;
            label.setText( "Item NO [" + finalContents.get(index).getRowNum() + "] Item Name [" + finalContents.get(index).getFileName() + "]" );
        });

        for (Node n : gridPane.getChildren()){
            if (n == tvContents){
                gridPane.getChildren().remove(n);
            }
        }

        gridPane.add(tvContents,2,8, 4, 4);

        if(!export_btn.isVisible()){
            export_btn.setVisible(true);
        }
        if (list.isEmpty()){
            export_btn.setDisable(true);
            label.setText("");
        }
        else {
            export_btn.setDisable(false);
        }

        if(!download_btn.isVisible()){
            download_btn.setVisible(true);
        }
        if (list.isEmpty()){
            download_btn.setDisable(true);
        }
        else {
            download_btn.setDisable(false);
        }

        export_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    writeExcel(finalContents);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void writeExcel(ObservableList<Content> contents) throws Exception {
        Writer writer = null;
        try {
            File file = new File(TempFiles.getLogs_directory() + "/HiveUsersContents.csv");
            writer = new BufferedWriter(new FileWriter(file));
            String column_title = "Content Name" + "," + "View Count" + "," + "Content File Size" + "," + "Content Type" + "," + "Place Type" + "," + "Place Name" + "," + "Author" + "\n";
            writer.write(column_title);

            for (Content person : contents) {

                String text = person.getFileName() + "," + person.getViewCount() + "," + person.getFileSize() + "," + person.getFileType() + "," + person.getPlaceType() + "," + person.getPlaceName() + "," + person.getPerson() + "\n";

                writer.write(text);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {

            writer.flush();
            writer.close();
        }
    }

    //Setting categories combo box
    private void setCategoriesComboBox(String plcID) throws JSONException {

        List<String> categories_list = new ArrayList<>();

        if ( plcID != null ) {
            try {
                categories_list.clear();
                categories_list = new Get().get( "https://" + community_url + "/api/core/v3/places/" + plcID + "/categories" ,null);

                categories.setAll(categories_list);
                comboBox.setItems(categories);

                comboBox.setOnMouseClicked(event -> {
                    categoryField.setText("");
                    categoryField.setStyle("-fx-control-inner-background:white");
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            categories = FXCollections.observableArrayList(categories_list);
            comboBox = new ComboBox(categories);
            Label cat_label = new Label("Place Categories" );
            pane_get.add(cat_label,0,2);
            pane_get.add( comboBox,1,2 );
        }
    }

    //********************************* upload method ******************************
    private void upload() {
        if ( sourceDir != null && plcID != null ) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle( "Confirmation Dialog" );
            //alert.setHeaderText("Look, a Confirmation Dialog");
            alert.setContentText( "Are you sure ?" );
            Optional<ButtonType> result = alert.showAndWait();
            String category;

            if ( result.get() == ButtonType.OK ) {

                if ( categoryField.getText().isEmpty() ) {
                    if (comboBox.getSelectionModel().getSelectedIndex() == -1){
                        category = "";
                    }
                    else {
                        category = comboBox.getSelectionModel().getSelectedItem().toString();
                    }

                }
                else {
                    category = categoryField.getText();
                }

                MultiPartFileUploader multiPartFileUploader = new MultiPartFileUploader( plcID, sourceDir.toString(), category, tagField.getText() );
                Runnable runnable = multiPartFileUploader::init;

                Thread thread1 = new Thread(runnable,"MultiPart");
                thread1.start();

                try {
                    thread1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ProgressDialog.ProgressForm pForm = new ProgressDialog.ProgressForm();

                Task<Void> task;

                if ( multiPartFileUploader.files.length > 0 ) {
                    task = new Task<Void>() {
                        @Override
                        public Void call() throws InterruptedException, JSONException {

                            for ( int i = 0; i < multiPartFileUploader.files.length; i ++ ) {
                                if ( !pForm.getDialogStage().isShowing() ) {
                                    cancel();
                                }
                                else {
                                    if ( multiPartFileUploader.mapID.containsKey( multiPartFileUploader.files[i].getName() ) ) {
                                        try {
                                            multiPartFileUploader.update(multiPartFileUploader.mapID, multiPartFileUploader.noUpdate, multiPartFileUploader.updated, i);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        multiPartFileUploader.upload(multiPartFileUploader.uploaded, multiPartFileUploader.notUploaded, multiPartFileUploader.map, i);
                                    }//End else

                                    updateProgress(i, multiPartFileUploader.files.length);
                                    Thread.sleep(200);
                                }
                            }//End for loop

                            multiPartFileUploader.recordUploadedFiles(multiPartFileUploader.map);
                            multiPartFileUploader.log(multiPartFileUploader.updated.toString(),multiPartFileUploader.noUpdate.toString(),multiPartFileUploader.uploaded.toString(),multiPartFileUploader.notUploaded.toString());
                            updateProgress(multiPartFileUploader.files.length, multiPartFileUploader.files.length);

                            return null ;
                        }
                    };
                    pForm.activateProgressBar(task);

                    // this method gets the result of the task and update the UI based on its value:
                    task.setOnSucceeded(e -> {
                        pForm.getDialogStage().close();
                        uploadToJive.setDisable( false );
                    });

                    task.setOnCancelled( event -> {
                        uploadToJive.setDisable( false );
                        pForm.getDialogStage().close();
                        try {
                            if ( multiPartFileUploader.getMultipartUtility().getHttpConn().getResponseCode() == 401 ) {
                                Alert alert_authorisation = new Alert(Alert.AlertType.INFORMATION);
                                alert_authorisation.setHeaderText("You are not authorised, contact Jive Support");
                                alert_authorisation.setTitle("Authorisation Information");
                                alert_authorisation.show();
                            }
                            else if(multiPartFileUploader.getMultipartUtility().getHttpConn().getResponseCode() == 403){
                                Alert alert_authorisation = new Alert(Alert.AlertType.INFORMATION);
                                alert_authorisation.setHeaderText("Forbidden, contact Jive Support");
                                alert_authorisation.setTitle("Authorisation Information");
                                alert_authorisation.show();
                            }
                            else {
                                Alert alert_error = new Alert(Alert.AlertType.ERROR);
                                alert_error.setTitle("Update Error");
                                alert_error.setHeaderText(multiPartFileUploader.getMultipartUtility().getHttpConn().getResponseMessage());
                                alert_error.show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    uploadToJive.setDisable(true);
                    pForm.getDialogStage().show();
                    Thread thread = new Thread(task);
                    thread.start();
                }
                else {
                    Alert alertList = new Alert(Alert.AlertType.INFORMATION);
                    alertList.setTitle("Information Dialog");
                    alertList.setHeaderText(null);
                    alertList.setContentText("Upload list is empty");
                    alertList.showAndWait();
                }

            } else {
                // ... user chose CANCEL or closed the dialog
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Select source directory!");
            alert.showAndWait();
        }
    }

    //****************************** Delete Method *********************************************
    private void delete( Optional<ButtonType> buttonTypeOptional_del ) {
        if ( buttonTypeOptional_del.get() == ButtonType.OK ) {

            Runnable runnableDel = () -> {
                try {
                    helperDelete = new Helper( 101, plcID , null);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };
            Thread threadInitDel = new Thread( runnableDel );
            threadInitDel.start();
            try {
                threadInitDel.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ProgressDialog.ProgressForm progressForm = new ProgressDialog.ProgressForm();
            List<String> contentID_list = new ArrayList<>();
            for (Content id : tvContents.getSelectionModel().getSelectedItems()){
                contentID_list.add(tvContents.getSelectionModel().getSelectedItem().getContentId());
            }


            Task<Void> taskDelete = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
//                    if ( helperDelete.li.size() > 0 ) {
                    if ( contentID_list.size() > 0 ) {
                        helperDelete.statusCode.clear();
//                        for ( int i = 0; i < helperDelete.li.size(); i++ ) {
                        for ( int i = 0; i < contentID_list.size(); i++ ) {
                            if ( !progressForm.getDialogStage().isShowing() ) {
                                cancel();
                            }
                            else {
                                try {
//                                    DeleteFilesInJive deleteFiles = new DeleteFilesInJive(helperDelete.url+ helperDelete.li.get(i), helperDelete.DELETE,null);
                                    DeleteFilesInJive deleteFiles = new DeleteFilesInJive(helperDelete.url+ contentID_list.get(i), helperDelete.DELETE,null);
                                    if ( deleteFiles.getStatus() == 204 ) {
                                        helperDelete.statusCode.add( deleteFiles.getStatus() );
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
//                                updateProgress( i, helperDelete.li.size() );
                                updateProgress( i, contentID_list.size() );
                                Thread.sleep(200);
                            }
                        }
//                        updateProgress(helperDelete.li.size(), helperDelete.li.size());
                        updateProgress(contentID_list.size(), contentID_list.size());
                    }
                    return null;
                }
            };

            progressForm.activateProgressBar( taskDelete );

            taskDelete.setOnSucceeded(event_del_complete -> {
                progressForm.getDialogStage().close();
                btn_deleteFiles.setDisable(false);
                getFiles( response , root , plcID);
            });

            taskDelete.setOnCancelled(eventCanceled -> btn_deleteFiles.setDisable(false));
            btn_deleteFiles.setDisable(true);
            progressForm.getDialogStage().show();

            Thread threadDelete = new Thread(taskDelete);
            threadDelete.start();
        }
        else {

        }
    }
    //****************************** Move Method ***********************************************
    private void move() {
        Alert move_alert = new Alert(Alert.AlertType.CONFIRMATION);
        move_alert.setTitle( "Bulk Move Content" );
        move_alert.setContentText( "Moving contents in bulk" );
        Optional<ButtonType> buttonTypeOptional_move = move_alert.showAndWait();

        if ( buttonTypeOptional_move.get() == ButtonType.OK ) {
            Runnable runnableMove = () -> {
                try {
//                    helperMove = new Helper( 101, moveFrom_placeID , moveFromTextField.getText() );
                    helperMove = new Helper( 101, moveFrom_placeID, null );
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };

            Thread threadInitMove = new Thread(runnableMove);
            threadInitMove.start();
            try {
                threadInitMove.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ProgressDialog.ProgressForm pFormMove = new ProgressDialog.ProgressForm();

            Task<Void> taskMove;
            if ( helperMove.jsonObjects.size() > 0 ) {
                taskMove = new Task<Void>() {
                    @Override
                    public Void call() throws InterruptedException {
                        String parentPlace = "https://" + community_url + "/api/core/v3/places/" + moveTo_placeID + "";
                        String data = null;
                        DeleteFilesInJive put;

                        helperMove.statusCode.clear();
                        for ( int i = 0; i < helperMove.jsonObjects.size(); i++ ) {
                            if ( !pFormMove.getDialogStage().isShowing() ) {
                                cancel();
                            }
                            else {
                                try {
                                    if( helperMove.jsonObjects.get(i).has("parent") ) {
                                        helperMove.jsonObjects.get(i).put( "parent" , parentPlace );
                                        data = helperMove.jsonObjects.get(i).toString();
                                    }

                                    put = new DeleteFilesInJive(helperMove.url + helperMove.li.get(i) , helperMove.MOVE , data );

                                    if ( put.getStatus() == 200 ) {
                                        helperMove.statusCode.add( put.getStatus() );
                                    }
                                    else {
                                        System.out.println( "status: " + put.getStatus() );
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                updateProgress( i , helperMove.jsonObjects.size() );
                                Thread.sleep(200);
                            }
                        }
                        updateProgress( helperMove.jsonObjects.size() , helperMove.jsonObjects.size() );
                        return null ;
                    }
                };
                pFormMove.activateProgressBar( taskMove );

                // this method would get the result of the task and update the UI based on its value:
                taskMove.setOnSucceeded(eventMove -> {
                    pFormMove.getDialogStage().close();
                    btn_move.setDisable(false);
                });

                taskMove.setOnCancelled( eventMoveCanceled -> btn_move.setDisable( false ) );

                btn_move.setDisable( true );
                pFormMove.getDialogStage().show();
                Thread threadMove = new Thread( taskMove );
                threadMove.start();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle( "Information Dialog" );
                alert.setHeaderText( null );
                alert.setContentText( "Move list is empty" );
                alert.showAndWait();
            }
        }
        else {

        }
    }

    //****************************** Search Place ***************************************

    private String searchPlaceName( TextField plc_field ) {

        if(mapID.isEmpty()){
            getCache();
        }

        try {
            if ( plc_field.getText() != null ) {
                for ( String s : mapID.keySet() ) {
                    if ( s.equals( plc_field.getText() ) ) {
                        plc_field.setStyle( "-fx-control-inner-background: green" );
                        plcID = mapID.get(s);
                        if( plcID != null ){
                            setCategoriesComboBox(plcID);
                        }
                    }
                }
                if ( plcID == null ) {

                    getPlaceURI = new Get();
                    getPlaceURI.get( "https://" + community_url + "/api/core/v3/places?count=100",null );
                    getCache();

                    for ( String n : mapID.keySet() ) {
                        if ( n.equals( plc_field.getText() ) ) {
                            plc_field.setStyle("-fx-control-inner-background: green");
                            plcID = mapID.get(n);
                            if( plcID != null ){
                                setCategoriesComboBox(plcID);
                            }
                        }
                    }
                    if ( plcID == null ) {
                        plc_field.setStyle("-fx-control-inner-background: red");
                        plcID = null;
                    }
                }
            }
            else {
                plc_field.setStyle( "-fx-control-inner-background: red" );
                plcID = null;
            }
            System.out.println(plcID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return plcID;
    }
    private void getCache() {
        try {
            String filePath = TempFiles.getFile_name_id();
            BufferedReader reader = new BufferedReader( new FileReader( filePath ) );

            String line;
            while ( (line = reader.readLine() ) != null ) {
                String[] parts = line.split("<-::->", 2 );
                if ( parts.length >= 2 ) {
                    String key = parts[0];
                    String value = parts[1];
                    mapID.put( key , value );
                } else {
                    System.out.println("ignoring line: " + line);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //******************************* Get Files *****************************************

    private void getFiles(Label response, GridPane root, String placeId) {
        Helper helper = null;
        List < Content > list;

        if (placeId == null){
            list = new ArrayList<>();
        }
        else {
            list = getTableViewData(placeId, helper, null);
        }

        ObservableList<Content> contents = FXCollections.observableArrayList(list);

        tvContents = new TableView<>( contents );
//        TableColumn<Content, Integer> rowNum = new TableColumn<>("Item");
//        rowNum.setCellValueFactory(new PropertyValueFactory<>("rowNumber"));
//        tvContents.getColumns().add(rowNum);
        TableColumn<Content, String> docName = new TableColumn<>("Content Title" );
        docName.setCellValueFactory(new PropertyValueFactory<>( "fileName" ) );
        tvContents.getColumns().add( docName );

        TableColumn<Content, Integer> docSize = new TableColumn<>("File Size" );
        docSize.setCellValueFactory( new PropertyValueFactory<>( "fileSize" ) );
        tvContents.getColumns().add( docSize );

        TableColumn<Content, String> docType = new TableColumn<>("Content Type" );
        docType.setCellValueFactory(new PropertyValueFactory<>( "fileType" ) );
        tvContents.getColumns().add( docType );

        TableColumn<Content, String> contentId = new TableColumn<>("Content ID" );
        contentId.setCellValueFactory(new PropertyValueFactory<>( "contentId" ) );
        tvContents.getColumns().add( contentId );

        TableColumn<Content, String> jivePlace = new TableColumn<>("Place Name" );
        jivePlace.setCellValueFactory(new PropertyValueFactory<>( "placeName" ) );
        tvContents.getColumns().add( jivePlace );

        TableColumn<Content, String> placeType = new TableColumn<>("Place Type" );
        placeType.setCellValueFactory(new PropertyValueFactory<>( "placeType" ) );
        tvContents.getColumns().add( placeType );

        tvContents.setPrefWidth( 600 );
        tvContents.setPrefHeight( 400 );

        tvContents.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TableView.TableViewSelectionModel<Content> tableViewSelectionModel = tvContents.getSelectionModel();
        ObservableList<Content> finalContents = contents;

        tableViewSelectionModel.selectedIndexProperty().addListener(( observableValue , oldValue , newValue ) -> {
            if (tableViewSelectionModel.getSelectedIndices().size() == 1) {
                int index = (int) newValue;
                response.setText("Item NO [" + finalContents.get(index).getRowNum() + "] Item Name [" + finalContents.get(index).getFileName() + "]");
                btn_deleteFiles.setDisable(false);
            }
            else if (tableViewSelectionModel.getSelectedIndices().size() > 1 ){
                btn_deleteFiles.setDisable(false);
            }
        });

        for (Node n : root.getChildren()){
            if ( n == tvContents){
                root.getChildren().remove(n);
            }
        }

        root.add(tvContents, 0, 3, 1, 1);
//        Label title = new Label("jive Content to be deleted");
//        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
//        title.setTextFill(Color.CADETBLUE);
//        root.add(title, 1, 1, 1, 1);

    }

    Map<String,String> binaryData_map = new HashMap<>();
    private List<Content> getTableViewData(String placeId, Helper helper, List<String> filters) {
        List<Content> list = new ArrayList<>();
        Map<String, List> nameSize;

        try {
            helper = new Helper( 101, placeId ,filters);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        nameSize = helper.getList();
        binaryData_map = helper.getBinaryData_map();

        for ( String fName : nameSize.keySet() ) {
            list.add( new Content(  list.size() + 1 , fName , nameSize.get(fName).get(4).toString() , nameSize.get(fName).get(0).toString() , nameSize.get(fName).get(1).toString() , nameSize.get(fName).get(2).toString() , nameSize.get(fName).get(3).toString() ,null,nameSize.get(fName).get(5).toString()) );
        }
        return list;
    }

    private List<Content> getTableViewData( Helper helper, String peopleId,String person, List<String> filters) {
        Map<String, List> nameSize;
        List<Content> list = new ArrayList<>();

        try {
            helper = new Helper( 100, peopleId , filters);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        nameSize = helper.getList();

        for ( String fName : nameSize.keySet() ) {
            list.add( new Content(  list.size() + 1  , fName , nameSize.get(fName).get(4).toString() , nameSize.get(fName).get(0).toString() , nameSize.get(fName).get(1).toString() , nameSize.get(fName).get(2).toString() , nameSize.get(fName).get(3).toString() , person,nameSize.get(fName).get(5).toString() ) );
        }
        return list;
    }

    private void download(File downloadDirectory , Button download_btn) {
        if(!binaryData_map.isEmpty()){

            String iPass = MultipartUtility.getUsername() + ":" + MultipartUtility.getPassword();
            String iAuth = "Basic " + new String(new Base64().encode(iPass.getBytes()));
            ProgressDialog.ProgressForm progressDialog_download = new ProgressDialog.ProgressForm();

            Task<Void> download_task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int index = 0;
                    for (String key : binaryData_map.keySet()){
                        if ( !progressDialog_download.getDialogStage().isShowing() ) {
                            cancel();
                        }
                        else {
                            java.net.URL binaryURL = new java.net.URL(binaryData_map.get(key));
                            HttpURLConnection httpURLConnection = (HttpURLConnection) binaryURL.openConnection();
                            httpURLConnection.addRequestProperty("Authorization", iAuth);

                            if (httpURLConnection.getResponseCode() == 200){
                                InputStream is = httpURLConnection.getInputStream();

                                File file = new File(downloadDirectory + "/" + key);
                                OutputStream outputStream = new FileOutputStream(file);
                                IOUtil.copyCompletely(is,outputStream);
                                outputStream.close();
                            }
                            else {
                                System.out.println(key + " -> " + httpURLConnection.getResponseCode() + " : " + httpURLConnection.getResponseMessage());
                            }
                            updateProgress(index++,binaryData_map.size());
                            Thread.sleep(200);
                        }

                    }
                    updateProgress(binaryData_map.size(),binaryData_map.size());
                    return null;
                }
            };
            progressDialog_download.activateProgressBar(download_task);

            download_task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    progressDialog_download.getDialogStage().close();
                    download_btn.setDisable(false);
                }
            });

            download_task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {

                    download_btn.setDisable(false);
                }
            });
            download_btn.setDisable(true);
            progressDialog_download.getDialogStage().show();
            Thread download_thread = new Thread(download_task);
            download_thread.start();
        }

    }
    File getAvatar(String url) throws IOException {
        String iPass = MultipartUtility.getUsername() + ":" + MultipartUtility.getPassword();
        String iAuth = "Basic " + new String(new Base64().encode(iPass.getBytes()));
        java.net.URL binaryURL = new java.net.URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) binaryURL.openConnection();
        httpURLConnection.addRequestProperty("Authorization", iAuth);

        if (httpURLConnection.getResponseCode() == 200){
            InputStream is = httpURLConnection.getInputStream();

            File file = new File(TempFiles.getLogs_directory() + "/" + "avatar.png");
            OutputStream outputStream = new FileOutputStream(file);
            IOUtil.copyCompletely(is,outputStream);
            outputStream.close();
            return file;
        }
        else {
            System.out.println(httpURLConnection.getResponseCode() + " : " + httpURLConnection.getResponseMessage());
        }
        return null;
    }
}

