package com.a.e.qurbanzada;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by aqn3130 on 18/11/2017.
 */
public class Content {
    private final SimpleIntegerProperty rowNumber = new SimpleIntegerProperty(0);
    private final SimpleStringProperty fileName = new SimpleStringProperty("");
    private final SimpleStringProperty viewCount = new SimpleStringProperty("");
    private final SimpleStringProperty fileSize = new SimpleStringProperty("");
    private final SimpleStringProperty fileType = new SimpleStringProperty("");
    private final SimpleStringProperty placeName = new SimpleStringProperty("");
    private final SimpleStringProperty placeType = new SimpleStringProperty("");
    private final SimpleStringProperty person = new SimpleStringProperty("");
    private final SimpleStringProperty contentId = new SimpleStringProperty("");

//    private final SimpleLongProperty lastModified = new SimpleLongProperty(0);

    public Content(){
        this(0,"", "" ,"","","","","", "");

    }

    public SimpleStringProperty fileTypeProperty() {
        return fileType;
    }

    public Content(int rowNumber, String fileName, String viewCount ,String fileSize, String fileType, String placeName,String placeType,String person,String contentId){
        setRowNum(rowNumber);
        setFileName(fileName);
        setViewCount(viewCount);
        setFileSize(fileSize);
        setFileType(fileType);
        setPlaceName(placeName);
        setPlaceType(placeType);
        setPerson(person);
        setContentId(contentId);
//        setLastModified(lastModified);
    }

    public void setRowNum(int rn){ rowNumber.set(rn); }
    public int getRowNum(){return rowNumber.get();}

    public String getFileName(){
        return fileName.get();
    }
    public void setFileName(String fName) {
        fileName.set(fName);
    }

    public void setViewCount(String vc){ viewCount.set(vc); }
    public String getViewCount(){return viewCount.get();}

    public void setFileSize(String fSize){
        fileSize.set(fSize);
    }
    public String getFileSize(){return fileSize.get();}

    public void setPlaceName(String pName){
        placeName.set(pName);
    }
    public void setPlaceType(String pType){ placeType.set(pType);}
    public String getPlaceType(){
        return placeType.get();
    }
    public String getPlaceName(){
        return placeName.get();
    }

    public void setFileType(String fType) {
        fileType.set(fType);
    }
    public String getFileType() {
        return fileType.get();
    }

    public void setPerson(String p){
        person.set(p);
    }
    public String getPerson(){
        return person.get();
    }

    public String getContentId() {
        return contentId.get();
    }

    public SimpleStringProperty contentIdProperty() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId.set(contentId);
    }
//    public long getLastModified(){
//        return lastModified.get();
//    }
//    public void setLastModified(long lModified) {
//        lastModified.set(lModified);
//    }

}
