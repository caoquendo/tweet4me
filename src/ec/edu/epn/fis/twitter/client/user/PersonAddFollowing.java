package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.rest.UserAccount;
import com.twitterapime.search.InvalidQueryException;
import com.twitterapime.search.LimitExceededException;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.components.controls.Button;
import ec.edu.epn.fis.uil4midp.components.controls.Label;
import ec.edu.epn.fis.uil4midp.components.controls.TextBox;
import ec.edu.epn.fis.uil4midp.controllers.NavigableController;
import ec.edu.epn.fis.uil4midp.views.ConfirmationDialog;
import ec.edu.epn.fis.uil4midp.views.Dialog;
import ec.edu.epn.fis.uil4midp.views.Form;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import javax.microedition.lcdui.TextField;

public class PersonAddFollowing extends Form {

    private Label lblPrompt;
    private TextBox tbSearchInput;
    private Button btnSearch;
    private TweetForME midlet;
    private Environment env;

    public PersonAddFollowing(TweetForME midlet) {
        super("Seguir a alguien");

        this.midlet = midlet;
        this.env = Environment.getInstance();
    }

    public void initialize() {
        lblPrompt = new Label("Sigue a nuevas personas.");
        addVisualComponent(lblPrompt);

        tbSearchInput = new TextBox("Nombre de usuario", 40, TextField.ANY);
        addVisualComponent(tbSearchInput);

        btnSearch = new Button("Buscar y seguir");
        btnSearch.setActionListener(new FollowPersonActionListener());
        addVisualComponent(btnSearch);
    }

    class FollowPersonActionListener implements ActionListener {

        public void execute() {
            if (tbSearchInput.getText().length() == 0) {
                showDialog(new MessageDialog("Seguir Persona", "Debes ingresar un nombre de usuario."));
                return;
            }

            final String userName = tbSearchInput.getText().startsWith("@") ? tbSearchInput.getText().substring(1) : tbSearchInput.getText();
            final UserAccount personAccount = new UserAccount(userName);

            final ConfirmationDialog cdFindFollow = new ConfirmationDialog("Seguir Persona", "¿Buscar y seguir a @" + userName + "?");
            cdFindFollow.setDismissActionListener(new ActionListener() {

                public void execute() {
                    if (cdFindFollow.getDialogResult() == Dialog.DIALOG_YES) {
                        final ProgressDialog pdFindFollow = new ProgressDialog("Seguir Persona", "Espere...");
                        pdFindFollow.setDismissActionListener(new ActionListener() {

                            public void execute() {
                                showDialog(new MessageDialog("Seguir Persona", "Ahora sigues a " + "@" + userName + "."));
                                ((NavigableController) getController()).goToStartView();
                            }
                        });


                        Thread t = new Thread(new Runnable() {

                            public void run() {
                                try {
                                    env.getFriendsManager().follow(personAccount);
                                    pdFindFollow.close();
                                    return;
                                } catch (IOException ex) {
                                    showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                                } catch (LimitExceededException ex) {
                                    showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                                } catch (InvalidQueryException ex) {
                                    showDialog(new MessageDialog("Seguir Persona", "Ya sigues a " + "@" + userName + " o no existe."));
                                }
                                pdFindFollow.close(false);
                            }
                        });

                        showDialog(pdFindFollow);
                        t.start();
                    } else {
                        tbSearchInput.setText("");
                    }
                }
            });

            showDialog(cdFindFollow);
        }
    }
}
