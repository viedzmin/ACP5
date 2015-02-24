package ua.artcode.view;

import ua.artcode.manager.ClientManager;
import ua.artcode.manager.ClientManagerImpl;

import java.util.Scanner;

/**
 * Created by serhii on 24.02.15.
 */
public class MenuConsole {

    private ClientManager clientManager = new ClientManagerImpl();
    private Scanner scanner = new Scanner(System.in);

    public void showMenu(){
        System.out.println("1.Create client");
        System.out.println("2.Show client");
        System.out.println("3.Exit");
    }

    public void choose(int choose){
        switch (choose) {
            case 1 : {
                System.out.println("Input login");
                String login = scanner.nextLine();
                System.out.println("Input pass");
                String pass = scanner.nextLine();
                System.out.println("Input phone");
                String phone = scanner.nextLine();
                System.out.println("Input email");
                String email = scanner.nextLine();
                clientManager.register(login, pass, phone, email);
                break;
            }
            case 2 : {
                System.out.println("Input login");
                String login = scanner.nextLine();
                System.out.println("Input pass");
                String pass = scanner.nextLine();
                String accessKey = clientManager.signIn(login,pass);
                System.out.println(accessKey);
                break;
            }
            case 3 : {
                System.exit(1);
                break;
            }
        }
    }



}