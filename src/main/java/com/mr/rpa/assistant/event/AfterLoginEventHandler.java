package com.mr.rpa.assistant.event;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.VBox;

/**
 * Created by feng on 2020/2/4 0004
 */
public class AfterLoginEventHandler implements EventHandler<ActionEvent> {

	private VBox toolbar;

	public AfterLoginEventHandler(VBox toolbar){
		this.toolbar = toolbar;
	}

	@Override
	public void handle(ActionEvent event) {
		this.toolbar.getChildren().remove(2);
	}
}
