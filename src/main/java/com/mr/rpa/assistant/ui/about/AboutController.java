package com.mr.rpa.assistant.ui.about;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import com.mr.rpa.assistant.util.AssistantUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class AboutController implements Initializable {

    private static final String WEIBO = "http://microrule.com/";
    private static final String WEIXIN = "http://microrule.com/";
    private static final String QQ = "http://microrule.com/";
    private static final String WEBSITE = "http://microrule.com/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
    }

    private void loadWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
            handleWebpageLoadException(url);
        }
    }

    private void handleWebpageLoadException(String url) {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        Stage stage = new Stage();
        Scene scene = new Scene(new StackPane(browser));
        stage.setScene(scene);
        stage.setTitle("MR");
        stage.show();
        AssistantUtil.setStageIcon(stage);
    }

    @FXML
    private void loadMainPage(ActionEvent event) {
        loadWebpage(WEBSITE);
    }

    @FXML
    private void loadWeiBo(ActionEvent event) {
        loadWebpage(WEIBO);
    }

    @FXML
    private void loadWeiXin(ActionEvent event) {
        loadWebpage(WEIBO);
    }

    @FXML
    private void loadQQ(ActionEvent event) {
        loadWebpage(QQ);
    }
}
