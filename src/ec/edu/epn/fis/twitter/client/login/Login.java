package ec.edu.epn.fis.twitter.client.login;

import com.twitterapime.rest.Credential;
import com.twitterapime.rest.TweetER;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.MainWindow;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.twitter.client.secret.APIValues;
import ec.edu.epn.fis.twitter.client.user.MainController;
import ec.edu.epn.fis.twitter.client.util.HandyDB;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.components.controls.Button;
import ec.edu.epn.fis.uil4midp.components.controls.Label;
import ec.edu.epn.fis.uil4midp.components.controls.Switch;
import ec.edu.epn.fis.uil4midp.components.controls.TextBox;
import ec.edu.epn.fis.uil4midp.util.FontManager;
import ec.edu.epn.fis.uil4midp.views.Form;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import javax.microedition.lcdui.TextField;

public class Login extends Form {

    private Label lblWelcome;
    private TextBox tbNombreUsuario;
    private TextBox tbContraseña;
    private Switch swRememberPassword;
    private Button btnLogin;

    private TweetForME midlet;
    private Environment env;

    public Login(final TweetForME midlet) {
        super("Tweet 4 ME");

        this.midlet = midlet;
        this.env = env = Environment.getInstance();
        
        setLeftButton("Salir", new ActionListener() {

            public void execute() {
                midlet.destroyApp(true);
            }
        });
    }

    public void initialize() {
        lblWelcome = new Label("Ingresar a Twitter");
        lblWelcome.setFont(FontManager.getBoldFont());
        addVisualComponent(lblWelcome);

        tbNombreUsuario = new TextBox("Usuario", 50, TextField.ANY | TextField.NON_PREDICTIVE);
        addVisualComponent(tbNombreUsuario);

        tbContraseña = new TextBox("Contraseña", 50, true);
        addVisualComponent(tbContraseña);

        swRememberPassword = new Switch("Recordar mis datos");
        addVisualComponent(swRememberPassword);

        btnLogin = new Button("Aceptar");
        btnLogin.setActionListener(new ActionListener() {

            public void execute() {
                // Verificar que se hayan ingresado nombre de usuario y contraseña.
                if (tbNombreUsuario.getText().length() == 0 || tbContraseña.getText().length() == 0) {
                    showDialog(new MessageDialog("Tweet 4 ME", "Debe ingresar su nombre de usuario y contraseña."));
                    return;
                }
                
                //Preparar el cuadro de progreso
                final ProgressDialog pdLogin = new ProgressDialog("Tweet 4 ME", "Iniciando sesión...");
                pdLogin.setDismissActionListener(new ActionListener() {
                    public void execute() {
                        try {
                            //Verificar si se debe almacenar la info
                            boolean shouldStoreData = swRememberPassword.isTurnedOn();
                            if (shouldStoreData) {
                                HandyDB.writeBoolean("T4M_PWSaved", true);
                                HandyDB.writeString("T4M_User", tbNombreUsuario.getText());
                                HandyDB.writeString("T4M_Pass", tbContraseña.getText());
                            } else {
                                HandyDB.delete("T4M_PWSaved");
                                HandyDB.delete("T4M_User");
                                HandyDB.delete("T4M_Pass");
                            }

                            //Añadir el nuevo controlador a la ventana.
                            controller.getWindow().setViewController(new MainController((MainWindow) controller.getWindow(), midlet));
                        } catch (IOException ex) {
                        }
                    }
                });

                //Mostrar el diálogo de progreso
                showDialog(pdLogin);

                //Iniciar sesión
                Thread t = new Thread(new Runnable() {

                    public void run() {
                        try {
                            Credential cr = new Credential(tbNombreUsuario.getText(),
                                    tbContraseña.getText(),
                                    APIValues.APP_CONSUMER_KEY,
                                    APIValues.APP_CONSUMER_SECRET);

                            UserAccountManager manager = UserAccountManager.getInstance(cr);
                            manager.setServiceURL(UserAccountManager.TWITTER_API_URL_SERVICE_OAUTH_ACCESS_TOKEN, "http://api.twitter.com/oauth/access_token");

                            env.setUserAccountManager(manager);

                            if (manager.verifyCredential()) {
                                env.setTwitter(TweetER.getInstance(manager));
                                pdLogin.close();
                            }  else {
                                pdLogin.close(false);
                                showDialog(new MessageDialog("Tweet 4 ME", "Imposible iniciar sesión. Revise su nombre de usuario y contraseña."));
                            }
                            return;
                        } catch (IOException ex) {
                            showDialog(new MessageDialog("Tweet 4 ME", "Conexión a Internet falló. Imposible conectar a Twitter."));
                        } catch (LimitExceededException ex) {
                            showDialog(new MessageDialog("Tweet 4 ME", "Límites de conexión con Twitter excedidos."));
                        } catch (Exception ex) {
                            showDialog(new MessageDialog("Tweet 4 ME", "Imposible iniciar sesión. Revise su nombre de usuario y contraseña."));
                        }
                        pdLogin.close(false);
                    }
                }, "T4M_LoginThread");
                t.start();
            }
        });
        addVisualComponent(btnLogin);

        String userName;
        String passWord;
        boolean isPasswordSaved = HandyDB.readBoolean("T4M_PWSaved");
        if (isPasswordSaved) {
            userName = HandyDB.readString("T4M_User");
            passWord = HandyDB.readString("T4M_Pass");

            tbNombreUsuario.setText(userName);
            tbContraseña.setText(passWord);
            swRememberPassword.setTurnedOn(true);
        }
    }
}
