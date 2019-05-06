package ru.exsoft.gui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.exsoft.OpenCV;

public class GeneralController {
    @FXML
    private ImageView imageviev;

    public void initialize(){
        new Thread(() -> {
            while (OpenCV.getWatcher() == null){}
            while (true){
                updateImage(OpenCV.getWatcher().getCurrentImage());
            }
        }).start();
    }

    public void updateImage(Image image) {
        this.imageviev.setImage(image);
    }

}
