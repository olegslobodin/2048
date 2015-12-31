package sample.address;

import static org.junit.Assert.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Created by Oleg on 30.12.2015.
 */
public class BlockTest extends Application{

    @org.junit.Test
    public void testGetValue() throws Exception {
        Application.launch();
        assertEquals(256, new Block(200, 200, 4, 0, 0, 256).getValue());
    }

    @org.junit.Test
    public void testGetAllCoords() throws Exception {

    }

    @org.junit.Test
    public void testGetCoord() throws Exception {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane layout = new AnchorPane();
        layout.setPrefSize(290, 390);
        Scene scene = new Scene(layout);
        primaryStage.show();
        primaryStage.close();
    }
}