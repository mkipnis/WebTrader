package org.DistributedATS.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import org.DistributedATS.shared.FIXUserSession;

public class LogonCanvas extends Canvas {

  final StaticTextItem statusItem = new StaticTextItem("Status:");

  final DynamicForm logonForm = new DynamicForm();

  final static String USERNAME = "Username";
  final static String PASSWORD = "Password";
  final static String LOGON = "Login";

  public LogonCanvas(final WebTrader webTrader) {
    //logonForm.setWidth("100%");
    //logonForm.setHeight("100%");
    logonForm.setNumCols(10);
    logonForm.setMargin(10);

    final TextItem usernameItem = new TextItem(USERNAME);
    usernameItem.setRequired(true);

    final PasswordItem passwordItem = new PasswordItem(PASSWORD);
    passwordItem.setRequired(true);

    // final StaticTextItem statusItem = new StaticTextItem("Status:");
    statusItem.setStartRow(true);

    statusItem.setValue("Please enter username and password");
    statusItem.setWidth("100%");
    statusItem.setColSpan(10);
    
    //this.setBackgroundColor("#C1CDE1");
    //this.setWidth100();
    this.setHeight("5%");

    SubmitItem logonButton = new SubmitItem();

    logonButton.setName(LOGON);
    logonButton.setStartRow(false);
    logonButton.setTitle(LOGON);
    logonButton.addClickHandler(
        new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
          @Override
          public void onClick(
              com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
            if (logonForm.validate()) {

              webTrader.getFIXService().logon(
                  (String)usernameItem.getValue(),
                  (String)passwordItem.getValue(),
                  new AsyncCallback<FIXUserSession>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      // TODO Auto-generated method stub
                      SC.say("Services not available, please try later.");
                    }

                    @Override
                    public void onSuccess(FIXUserSession fixUserSession) {
                      // TODO Auto-generated method stub

                      UserSessionSingleton.getInstance().setFIXUserSession(
                          fixUserSession);

                      webTrader.initiateLogon();
                    }
                  });

            } else {
              SC.warn("Submission Error", "Please enter username and password");
              logonForm.focus();
            }
          }
        });

    logonForm.setFields(usernameItem, passwordItem, logonButton, statusItem);

    addChild(logonForm);
  }

  public void disableLogon() {
    final TextItem usernameItem = (TextItem)logonForm.getItem(USERNAME);
    final TextItem passwordItem = (TextItem)logonForm.getItem(PASSWORD);
    final SubmitItem logonButton = (SubmitItem)logonForm.getItem(LOGON);

    usernameItem.setDisabled(true);
    passwordItem.setDisabled(true);
    logonButton.setDisabled(true);
  }
}
