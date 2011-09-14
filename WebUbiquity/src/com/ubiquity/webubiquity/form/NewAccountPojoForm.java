package com.ubiquity.webubiquity.form;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.validator.EmailValidator;

import com.ubiquity.webubiquity.WebUbiquityApplication;
import com.ubiquity.webubiquity.window.ListWindow;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class NewAccountPojoForm extends VerticalLayout {

    // the 'POJO' we're editing
    Account account;

    private static final String COMMON_FIELD_WIDTH = "12em";

    public NewAccountPojoForm() {

        account = new Account(); // a person POJO
        BeanItem<Account> accountItem = new BeanItem<Account>(account); // item from
                                                                    // POJO

        //Submitted panel
        final Label submitted = new Label("Thank you for signing up for our beta! You have " +
        "been placed on the waiting list and will receive an email when you are accepted " + 
        "into the beta.");
        
        final Button back = new Button("Back", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                NewAccountPojoForm.this.getApplication().close();
            }
        });
        
        // Create the Form
        final Form accountForm = new Form();
        accountForm.setCaption("Create Account");
        accountForm.setWriteThrough(false); // we want explicit 'apply'
        accountForm.setInvalidCommitted(false); // no invalid values in datamodel

        // FieldFactory for customizing the fields and adding validators
        accountForm.setFormFieldFactory(new AccountFieldFactory());
        accountForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

        // Determines which properties are shown, and in which order:
        accountForm.setVisibleItemProperties(Arrays.asList(new String[] {
                "username", "email"}));

        // Add form to layout
        addComponent(accountForm);

        // The cancel / apply buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        Button discardChanges = new Button("Discard changes",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        accountForm.discard();
                    }
                });
        discardChanges.setStyleName(BaseTheme.BUTTON_LINK);
        buttons.addComponent(discardChanges);
        buttons.setComponentAlignment(discardChanges, Alignment.MIDDLE_LEFT);

        Button apply = new Button("Submit", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    accountForm.commit();
                    NewAccountPojoForm.this.removeAllComponents();
                    NewAccountPojoForm.this.addComponent(submitted);
                    NewAccountPojoForm.this.addComponent(back);
                    ((WebUbiquityApplication) NewAccountPojoForm.this.getApplication()).betaSignup(account.getUsername(), account.getEmail());
                } catch (Exception e) {
                    // Ignored, we'll let the Form handle the errors
                }
            }
        });
        
        buttons.addComponent(back);
        buttons.addComponent(apply);
        accountForm.getFooter().addComponent(buttons);
        accountForm.getFooter().setMargin(false, false, true, true);
    }

    private class AccountFieldFactory extends DefaultFieldFactory {


        public AccountFieldFactory() {
        }

        @Override
        public Field createField(Item item, Object propertyId,
                Component uiContext) {
            Field f;
            f = super.createField(item, propertyId, uiContext);

            if ("username".equals(propertyId)) {
                TextField tf = (TextField) f;
                tf.setRequired(true);
                tf.setRequiredError("Please enter a user name");
                tf.setWidth(COMMON_FIELD_WIDTH);
                tf.addValidator(new UsernameValidator(
                        "Username already taken! Choose another."));
            } else if ("email".equals(propertyId)) {
                TextField tf = (TextField) f;
                tf.setRequired(true);
                tf.setRequiredError("Please enter a valid email address");
                tf.setWidth(COMMON_FIELD_WIDTH);
                tf.addValidator(new FormEmailValidator(
                        "The email address entered is not valid! Try again."));
            }
            return f;
        }
    }

    public class Account implements Serializable {

        private String username = "";
        private String email = "";
        private UUID uuid;

        public Account() {
            uuid = UUID.randomUUID();
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public UUID getUuid() {
            return uuid;
        }
    }

    public class UsernameValidator implements Validator {

    	private String message;
    	
    	public UsernameValidator(String message) {
    		this.message = message;
    	}
    	
		@Override
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
                throw new InvalidValueException(message);
            }
		}

		@Override
		public boolean isValid(Object value) {
			if (value == null || !(value instanceof String)) {
				return false;
			}
			if (((WebUbiquityApplication) NewAccountPojoForm.this.getApplication()).userExists((String)value)) {
				return false;
			}
			return true;
		}
    	
    }
    
    public class FormEmailValidator implements Validator {

        private String message;

        public FormEmailValidator(String message) {
            this.message = message;
        }

        public boolean isValid(Object value) {
        	return EmailValidator.getInstance().isValid((String) value);
        }

        public void validate(Object value) throws InvalidValueException {
        	if (!isValid(value)) {
                throw new InvalidValueException(message);
            }
        }

    }
}
